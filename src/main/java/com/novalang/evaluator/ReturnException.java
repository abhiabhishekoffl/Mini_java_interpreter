package com.novalang.evaluator;

public class ReturnException extends RuntimeException {
    private final Object value;

    public ReturnException(Object value) {
        super(null, null, false, false); // disables stack trace generation for efficiency
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
