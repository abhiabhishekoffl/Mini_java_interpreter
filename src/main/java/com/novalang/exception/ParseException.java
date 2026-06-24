package com.novalang.exception;

public class ParseException extends RuntimeException {
    private final int line;

    public ParseException(int line, String message) {
        super("Parse error at line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
