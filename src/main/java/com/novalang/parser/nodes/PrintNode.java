package com.novalang.parser.nodes;

public class PrintNode extends Node {
    private final Node expr;

    public PrintNode(Node expr) {
        this.expr = expr;
    }

    public Node getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return "print " + expr + ";";
    }
}
