package com.novalang.evaluator;

import com.novalang.parser.nodes.BlockNode;
import java.util.List;

public class NovaFunction {
    private final String name;
    private final List<String> parameters;
    private final BlockNode body;
    private final Environment closure;

    public NovaFunction(String name, List<String> parameters, BlockNode body, Environment closure) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.closure = closure;
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

    public Environment getClosure() {
        return closure;
    }

    @Override
    public String toString() {
        return "<fn " + name + ">";
    }
}
