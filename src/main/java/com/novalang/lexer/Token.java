package com.novalang.lexer;

public class Token {
    public enum Type {
        // Keywords
        LET, TRUE, FALSE, IF, ELSE, WHILE, FN, RETURN, PRINT,
        
        // Literals
        IDENTIFIER, NUMBER, STRING,
        
        // Operators
        PLUS, MINUS, STAR, SLASH, PERCENT,
        EQ_EQ, BANG_EQ, LESS_EQ, GREATER_EQ, LESS, GREATER,
        BANG, AMP_AMP, PIPE_PIPE, EQUAL,
        
        // Punctuation
        SEMICOLON, COMMA, LPAR, RPAR, LBRACE, RBRACE,
        
        EOF
    }

    private final Type type;
    private final String lexeme;
    private final Object literal;
    private final int line;

    public Token(Type type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public Type getType() { return type; }
    public String getLexeme() { return lexeme; }
    public Object getLiteral() { return literal; }
    public int getLine() { return line; }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal + " (line " + line + ")";
    }
}
