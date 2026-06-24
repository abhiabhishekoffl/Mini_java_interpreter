package com.novalang.exception;

public class LexerException extends RuntimeException {
    private final int line;

    public LexerException(int line, String message) {
        super("Lexer error at line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
