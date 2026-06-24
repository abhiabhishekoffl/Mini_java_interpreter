package com.novalang.parser.nodes;

public class VarAssignNode extends Node {
    private final String name;
    private final Node valueExpr;
    private final boolean isDeclaration;

    public VarAssignNode(String name, Node valueExpr, boolean isDeclaration) {
        this.name = name;
        this.valueExpr = valueExpr;
        this.isDeclaration = isDeclaration;
    }

    public String getName() {
        return name;
    }

    public Node getValueExpr() {
        return valueExpr;
    }

    public boolean isDeclaration() {
        return isDeclaration;
    }

    @Override
    public String toString() {
        return (isDeclaration ? "let " : "") + name + " = " + valueExpr;
    }
}
