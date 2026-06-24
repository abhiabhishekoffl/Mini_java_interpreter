# NovaLang Technical Language Specification

This document details the formal syntax and behavior of the **NovaLang** programming language.

## 1. Data Types
NovaLang is dynamically typed and supports three primitive types:
- **Number**: Internally represented as a Java double-precision 64-bit float (e.g. `10`, `3.14`, `-0.5`).
- **String**: Sequences of characters wrapped in double quotes (e.g., `"Hello, World!"`). Supports basic escape characters (`\n`, `\t`, `\"`, `\\`).
- **Boolean**: Logical values `true` and `false`.

## 2. Variables
Variables must be declared before assignment using the `let` keyword. 
- **Declaration**: `let name = value;`
- **Reassignment**: `name = newValue;`

## 3. Operators

### Arithmetic
- Addition/String Concatenation: `+`
  - If either operand is a String, converts both to String and concatenates.
  - If both are Numbers, performs addition.
- Subtraction: `-` (also supports unary negation `-x`)
- Multiplication: `*`
- Division: `/` (yields division by zero runtime error if b == 0)
- Modulo: `%` (yields modulo by zero runtime error if b == 0)

### Comparison
- Equal: `==`
- Not Equal: `!=`
- Less Than: `<` (Numbers only)
- Less Than or Equal: `<=` (Numbers only)
- Greater Than: `>` (Numbers only)
- Greater Than or Equal: `>=` (Numbers only)

### Logical
- AND: `&&`
- OR: `||`
- NOT: `!`

## 4. Control Flow

### If-Else Statements
Executes conditional blocks. Condition parentheses are optional.
```
if (x > 10) {
    print "Greater";
} else {
    print "Lesser";
}
```

### While Loops
Executes loop blocks while the condition is true.
```
let i = 0;
while i < 5 {
    print i;
    i = i + 1;
}
```

## 5. Functions
Functions are declared using the `fn` keyword, and return values using `return`.
- **Definition**:
  ```
  fn add(a, b) {
      return a + b;
  }
  ```
- **Invocation**:
  ```
  let sum = add(5, 10);
  ```

## 6. Input/Output
- **Print**: `print <expr>;` (captures printed statements to standard output).

## 7. Comments
Comments start with a `#` character and extend to the end of the line.
```
# This is a comment
let x = 10; # inline comment
```
