package com.novalang.parser.nodes;

public class BooleanNode extends Node {
    private final Boolean value;

    public BooleanNode(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
