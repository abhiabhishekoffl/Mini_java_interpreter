package com.novalang.parser.nodes;

public class IfNode extends Node {
    private final Node condition;
    private final BlockNode thenBlock;
    private final Node elseBlock; // Can be BlockNode, IfNode, or null

    public IfNode(Node condition, BlockNode thenBlock, Node elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public Node getCondition() {
        return condition;
    }

    public BlockNode getThenBlock() {
        return thenBlock;
    }

    public Node getElseBlock() {
        return elseBlock;
    }

    @Override
    public String toString() {
        return "if " + condition + " " + thenBlock + (elseBlock != null ? " else " + elseBlock : "");
    }
}
