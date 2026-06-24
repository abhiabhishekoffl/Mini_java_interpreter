package com.novalang;

import com.novalang.evaluator.Environment;
import com.novalang.evaluator.Evaluator;
import com.novalang.exception.LexerException;
import com.novalang.exception.ParseException;
import com.novalang.lexer.Lexer;
import com.novalang.lexer.Token;
import com.novalang.parser.Parser;
import com.novalang.parser.nodes.Node;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NovaLangServiceTest {

    @Test
    public void testLexerHappyPath() {
        Lexer lexer = new Lexer("let x = 10; # comment\nprint x;");
        List<Token> tokens = lexer.scanTokens();
        
        // Exclude EOF for simple token matching
        assertTrue(tokens.size() > 5);
        assertEquals(Token.Type.LET, tokens.get(0).getType());
        assertEquals(Token.Type.IDENTIFIER, tokens.get(1).getType());
        assertEquals("x", tokens.get(1).getLexeme());
        assertEquals(Token.Type.EQUAL, tokens.get(2).getType());
        assertEquals(Token.Type.NUMBER, tokens.get(3).getType());
        assertEquals(10.0, tokens.get(3).getLiteral());
    }

    @Test
    public void testLexerInvalidCharacter() {
        Lexer lexer = new Lexer("let x = @;");
        assertThrows(LexerException.class, () -> lexer.scanTokens());
    }

    @Test
    public void testParserHappyPath() {
        Lexer lexer = new Lexer(
            "let x = 10;\n" +
            "if x > 5 {\n" +
            "  x = 20;\n" +
            "}\n" +
            "while x > 0 {\n" +
            "  x = x - 1;\n" +
            "}\n" +
            "fn add(a, b) {\n" +
            "  return a + b;\n" +
            "}\n" +
            "add(1, 2);"
        );
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        List<Node> statements = parser.parse();
        assertEquals(5, statements.size()); // let, if, while, fn, call
    }

    @Test
    public void testParserMalformedSyntax() {
        Lexer lexer = new Lexer("let x = ;");
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        assertThrows(ParseException.class, () -> parser.parse());
    }

    @Test
    public void testEvaluatorArithmeticAndStringConcat() {
        Environment env = new Environment();
        Evaluator evaluator = new Evaluator();

        // 1. Math: 10 + 5 * 2
        Lexer lexer = new Lexer("let result = 10 + 5 * 2;");
        List<Node> statements = new Parser(lexer.scanTokens()).parse();
        evaluator.evaluate(statements.get(0), env);
        assertEquals(20.0, env.get("result"));

        // 2. Concat: "Hello " + "World"
        lexer = new Lexer("let msg = \"Hello \" + \"World\";");
        statements = new Parser(lexer.scanTokens()).parse();
        evaluator.evaluate(statements.get(0), env);
        assertEquals("Hello World", env.get("msg"));
    }

    @Test
    public void testEvaluatorVariableScope() {
        Environment env = new Environment();
        Evaluator evaluator = new Evaluator();

        // Outer let x = 10
        Lexer lexer = new Lexer(
            "let x = 10;\n" +
            "{\n" +
            "  let x = 20;\n" +
            "  let y = 30;\n" +
            "}\n" +
            "let z = x;"
        );
        List<Node> statements = new Parser(lexer.scanTokens()).parse();
        for (Node stmt : statements) {
            evaluator.evaluate(stmt, env);
        }

        assertEquals(10.0, env.get("x"));
        assertEquals(10.0, env.get("z"));
        assertThrows(RuntimeException.class, () -> env.get("y"));
    }

    @Test
    public void testEvaluatorRecursion() {
        Environment env = new Environment();
        Evaluator evaluator = new Evaluator();

        // Factorial function
        Lexer lexer = new Lexer(
            "fn fact(n) {\n" +
            "  if n <= 1 { return 1; }\n" +
            "  return n * fact(n - 1);\n" +
            "}\n" +
            "let res = fact(5);"
        );
        List<Node> statements = new Parser(lexer.scanTokens()).parse();
        for (Node stmt : statements) {
            evaluator.evaluate(stmt, env);
        }

        assertEquals(120.0, env.get("res"));
    }

    @Test
    public void testEvaluatorUndefinedVariable() {
        Environment env = new Environment();
        Evaluator evaluator = new Evaluator();
        Lexer lexer = new Lexer("print y;");
        List<Node> statements = new Parser(lexer.scanTokens()).parse();
        assertThrows(RuntimeException.class, () -> evaluator.evaluate(statements.get(0), env));
    }
}
