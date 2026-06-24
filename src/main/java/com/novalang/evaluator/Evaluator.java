package com.novalang.evaluator;

import com.novalang.parser.nodes.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Evaluator {
    private final StringBuilder outputBuffer = new StringBuilder();

    public String getOutput() {
        return outputBuffer.toString();
    }

    public Object evaluate(Node node, Environment env) {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Execution timed out (5s limit). Check for infinite loops.");
        }

        if (node instanceof NumberNode) {
            return ((NumberNode) node).getValue();
        }
        
        if (node instanceof StringNode) {
            return ((StringNode) node).getValue();
        }
        
        if (node instanceof BooleanNode) {
            return ((BooleanNode) node).getValue();
        }
        
        if (node instanceof VarAccessNode) {
            return env.get(((VarAccessNode) node).getName());
        }
        
        if (node instanceof VarAssignNode) {
            VarAssignNode assign = (VarAssignNode) node;
            Object val = evaluate(assign.getValueExpr(), env);
            if (assign.isDeclaration()) {
                env.define(assign.getName(), val);
            } else {
                env.assign(assign.getName(), val);
            }
            return val;
        }
        
        if (node instanceof BinaryOpNode) {
            BinaryOpNode binary = (BinaryOpNode) node;
            Object left = evaluate(binary.getLeft(), env);
            Object right = evaluate(binary.getRight(), env);
            String op = binary.getOp();

            switch (op) {
                case "+":
                    if (left instanceof String || right instanceof String) {
                        return format(left) + format(right);
                    }
                    if (left instanceof Double && right instanceof Double) {
                        return (Double) left + (Double) right;
                    }
                    throw new RuntimeException("Type mismatch: invalid operands for '+' (" + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName() + ")");
                case "-":
                    checkNumberOperands(op, left, right);
                    return (Double) left - (Double) right;
                case "*":
                    checkNumberOperands(op, left, right);
                    return (Double) left * (Double) right;
                case "/":
                    checkNumberOperands(op, left, right);
                    if ((Double) right == 0.0) {
                        throw new RuntimeException("Division by zero");
                    }
                    return (Double) left / (Double) right;
                case "%":
                    checkNumberOperands(op, left, right);
                    if ((Double) right == 0.0) {
                        throw new RuntimeException("Modulo by zero");
                    }
                    return (Double) left % (Double) right;
                case "==":
                    return Objects.equals(left, right);
                case "!=":
                    return !Objects.equals(left, right);
                case "<":
                    checkNumberOperands(op, left, right);
                    return (Double) left < (Double) right;
                case "<=":
                    checkNumberOperands(op, left, right);
                    return (Double) left <= (Double) right;
                case ">":
                    checkNumberOperands(op, left, right);
                    return (Double) left > (Double) right;
                case ">=":
                    checkNumberOperands(op, left, right);
                    return (Double) left >= (Double) right;
                case "&&":
                    checkBooleanOperands(op, left, right);
                    return (Boolean) left && (Boolean) right;
                case "||":
                    checkBooleanOperands(op, left, right);
                    return (Boolean) left || (Boolean) right;
                default:
                    throw new RuntimeException("Unknown operator: " + op);
            }
        }
        
        if (node instanceof UnaryOpNode) {
            UnaryOpNode unary = (UnaryOpNode) node;
            Object operand = evaluate(unary.getOperand(), env);
            String op = unary.getOp();
            
            switch (op) {
                case "-":
                    if (!(operand instanceof Double)) {
                        throw new RuntimeException("Type mismatch: operand for '-' must be a number.");
                    }
                    return -(Double) operand;
                case "!":
                    if (!(operand instanceof Boolean)) {
                        throw new RuntimeException("Type mismatch: operand for '!' must be a boolean.");
                    }
                    return !(Boolean) operand;
                default:
                    throw new RuntimeException("Unknown unary operator: " + op);
            }
        }
        
        if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            Object condition = evaluate(ifNode.getCondition(), env);
            if (!(condition instanceof Boolean)) {
                throw new RuntimeException("Type mismatch: condition in 'if' statement must be a boolean.");
            }
            if ((Boolean) condition) {
                return evaluate(ifNode.getThenBlock(), env);
            } else if (ifNode.getElseBlock() != null) {
                return evaluate(ifNode.getElseBlock(), env);
            }
            return null;
        }
        
        if (node instanceof WhileNode) {
            WhileNode whileNode = (WhileNode) node;
            while (true) {
                Object condition = evaluate(whileNode.getCondition(), env);
                if (!(condition instanceof Boolean)) {
                    throw new RuntimeException("Type mismatch: condition in 'while' statement must be a boolean.");
                }
                if (!(Boolean) condition) {
                    break;
                }
                evaluate(whileNode.getBody(), env);
                if (Thread.currentThread().isInterrupted()) {
                    throw new RuntimeException("Execution timed out (5s limit). Check for infinite loops.");
                }
            }
            return null;
        }
        
        if (node instanceof PrintNode) {
            PrintNode printNode = (PrintNode) node;
            Object value = evaluate(printNode.getExpr(), env);
            outputBuffer.append(format(value)).append("\n");
            return value;
        }
        
        if (node instanceof BlockNode) {
            BlockNode blockNode = (BlockNode) node;
            Environment blockEnv = new Environment(env);
            Object lastResult = null;
            for (Node statement : blockNode.getStatements()) {
                lastResult = evaluate(statement, blockEnv);
            }
            return lastResult;
        }
        
        if (node instanceof FunctionDefNode) {
            FunctionDefNode def = (FunctionDefNode) node;
            NovaFunction function = new NovaFunction(def.getName(), def.getParameters(), def.getBody(), env);
            env.define(def.getName(), function);
            return function;
        }
        
        if (node instanceof FunctionCallNode) {
            FunctionCallNode call = (FunctionCallNode) node;
            Object functionObj = env.get(call.getName());
            if (!(functionObj instanceof NovaFunction)) {
                throw new RuntimeException("'" + call.getName() + "' is not a function.");
            }
            NovaFunction function = (NovaFunction) functionObj;
            List<Node> arguments = call.getArguments();
            
            if (arguments.size() != function.getParameters().size()) {
                throw new RuntimeException("Function '" + function.getName() + "' expected " +
                        function.getParameters().size() + " arguments but got " + arguments.size());
            }
            
            List<Object> evaluatedArgs = new ArrayList<>();
            for (Node arg : arguments) {
                evaluatedArgs.add(evaluate(arg, env));
            }
            
            Environment closureEnv = new Environment(function.getClosure());
            for (int i = 0; i < function.getParameters().size(); i++) {
                closureEnv.define(function.getParameters().get(i), evaluatedArgs.get(i));
            }
            
            try {
                return evaluate(function.getBody(), closureEnv);
            } catch (ReturnException returnException) {
                return returnException.getValue();
            }
        }
        
        if (node instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode) node;
            Object returnValue = null;
            if (returnNode.getExpr() != null) {
                returnValue = evaluate(returnNode.getExpr(), env);
            }
            throw new ReturnException(returnValue);
        }
        
        throw new RuntimeException("Unknown AST node type: " + node.getClass().getSimpleName());
    }

    private void checkNumberOperands(String op, Object left, Object right) {
        if (!(left instanceof Double && right instanceof Double)) {
            throw new RuntimeException("Type mismatch: operands for '" + op + "' must be numbers.");
        }
    }

    private void checkBooleanOperands(String op, Object left, Object right) {
        if (!(left instanceof Boolean && right instanceof Boolean)) {
            throw new RuntimeException("Type mismatch: operands for '" + op + "' must be booleans.");
        }
    }

    private String format(Object value) {
        if (value == null) return "null";
        if (value instanceof Double) {
            return String.valueOf(value);
        }
        return value.toString();
    }
}
