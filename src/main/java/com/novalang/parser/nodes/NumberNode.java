package com.novalang.parser.nodes;

public class NumberNode extends Node {
    private final Double value;

    public NumberNode(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
