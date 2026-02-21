# Sift: A Fluent Regex Builder for Java

[![sift-core](https://img.shields.io/maven-central/v/com.mirkoddd/sift-core?label=sift-core)](https://central.sonatype.com/artifact/com.mirkoddd/sift-core)
[![sift-annotations](https://img.shields.io/maven-central/v/com.mirkoddd/sift-annotations?label=sift-annotations)](https://central.sonatype.com/artifact/com.mirkoddd/sift-annotations)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Tests](https://github.com/mirkoddd/Sift/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mirkoddd/Sift/actions)
[![Coverage](https://raw.githubusercontent.com/mirkoddd/Sift/main/.github/badges/jacoco.svg)](https://github.com/mirkoddd/Sift/actions)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

**Sift** is a lightweight, zero-allocation, type-safe Java library designed to construct complex Regular Expressions through a readable, object-oriented API. It eliminates the maintenance burden of cryptic string-based regex by applying SOLID principles to pattern construction.

*(And yes, the core engine boasts **100% Test Coverage**).*

## Rationale

Regular expressions are powerful but often become "write-only" code. Sift transforms regex creation into a structured coding process, ensuring that patterns are:

* **Readable:** The syntax strictly mirrors natural English prose, making the intent of the pattern obvious.
* **Type-Safe:** A Type-State machine (`QuantifierStep` -> `TypeStep` -> `ConnectorStep`) enforces grammatical correctness at compile-time, preventing malformed regex strings.
* **Composable:** Complex patterns are built by combining smaller, reusable `SiftPattern` objects.
* **Extensible:** Adheres strictly to the Open/Closed Principle, allowing you to define custom domain grammars without modifying the core library.

---

## Installation

Sift is modularized to keep your dependency graph clean.

### Gradle

```groovy
dependencies {
   // Replace <latest-version> with the version shown in the Maven Central badge above
   
    // Core Engine: Fluent API for Regex generation (Zero external dependencies)
    implementation 'com.mirkoddd:sift-core:<latest-version>'
    
    // Optional: Integration with Jakarta Validation / Hibernate Validator
    implementation 'com.mirkoddd:sift-annotations:<latest-version>'
}
```

## Quick Start

### Basic Validation

To strictly validate an entire string (e.g., a simple Username rule), use `Sift.fromStart()` and `untilEnd()`:

```java
import static com.mirkoddd.sift.Sift.*;

String regex = fromStart()
    .letters()
    .then() // Transitions to the next logical token
    .atLeast(3).alphanumeric()
    .untilEnd()
    .shake();

// Result: ^[a-zA-Z][a-zA-Z0-9]{3,}$
```

### Searching & Extracting Data
If you need to find a pattern inside a larger text (like scanning logs or prices), use `Sift.anywhere()`:

```java
import static com.mirkoddd.sift.Sift.*;
import static com.mirkoddd.sift.SiftPatterns.*;

String priceRegex = anywhere()
    .followedBy(literal("Cost: $"))
    .then().oneOrMore().digits()
    .then().optional().pattern(  // Easily apply quantifiers to nested sub-patterns
        anywhere().character('.').then().exactly(2).digits()
    )
    .shake();

// Result: Cost: \$[0-9]+(?:\.[0-9]{2})?
```

## API Overview

### 1. Entry Points (`Sift`)
* `fromStart()`: Anchors the regex to the beginning of the string (`^`).
* `anywhere()`: Creates a free-floating pattern.
* `wordBoundary()`: Anchors to a word boundary (`\b`).

### 2. Quantifiers (`QuantifierStep`)
Define *how many times* an element should occur. These are always followed by a `TypeStep`:
* `.exactly(n)`
* `.atLeast(n)`
* `.oneOrMore()`
* `.zeroOrMore()`
* `.optional()`

### 3. Types (`TypeStep`)
Define *what* to match (Nouns):
* `.digits()`: `[0-9]`
* `.letters()`: `[a-zA-Z]`
* `.lettersLowercaseOnly()`: `[a-z]`
* `.lettersUppercaseOnly()`: `[A-Z]`
* `.alphanumeric()`: `[a-zA-Z0-9]`
* `.any()`: `.` (Dot)
* `.character(char)`: Matches a single literal character safely.
* `.pattern(SiftPattern)`: Matches a complex sub-pattern.

### 4. Connectors & Refinements (`ConnectorStep`)
Link your tokens together or modify existing character classes:
* `.then()`: **State transition.** Returns to the `QuantifierStep` to begin defining a *new* token.
* `.followedBy(char)`: **Shortcut.** Appends a single literal character immediately.
* `.followedBy(SiftPattern... patterns)`: **Varargs Shortcut.** Appends one or more complex sub-patterns in sequence without breaking the flow.
* `.including(char...)` / `.excluding(char...)`: Modifies the preceding character class using logical intersection/subtraction (e.g., `[a-z&&[^aeiou]]+`).

### 5. Logic and Groups (`SiftPatterns`)
Use static imports from `SiftPatterns` for advanced composition:
* `anyOf(SiftPattern...)`: Logical OR `(?:A|B)`.
* `capture(SiftPattern)`: Anonymous capturing group `(...)`.
* `capture(String, SiftPattern)`: Named capturing group `(?<name>...)`.
* `literal(String)`: Safely escapes plain text.

## Jakarta Validation (`sift-annotations`)
Sift natively integrates with the Jakarta Validation API (JSR-380). This prevents regex duplication across your codebase by allowing you to define rules as reusable providers.

**1. Define a Rule**
Implement the `SiftRegexProvider` interface:

```java
public class NumericPinRule implements SiftRegexProvider {
    @Override
    public String getRegex() {
        return Sift.fromStart()
            .exactly(5).digits()
            .untilEnd()
            .shake();
    }
}
```

**2. Annotate your DTO**
Use `@SiftMatch` directly on your fields. The validator engine will compile the pattern automatically.

```java
import com.mirkoddd.sift.SiftMatch;

public class RegistrationDto {
    @SiftMatch(
        value = NumericPinRule.class,
        message = "PIN must be exactly 5 digits"
    )
    private String pinCode;
}
```

## Open/Closed Principle
Sift is designed to be extended. You don't need to modify the library to add new domain-specific grammar. Just create methods that return a `SiftPattern` (a functional interface with a single `.shake()` method) and inject them effortlessly.

```java
// 1. Create your custom extension
SiftPattern datePattern = anywhere()
    .exactly(4).digits().followedBy('-')
    .then().exactly(2).digits().followedBy('-')
    .then().exactly(2).digits();

// 2. Use it in the fluent chain (leveraging Varargs for extreme readability!)
String logRegex = fromStart()
    .followedBy(
        literal("[INFO] "), 
        datePattern
    )
    .untilEnd()
    .shake();
```

## License
This project is licensed under the Apache License, Version 2.0. See the LICENSE file for details.