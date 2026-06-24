package com.novalang.service;

import com.novalang.evaluator.Environment;
import com.novalang.evaluator.Evaluator;
import com.novalang.exception.LexerException;
import com.novalang.exception.ParseException;
import com.novalang.lexer.Lexer;
import com.novalang.lexer.Token;
import com.novalang.model.ExecuteResponse;
import com.novalang.parser.Parser;
import com.novalang.parser.nodes.Node;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
public class NovaLangService {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ExecuteResponse execute(final String code) {
        if (code == null) {
            return new ExecuteResponse(false, "", "No code provided", 0);
        }
        if (code.length() > 10000) {
            return new ExecuteResponse(false, "", "Code exceeds maximum length of 10,000 characters", 0);
        }

        final long startTime = System.currentTimeMillis();

        Future<ExecuteResponse> future = executorService.submit(new Callable<ExecuteResponse>() {
            @Override
            public ExecuteResponse call() {
                try {
                    Lexer lexer = new Lexer(code);
                    List<Token> tokens = lexer.scanTokens();

                    Parser parser = new Parser(tokens);
                    List<Node> statements = parser.parse();

                    Environment env = new Environment();
                    Evaluator evaluator = new Evaluator();

                    for (Node statement : statements) {
                        evaluator.evaluate(statement, env);
                    }

                    long duration = System.currentTimeMillis() - startTime;
                    return new ExecuteResponse(true, evaluator.getOutput(), null, duration);
                } catch (LexerException e) {
                    long duration = System.currentTimeMillis() - startTime;
                    return new ExecuteResponse(false, "", e.getMessage(), duration);
                } catch (ParseException e) {
                    long duration = System.currentTimeMillis() - startTime;
                    return new ExecuteResponse(false, "", e.getMessage(), duration);
                } catch (RuntimeException e) {
                    long duration = System.currentTimeMillis() - startTime;
                    return new ExecuteResponse(false, "", e.getMessage(), duration);
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    return new ExecuteResponse(false, "", "Internal execution error: " + e.getMessage(), duration);
                }
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // Attempt to interrupt the thread
            long duration = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(false, "", "Execution timed out (5s limit). Check for infinite loops.", duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long duration = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(false, "", "Execution interrupted.", duration);
        } catch (ExecutionException e) {
            long duration = System.currentTimeMillis() - startTime;
            Throwable cause = e.getCause();
            String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
            return new ExecuteResponse(false, "", errorMsg, duration);
        }
    }

    public ExecuteResponse validate(String code) {
        if (code == null) {
            return new ExecuteResponse(false, "", "No code provided", 0);
        }
        if (code.length() > 10000) {
            return new ExecuteResponse(false, "", "Code exceeds maximum length of 10,000 characters", 0);
        }

        long startTime = System.currentTimeMillis();
        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.scanTokens();

            Parser parser = new Parser(tokens);
            parser.parse();

            long duration = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(true, "", null, duration);
        } catch (LexerException e) {
            long duration = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(false, "", e.getMessage(), duration);
        } catch (ParseException e) {
            long duration = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(false, "", e.getMessage(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new ExecuteResponse(false, "", e.getMessage(), duration);
        }
    }
}
