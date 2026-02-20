# Sift: A Fluent Regex Builder for Java

[![Maven Central](https://img.shields.io/maven-central/v/com.mirkoddd/sift-core)](https://central.sonatype.com/artifact/com.mirkoddd/sift-core)
[![Maven Central](https://img.shields.io/maven-central/v/com.mirkoddd/sift-annotations)](https://central.sonatype.com/artifact/com.mirkoddd/sift-annotations)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Tests](https://github.com/mirkoddd/Sift/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mirkoddd/Sift/actions)[![Coverage](https://raw.githubusercontent.com/mirkoddd/Sift/main/.github/badges/jacoco.svg)](https://github.com/mirkoddd/Sift/actions)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

**Sift** is a lightweight, type-safe Java library designed to construct complex Regular Expressions through a readable, object-oriented API. It eliminates the maintenance burden of cryptic string-based regex by applying SOLID principles to pattern construction.

## Rationale

Regular expressions are powerful but often become "write-only" code. Sift transforms regex creation into a structured coding process, ensuring that patterns are:

* **Readable:** The syntax mirrors natural language, making the intent of the pattern obvious.
* **Type-Safe:** A Type-State machine (`QuantifierStep` -> `TypeStep` -> `ConnectorStep`) enforces grammatical correctness at compile-time.
* **Composable:** Complex patterns are built by combining smaller, reusable `SiftPattern` objects.
* **Extensible:** Adheres strictly to the Open/Closed Principle, allowing you to define custom domain grammars without modifying the core library.

---

## Installation

Sift is modularized to keep your dependency graph clean.

### Gradle

```groovy
dependencies {
    // Core Engine: Fluent API for Regex generation (Zero external dependencies)
    implementation 'com.mirkoddd:sift-core:1.1.0'
    
    // Optional: Integration with Jakarta Validation / Hibernate Validator
    implementation 'com.mirkoddd:sift-annotations:1.1.0'
}
```

## Quick Start

### Basic Validation

To strictly validate an entire string (e.g., a simple Username rule), use `Sift.fromStart()` and `untilEnd()`:

```Java

import static com.mirkoddd.sift.Sift.*;

String regex = fromStart()
    .letters()
    .followedBy()
    .atLeast(3).alphanumeric()
    .untilEnd()
    .shake();

// Result: ^[a-zA-Z][a-zA-Z0-9]{3,}$
```

### Searching & Extracting Data
If you need to find a pattern inside a larger text (like scanning logs), use `Sift.anywhere()`:

```Java
import static com.mirkoddd.sift.Sift.*;
import static com.mirkoddd.sift.SiftPatterns.*;

String priceRegex = anywhere()
    .followedBy(literal("Cost: $"))
    .followedBy().oneOrMore().digits()
    .withOptional(
        anywhere().followedBy('.').followedBy().exactly(2).digits()
    )
    .shake();

// Result: Cost: \$[0-9]+(?:(?:\\.[0-9]{2}))?
```

## API Overview

1. Entry Points (`Sift`)

* `fromStart()`: Anchors the regex to the beginning of the string (`^`).

* `anywhere()`: Creates a free-floating pattern.

* `wordBoundary()`: Anchors to a word boundary (`\b`).

2. Quantifiers (`QuantifierStep`)\
   \
   Define how many times an element should occur:

* `.exactly(n)`

* `.atLeast(n)`

* `.oneOrMore()`

* `.zeroOrMore()`

* `.optional()`

* `.withOptional(char | SiftPattern)`: Syntactic sugar to make an entire block or character optional.


3. Character Types (`TypeStep`)\
   \
   Define what to match:

* `.digits()`: `[0-9]`

* `.letters()`: `[a-zA-Z]`

* `.lettersLowercaseOnly()`: `[a-z]`

* `.lettersUppercaseOnly()`: `[A-Z]`

* `.alphanumeric()`: `[a-zA-Z0-9]`

* `.any()`: `.` (Dot)

* `.followedBy(char | SiftPattern)`: Match literals or complex sub-patterns.


4. Refinements (`ConnectorStep`)\
   \
   Modify existing character classes using logical intersection/subtraction:

```Java
// Lowercase letters excluding vowels
char[] vowels = {'a', 'e', 'i', 'o', 'u'};
String regex = anywhere()
    .oneOrMore().lettersLowercaseOnly()
    .excluding(vowels)
    .shake();

// Result: [a-z&&[^aeiou]]+
```

5. Logic and Groups (`SiftPatterns`)\
   \
   Use static imports from `SiftPatterns` for advanced composition:

* `anyOf(SiftPattern...)`: Logical OR `(?:A|B)`.

* `capture(SiftPattern)`: Anonymous capturing group `(...)`.

* `capture(String, SiftPattern)`: Named capturing group `(?<name>...)`.

* `literal(String)`: Safely escapes plain text.

## Jakarta Validation (`sift-annotations`)
Sift natively integrates with the Jakarta Validation API (JSR-380). This prevents regex duplication across your codebase by allowing you to define rules as reusable providers.

1. Define a Rule\
   \
   Implement the `SiftRegexProvider` interface:

```Java

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

2. Annotate your DTO\
   \
   Use `@SiftMatch` directly on your fields. The validator engine will compile the pattern automatically.

```Java

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
Sift is designed to be extended. You don't need to modify the library to add new domain-specific grammar. Just create methods that return a `SiftPattern` (a functional interface with a single `.shake()` method) and inject them using `.followedBy()` or `.withOptional()`.

```Java

// 1. Create your custom extension
SiftPattern datePattern = Sift.anywhere()
    .exactly(4).digits().followedBy('-')
    .followedBy().exactly(2).digits().followedBy('-')
    .followedBy().exactly(2).digits();

// 2. Use it in the fluent chain
String logRegex = Sift.fromStart()
    .followedBy(SiftPatterns.literal("[INFO] "))
    .followedBy(datePattern)
    .untilEnd()
    .shake();
```

## License
This project is licensed under the Apache License, Version 2.0. See the LICENSE file for details