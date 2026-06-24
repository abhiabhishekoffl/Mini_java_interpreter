package com.novalang.parser.nodes;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionCallNode extends Node {
    private final String name;
    private final List<Node> arguments;

    public FunctionCallNode(String name, List<Node> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<Node> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        String args = arguments.stream().map(Node::toString).collect(Collectors.joining(", "));
        return name + "(" + args + ")";
    }
}
