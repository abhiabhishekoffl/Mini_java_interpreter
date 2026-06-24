package com.novalang.parser.nodes;

public class WhileNode extends Node {
    private final Node condition;
    private final BlockNode body;

    public WhileNode(Node condition, BlockNode body) {
        this.condition = condition;
        this.body = body;
    }

    public Node getCondition() {
        return condition;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "while " + condition + " " + body;
    }
}
