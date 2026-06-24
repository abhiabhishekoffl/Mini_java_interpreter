package com.novalang.controller;

import com.novalang.model.ExecuteRequest;
import com.novalang.model.ExecuteResponse;
import com.novalang.service.NovaLangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExecuteController {

    private final NovaLangService novaLangService;

    @Autowired
    public ExecuteController(NovaLangService novaLangService) {
        this.novaLangService = novaLangService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecuteResponse> execute(@RequestBody ExecuteRequest request) {
        ExecuteResponse response = novaLangService.execute(request.getCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ExecuteResponse> validate(@RequestBody ExecuteRequest request) {
        ExecuteResponse response = novaLangService.validate(request.getCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/examples")
    public ResponseEntity<List<Map<String, String>>> getExamples() {
        List<Map<String, String>> examples = new ArrayList<>();

        examples.add(createExample("Hello World", "print \"Hello, World!\";"));
        
        examples.add(createExample("Variables & Math", 
            "let x = 10;\n" +
            "let y = 20;\n" +
            "let sum = x + y;\n" +
            "print \"Sum of x and y is: \" + sum;"
        ));

        examples.add(createExample("FizzBuzz",
            "# FizzBuzz program in NovaLang\n" +
            "let i = 1;\n" +
            "while i <= 20 {\n" +
            "  if i % 15 == 0 {\n" +
            "    print \"FizzBuzz\";\n" +
            "  } else {\n" +
            "    if i % 3 == 0 {\n" +
            "      print \"Fizz\";\n" +
            "    } else {\n" +
            "      if i % 5 == 0 {\n" +
            "        print \"Buzz\";\n" +
            "      } else {\n" +
            "        print i;\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  i = i + 1;\n" +
            "}"
        ));

        examples.add(createExample("Fibonacci",
            "# Recursive Fibonacci\n" +
            "fn fib(n) {\n" +
            "  if n <= 1 {\n" +
            "    return n;\n" +
            "  }\n" +
            "  return fib(n - 1) + fib(n - 2);\n" +
            "}\n" +
            "print \"Fibonacci of 7:\";\n" +
            "print fib(7);"
        ));

        examples.add(createExample("Factorial",
            "# Recursive Factorial\n" +
            "fn fact(n) {\n" +
            "  if n <= 1 {\n" +
            "    return 1;\n" +
            "  }\n" +
            "  return n * fact(n - 1);\n" +
            "}\n" +
            "print \"Factorial of 5:\";\n" +
            "print fact(5);"
        ));

        examples.add(createExample("Calculator",
            "# Multi-operation calculator\n" +
            "fn calculate(op, a, b) {\n" +
            "  if op == \"+\" { return a + b; }\n" +
            "  if op == \"-\" { return a - b; }\n" +
            "  if op == \"*\" { return a * b; }\n" +
            "  if op == \"/\" {\n" +
            "    if b == 0 {\n" +
            "      print \"Error: Division by zero!\";\n" +
            "      return 0;\n" +
            "    }\n" +
            "    return a / b;\n" +
            "  }\n" +
            "  print \"Unknown operation!\";\n" +
            "  return 0;\n" +
            "}\n" +
            "print \"10 / 2 = \" + calculate(\"/\", 10, 2);\n" +
            "print \"5 * 3 = \" + calculate(\"*\", 5, 3);"
        ));

        return ResponseEntity.ok(examples);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(status);
    }

    private Map<String, String> createExample(String name, String code) {
        Map<String, String> ex = new HashMap<>();
        ex.put("name", name);
        ex.put("code", code);
        return ex;
    }
}
