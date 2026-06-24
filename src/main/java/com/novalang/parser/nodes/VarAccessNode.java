package com.novalang.parser.nodes;

public class VarAccessNode extends Node {
    private final String name;

    public VarAccessNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
