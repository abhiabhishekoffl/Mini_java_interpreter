package com.novalang.parser;

import com.novalang.exception.ParseException;
import com.novalang.lexer.Token;
import com.novalang.parser.nodes.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Node> parse() {
        List<Node> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        return statements;
    }

    private Node statement() {
        if (match(Token.Type.LET)) {
            return letStatement();
        }
        if (match(Token.Type.IF)) {
            return ifStatement();
        }
        if (match(Token.Type.WHILE)) {
            return whileStatement();
        }
        if (match(Token.Type.FN)) {
            return functionDef();
        }
        if (match(Token.Type.RETURN)) {
            return returnStatement();
        }
        if (match(Token.Type.PRINT)) {
            return printStatement();
        }
        if (check(Token.Type.LBRACE)) {
            return blockStatement();
        }
        
        // Check for variable assignment (e.g. x = value;)
        if (check(Token.Type.IDENTIFIER) && peekNext().getType() == Token.Type.EQUAL) {
            return assignStatement();
        }
        
        return expressionStatement();
    }

    private Node letStatement() {
        Token nameToken = consume(Token.Type.IDENTIFIER, "Expect variable name.");
        consume(Token.Type.EQUAL, "Expect '=' after variable name.");
        Node value = expression();
        consume(Token.Type.SEMICOLON, "Expect ';' after variable declaration.");
        return new VarAssignNode(nameToken.getLexeme(), value, true);
    }

    private Node assignStatement() {
        Token nameToken = consume(Token.Type.IDENTIFIER, "Expect variable name.");
        consume(Token.Type.EQUAL, "Expect '=' after variable name.");
        Node value = expression();
        consume(Token.Type.SEMICOLON, "Expect ';' after variable assignment.");
        return new VarAssignNode(nameToken.getLexeme(), value, false);
    }

    private Node ifStatement() {
        boolean paren = match(Token.Type.LPAR);
        Node condition = expression();
        if (paren) {
            consume(Token.Type.RPAR, "Expect ')' after if condition.");
        }
        BlockNode thenBranch = blockStatement();
        Node elseBranch = null;
        if (match(Token.Type.ELSE)) {
            if (check(Token.Type.IF)) {
                elseBranch = statement();
            } else if (check(Token.Type.LBRACE)) {
                elseBranch = blockStatement();
            } else {
                throw new ParseException(peek().getLine(), "Expect 'if' or '{' after 'else'.");
            }
        }
        return new IfNode(condition, thenBranch, elseBranch);
    }

    private Node whileStatement() {
        boolean paren = match(Token.Type.LPAR);
        Node condition = expression();
        if (paren) {
            consume(Token.Type.RPAR, "Expect ')' after while condition.");
        }
        BlockNode body = blockStatement();
        return new WhileNode(condition, body);
    }

    private Node functionDef() {
        Token nameToken = consume(Token.Type.IDENTIFIER, "Expect function name.");
        consume(Token.Type.LPAR, "Expect '(' after function name.");
        List<String> parameters = new ArrayList<>();
        if (!check(Token.Type.RPAR)) {
            do {
                Token paramToken = consume(Token.Type.IDENTIFIER, "Expect parameter name.");
                parameters.add(paramToken.getLexeme());
            } while (match(Token.Type.COMMA));
        }
        consume(Token.Type.RPAR, "Expect ')' after parameters.");
        BlockNode body = blockStatement();
        return new FunctionDefNode(nameToken.getLexeme(), parameters, body);
    }

    private Node returnStatement() {
        Node expr = null;
        if (!check(Token.Type.SEMICOLON)) {
            expr = expression();
        }
        consume(Token.Type.SEMICOLON, "Expect ';' after return statement.");
        return new ReturnNode(expr);
    }

    private Node printStatement() {
        Node expr = expression();
        consume(Token.Type.SEMICOLON, "Expect ';' after print statement.");
        return new PrintNode(expr);
    }

    private BlockNode blockStatement() {
        consume(Token.Type.LBRACE, "Expect '{' to start block.");
        List<Node> statements = new ArrayList<>();
        while (!check(Token.Type.RBRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        consume(Token.Type.RBRACE, "Expect '}' to end block.");
        return new BlockNode(statements);
    }

    private Node expressionStatement() {
        Node expr = expression();
        consume(Token.Type.SEMICOLON, "Expect ';' after expression.");
        return expr;
    }

    private Node expression() {
        return logicalOr();
    }

    private Node logicalOr() {
        Node expr = logicalAnd();
        while (match(Token.Type.PIPE_PIPE)) {
            Token op = previous();
            Node right = logicalAnd();
            expr = new BinaryOpNode(op.getLexeme(), expr, right);
        }
        return expr;
    }

    private Node logicalAnd() {
        Node expr = comparison();
        while (match(Token.Type.AMP_AMP)) {
            Token op = previous();
            Node right = comparison();
            expr = new BinaryOpNode(op.getLexeme(), expr, right);
        }
        return expr;
    }

    private Node comparison() {
        Node expr = term();
        while (match(Token.Type.EQ_EQ, Token.Type.BANG_EQ, Token.Type.LESS, Token.Type.LESS_EQ, Token.Type.GREATER, Token.Type.GREATER_EQ)) {
            Token op = previous();
            Node right = term();
            expr = new BinaryOpNode(op.getLexeme(), expr, right);
        }
        return expr;
    }

    private Node term() {
        Node expr = factor();
        while (match(Token.Type.PLUS, Token.Type.MINUS)) {
            Token op = previous();
            Node right = factor();
            expr = new BinaryOpNode(op.getLexeme(), expr, right);
        }
        return expr;
    }

    private Node factor() {
        Node expr = unary();
        while (match(Token.Type.STAR, Token.Type.SLASH, Token.Type.PERCENT)) {
            Token op = previous();
            Node right = unary();
            expr = new BinaryOpNode(op.getLexeme(), expr, right);
        }
        return expr;
    }

    private Node unary() {
        if (match(Token.Type.BANG, Token.Type.MINUS)) {
            Token op = previous();
            Node operand = unary();
            return new UnaryOpNode(op.getLexeme(), operand);
        }
        return primary();
    }

    private Node primary() {
        if (match(Token.Type.FALSE)) return new BooleanNode(false);
        if (match(Token.Type.TRUE)) return new BooleanNode(true);
        
        if (match(Token.Type.NUMBER)) {
            return new NumberNode((Double) previous().getLiteral());
        }
        
        if (match(Token.Type.STRING)) {
            return new StringNode((String) previous().getLiteral());
        }
        
        if (match(Token.Type.IDENTIFIER)) {
            Token nameToken = previous();
            // Check if function call
            if (match(Token.Type.LPAR)) {
                List<Node> arguments = new ArrayList<>();
                if (!check(Token.Type.RPAR)) {
                    do {
                        arguments.add(expression());
                    } while (match(Token.Type.COMMA));
                }
                consume(Token.Type.RPAR, "Expect ')' after arguments.");
                return new FunctionCallNode(nameToken.getLexeme(), arguments);
            }
            return new VarAccessNode(nameToken.getLexeme());
        }
        
        if (match(Token.Type.LPAR)) {
            Node expr = expression();
            consume(Token.Type.RPAR, "Expect ')' after expression.");
            return expr;
        }
        
        throw new ParseException(peek().getLine(), "Expect expression, found '" + peek().getLexeme() + "'");
    }

    private boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(Token.Type type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == Token.Type.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        if (current + 1 >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(current + 1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(Token.Type type, String message) {
        if (check(type)) return advance();
        throw new ParseException(peek().getLine(), message);
    }
}
