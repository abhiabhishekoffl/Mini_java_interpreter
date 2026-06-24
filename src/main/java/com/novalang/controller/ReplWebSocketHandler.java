package com.novalang.controller;

import com.novalang.evaluator.Environment;
import com.novalang.evaluator.Evaluator;
import com.novalang.exception.LexerException;
import com.novalang.exception.ParseException;
import com.novalang.lexer.Lexer;
import com.novalang.lexer.Token;
import com.novalang.model.ReplSession;
import com.novalang.parser.Parser;
import com.novalang.parser.nodes.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
@EnableScheduling
public class ReplWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, ReplSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> wsSessions = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        Environment env = new Environment();
        activeSessions.put(sessionId, new ReplSession(sessionId, env));
        wsSessions.put(sessionId, session);

        String welcome = 
                "==================================================\r\n" +
                "  NovaLang Web REPL (v1.0.0)\r\n" +
                "  Type ':help' for assistance or 'exit' to quit.\r\n" +
                "==================================================\r\n" +
                "nova> ";
        session.sendMessage(new TextMessage(welcome));
    }

    @Override
    protected void handleTextMessage(final WebSocketSession session, TextMessage message) throws Exception {
        final String sessionId = session.getId();
        final ReplSession replSession = activeSessions.get(sessionId);
        if (replSession == null) {
            session.close();
            return;
        }

        replSession.updateActiveTime();

        String payload = message.getPayload().trim();

        if (payload.isEmpty()) {
            session.sendMessage(new TextMessage("nova> "));
            return;
        }

        // Handle commands
        if (payload.equalsIgnoreCase("exit") || payload.equalsIgnoreCase("quit")) {
            session.sendMessage(new TextMessage("Goodbye!\r\n"));
            session.close();
            return;
        }

        if (payload.startsWith(":")) {
            handleCommand(session, payload, replSession);
            return;
        }

        // Auto-append semicolon if missing for standard statements
        if (!payload.endsWith(";") && !payload.endsWith("{") && !payload.endsWith("}")) {
            payload = payload + ";";
        }

        final String finalPayload = payload;
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() {
                try {
                    Lexer lexer = new Lexer(finalPayload);
                    List<Token> tokens = lexer.scanTokens();

                    Parser parser = new Parser(tokens);
                    List<Node> statements = parser.parse();

                    Evaluator evaluator = new Evaluator();
                    Environment env = replSession.getEnvironment();

                    StringBuilder outputBuilder = new StringBuilder();
                    for (Node statement : statements) {
                        Object result = evaluator.evaluate(statement, env);
                        
                        // If it's an expression statement, we should print its value directly
                        if (statement instanceof VarAccessNode || statement instanceof NumberNode ||
                            statement instanceof StringNode || statement instanceof BooleanNode ||
                            statement instanceof BinaryOpNode || statement instanceof UnaryOpNode ||
                            statement instanceof FunctionCallNode) {
                            
                            if (evaluator.getOutput().isEmpty() && result != null) {
                                outputBuilder.append(result).append("\r\n");
                            }
                        }
                    }

                    // Append captured prints
                    String prints = evaluator.getOutput().replace("\n", "\r\n");
                    outputBuilder.append(prints);

                    return outputBuilder.toString();
                } catch (LexerException e) {
                    return e.getMessage() + "\r\n";
                } catch (ParseException e) {
                    return e.getMessage() + "\r\n";
                } catch (RuntimeException e) {
                    return e.getMessage() + "\r\n";
                } catch (Exception e) {
                    return "Internal error: " + e.getMessage() + "\r\n";
                }
            }
        });

        try {
            String result = future.get(5, TimeUnit.SECONDS);
            session.sendMessage(new TextMessage(result + "nova> "));
        } catch (TimeoutException e) {
            future.cancel(true);
            session.sendMessage(new TextMessage("Execution timed out (5s limit). Check for infinite loops.\r\nnova> "));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            session.sendMessage(new TextMessage("Execution interrupted.\r\nnova> "));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
            session.sendMessage(new TextMessage(errorMsg + "\r\nnova> "));
        }
    }

    private void handleCommand(WebSocketSession session, String command, ReplSession replSession) throws IOException {
        String cmd = command.toLowerCase();
        if (cmd.equals(":help")) {
            String help = 
                    "Available Commands:\r\n" +
                    "  :help     - Show this help message\r\n" +
                    "  :env      - List all variables currently defined in the environment\r\n" +
                    "  :reset    - Clear all variables and reset environment\r\n" +
                    "  :clear    - Clear terminal screen\r\n" +
                    "  exit/quit - Close connection\r\n" +
                    "\r\n" +
                    "NovaLang Features:\r\n" +
                    "  let x = 10;            - Variable declaration\r\n" +
                    "  x = 20;                - Variable assignment\r\n" +
                    "  print x;               - Print statements\r\n" +
                    "  if x > 5 { ... }       - Conditionals\r\n" +
                    "  while x > 0 { ... }    - Loops\r\n" +
                    "  fn add(a, b) { ... }   - Functions\r\n" +
                    "nova> ";
            session.sendMessage(new TextMessage(help));
        } else if (cmd.equals(":env")) {
            Environment env = replSession.getEnvironment();
            StringBuilder sb = new StringBuilder();
            Map<String, Object> bindings = env.getBindings();
            if (bindings.isEmpty()) {
                sb.append("Environment is empty.\r\n");
            } else {
                sb.append("Variables in scope:\r\n");
                for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                    sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\r\n");
                }
            }
            sb.append("nova> ");
            session.sendMessage(new TextMessage(sb.toString()));
        } else if (cmd.equals(":reset")) {
            replSession.updateActiveTime();
            // Clear bindings or replace environment
            activeSessions.put(session.getId(), new ReplSession(session.getId(), new Environment()));
            session.sendMessage(new TextMessage("Environment reset.\r\nnova> "));
        } else if (cmd.equals(":clear")) {
            // VT100 clear screen command
            session.sendMessage(new TextMessage("\u001b[2J\u001b[Hnova> "));
        } else {
            session.sendMessage(new TextMessage("Unknown command: " + command + ". Type ':help' for assistance.\r\nnova> "));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        activeSessions.remove(sessionId);
        wsSessions.remove(sessionId);
    }

    // Run every minute to clean up inactive sessions
    @Scheduled(fixedDelay = 60000)
    public void cleanUpInactiveSessions() {
        long now = System.currentTimeMillis();
        long thirtyMinutes = 30 * 60 * 1000;

        for (Map.Entry<String, ReplSession> entry : activeSessions.entrySet()) {
            if (now - entry.getValue().getLastActiveTime() > thirtyMinutes) {
                String sessionId = entry.getKey();
                WebSocketSession session = wsSessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage("\r\nSession timed out due to 30 minutes of inactivity.\r\n"));
                        session.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
                activeSessions.remove(sessionId);
                wsSessions.remove(sessionId);
            }
        }
    }
}
