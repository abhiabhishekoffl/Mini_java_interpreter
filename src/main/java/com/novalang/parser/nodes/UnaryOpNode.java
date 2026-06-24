package com.novalang.parser.nodes;

public class UnaryOpNode extends Node {
    private final String op;
    private final Node operand;

    public UnaryOpNode(String op, Node operand) {
        this.op = op;
        this.operand = operand;
    }

    public String getOp() {
        return op;
    }

    public Node getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return "(" + op + operand + ")";
    }
}
