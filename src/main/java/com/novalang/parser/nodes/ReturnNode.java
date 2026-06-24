package com.novalang.parser.nodes;

public class ReturnNode extends Node {
    private final Node expr;

    public ReturnNode(Node expr) {
        this.expr = expr;
    }

    public Node getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return "return" + (expr != null ? " " + expr : "") + ";";
    }
}
