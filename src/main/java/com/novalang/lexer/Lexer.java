package com.novalang.lexer;

import com.novalang.exception.LexerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, Token.Type> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("let", Token.Type.LET);
        keywords.put("true", Token.Type.TRUE);
        keywords.put("false", Token.Type.FALSE);
        keywords.put("if", Token.Type.IF);
        keywords.put("else", Token.Type.ELSE);
        keywords.put("while", Token.Type.WHILE);
        keywords.put("fn", Token.Type.FN);
        keywords.put("return", Token.Type.RETURN);
        keywords.put("print", Token.Type.PRINT);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(Token.Type.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(Token.Type.LPAR); break;
            case ')': addToken(Token.Type.RPAR); break;
            case '{': addToken(Token.Type.LBRACE); break;
            case '}': addToken(Token.Type.RBRACE); break;
            case ',': addToken(Token.Type.COMMA); break;
            case ';': addToken(Token.Type.SEMICOLON); break;
            case '+': addToken(Token.Type.PLUS); break;
            case '-': addToken(Token.Type.MINUS); break;
            case '*': addToken(Token.Type.STAR); break;
            case '/': addToken(Token.Type.SLASH); break;
            case '%': addToken(Token.Type.PERCENT); break;
            case '!':
                addToken(match('=') ? Token.Type.BANG_EQ : Token.Type.BANG);
                break;
            case '=':
                addToken(match('=') ? Token.Type.EQ_EQ : Token.Type.EQUAL);
                break;
            case '<':
                addToken(match('=') ? Token.Type.LESS_EQ : Token.Type.LESS);
                break;
            case '>':
                addToken(match('=') ? Token.Type.GREATER_EQ : Token.Type.GREATER);
                break;
            case '&':
                if (match('&')) {
                    addToken(Token.Type.AMP_AMP);
                } else {
                    throw new LexerException(line, "Unexpected character '&', expected '&&'");
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(Token.Type.PIPE_PIPE);
                } else {
                    throw new LexerException(line, "Unexpected character '|', expected '||'");
                }
                break;
            case '#':
                // A comment goes until the end of the line.
                while (peek() != '\n' && !isAtEnd()) {
                    advance();
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new LexerException(line, "Unexpected character '" + c + "'");
                }
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            if (peek() == '\\' && peekNext() == '"') {
                advance(); // consume the backslash
            }
            advance();
        }

        if (isAtEnd()) {
            throw new LexerException(line, "Unterminated string.");
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        // Handle escapes
        value = value.replace("\\\"", "\"")
                     .replace("\\n", "\n")
                     .replace("\\t", "\t")
                     .replace("\\\\", "\\");
        addToken(Token.Type.STRING, value);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        String lexeme = source.substring(start, current);
        try {
            Double value = Double.parseDouble(lexeme);
            addToken(Token.Type.NUMBER, value);
        } catch (NumberFormatException e) {
            throw new LexerException(line, "Invalid number literal: " + lexeme);
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        Token.Type type = keywords.get(text);
        if (type == null) {
            type = Token.Type.IDENTIFIER;
        }
        addToken(type);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(Token.Type type) {
        addToken(type, null);
    }

    private void addToken(Token.Type type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
