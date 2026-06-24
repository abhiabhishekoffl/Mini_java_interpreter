package com.novalang.parser.nodes;

import java.util.List;

public class FunctionDefNode extends Node {
    private final String name;
    private final List<String> parameters;
    private final BlockNode body;

    public FunctionDefNode(String name, List<String> parameters, BlockNode body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "fn " + name + "(" + String.join(", ", parameters) + ") " + body;
    }
}
