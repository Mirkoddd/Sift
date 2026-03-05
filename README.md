# Sift
<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=e931dfa9-02e9-406d-bde7-56f9e0000464"  alt=""/>[![sift-core](https://img.shields.io/maven-central/v/com.mirkoddd/sift-core?label=sift-core)](https://central.sonatype.com/artifact/com.mirkoddd/sift-core)
[![sift-annotations](https://img.shields.io/maven-central/v/com.mirkoddd/sift-annotations?label=sift-annotations)](https://central.sonatype.com/artifact/com.mirkoddd/sift-annotations)
[![Java 8+](https://img.shields.io/badge/Java-8+-blue.svg)](https://adoptium.net/) [![Tests](https://github.com/mirkoddd/Sift/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mirkoddd/Sift/actions)
[![Coverage](https://raw.githubusercontent.com/mirkoddd/Sift/main/.github/badges/jacoco.svg)](https://github.com/mirkoddd/Sift/actions)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![sift-core](https://javadoc.io/badge2/com.mirkoddd/sift-core/javadoc_sift--core.svg)](https://javadoc.io/doc/com.mirkoddd/sift-core)
[![sift-annotations](https://javadoc.io/badge2/com.mirkoddd/sift-annotations/javadoc_sift--annotations.svg)](https://javadoc.io/doc/com.mirkoddd/sift-annotations)

**The Type-Safe, SOLID, and Secure Regular Expression Builder for Java.**

Writing raw Regular Expressions in Java is error-prone, hard to read, and difficult to maintain. Sift solves this by providing a fluent, state-machine-driven Domain Specific Language (DSL) that guarantees valid syntax at compile time.

Build your rules step-by-step, filter out the noise, and when your pattern is ready, just `.shake()` the sieve!

---

## 📖 The Sift Cookbook
Looking for real-world examples? Check out the **[Sift Cookbook](COOKBOOK.md)**!
It contains advanced recipes demonstrating Sift's true power: parsing TSV logs, validating UUIDs/IPs, data extraction with named captures, lookarounds, and ReDoS mitigation techniques.

---

# Quick Start

Add Sift to your project dependencies:

**Maven:**

```XML
<dependency>
    <groupId>com.mirkoddd</groupId>
    <artifactId>sift-core</artifactId>
    <version>latest-version</version>
</dependency>
<dependency>
    <groupId>com.mirkoddd</groupId>
    <artifactId>sift-annotations</artifactId>
    <version>latest-version</version>
</dependency>
```    

**Gradle:**

```Groovy
// Replace <latest-version> with the version shown in the Maven Central badge above

// Core Engine: Fluent API for Regex generation (Zero external dependencies)
implementation 'com.mirkoddd:sift-core:<latest-version>'

// Optional: Integration with Jakarta Validation / Hibernate Validator
implementation 'com.mirkoddd:sift-annotations:<latest-version>'
 ```   
## Compatibility

Sift is compiled targeting **Java 8 bytecode** for maximum compatibility across all environments, including legacy Spring Boot 2.x servers and Android devices.

However, the internal codebase and test suite utilize modern **Java 17** features. If you plan to clone the repository and build it from source, ensure you have JDK 17 or higher installed.

# Key Features

* **Compile-Time Structural Guarantees:** Sift is not just fluent — it enforces a strict Type-State machine. Invalid regex constructions are impossible to express in the DSL. Entire classes of runtime errors are eliminated before your code even compiles.
* **Modular "LEGO Brick" Composition:** Build unanchored, reusable intermediate patterns using `Sift.fromAnywhere()` and seamlessly compose them into larger, strict boundaries.
* **Immutable & Thread-Safe Builders:** Every Sift pattern is built in a controlled, predictable flow using copy-on-write states and double-checked locking. No hidden shared state, no side effects. Safe to use in concurrent environments.
* **Advanced Regex & Lazy Evaluation:** Sift fully supports advanced engine features like Named Capturing Groups, Lookarounds, and Backreferences. Sift uniquely features **Lazy Validation** for backreferences, allowing you to compose separated blocks and cross-reference them safely before final assembly.
* **100% Native Regex Output:** Sift generates pure Java-compatible regular expressions. No wrappers, no runtime interpreters, no performance penalties. Call `.sieve()` to get a compiled `java.util.regex.Pattern`.
* **Semantic Over Symbolic:** Express intent using meaningful method names instead of cryptic symbols. Write `.atLeast(3).digits()` instead of `{3,}[0-9]` and make your code self-documenting.
* **Self-Contained Global Flags:** Apply Case-Insensitive, Multiline, or DotAll modes effortlessly via `Sift.filteringWith(...)`. Sift uses inline flags (e.g., `(?i)`), making the resulting string 100% portable across databases or JSON payloads.
* **ASCII by Default, Global Ready:** Standard methods like `letters()` or `digits()` default strictly to ASCII (`[a-zA-Z]`, `[0-9]`), preventing unexpected matches with foreign alphabets. Need internationalization? Switch explicitly to the `...Unicode()` counterparts.
* **ReDoS Mitigation Tools:** Sift helps you write safer patterns without memorizing obscure syntax by exposing possessive quantifiers via `.withoutBacktracking()` and lazy modifiers via `.asFewAsPossible()`.
* **Zero Dependencies:** The `sift-core` engine is pure Java. It doesn't pull in any bloated transitive dependencies, keeping your final artifact size incredibly small.

# Usage Examples

### 1. Fluent, Type-Safe Regex Generation

Forget about counting backslashes or memorizing obscure symbols. Sift guides your hand using your IDE's auto-completion.

```Java    
// Goal: Match an international username securely
String regex = Sift.fromStart()
    .exactly(1).uppercaseLettersUnicode() // Must start with an uppercase letter
    .then()
    .between(3, 15).wordCharactersUnicode().withoutBacktracking() // Secure against ReDoS
    .then()
    .optional().digits() // May end with an ASCII number
    .andNothingElse()
    .shake(); 

// Result: ^[\p{Lu}][\p{L}\p{Nd}_]{3,15}+[0-9]?$
```

### 2. Modular Composition (The LEGO Brick Approach)

Break down complex patterns into highly readable, reusable semantic variables.

```Java
// Define the basic building blocks using fromAnywhere().
// This ensures these blocks don't carry a '^' anchor, allowing them
// to be safely placed in the middle or end of our final chain.
var anywhere = Sift.fromAnywhere();
var hex8 = anywhere.exactly(8).hexDigits();
var hex4 = anywhere.exactly(4).hexDigits();
var hex12 = anywhere.exactly(12).hexDigits();
var separator = anywhere.character('-');

// Compose reusable intermediate blocks
var hex4andSeparator = hex4.followedBy(separator);

// Let's define the actual regex:
String actualUuidRegex = hex8
        .followedBy(
                separator,
                hex4andSeparator,
                hex4andSeparator,
                hex4andSeparator,
                hex12)
        .shake();


// Result: [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}
// Matches: 123e4567-e89b-12d3-a456-426614174000
// From UUID Validator test in SiftCookbookTest.java
```

### 3. Seamless Jakarta Validation

Stop duplicating regex logic in your DTOs. Centralize your rules and reuse them elegantly with `@SiftMatch`.

```Java
// 1. Define your reusable rule (e.g., matching "PROMO123")
public class PromoCodeRule implements SiftRegexProvider {
    public String getRegex() {
        return Sift.fromStart()
                .atLeast(4).letters()
                .then()
                .exactly(3).digits()
                .andNothingElse()
                .shake();
    }
}

// 2. Apply it to your models with Type-Safe Flags
public record ApplyPromoRequest(
        @SiftMatch(
                value = PromoCodeRule.class,
                flags = {SiftMatchFlag.CASE_INSENSITIVE}, // Allows "promo123" to pass
                message = "Invalid promo code format"
        )
        String promoCode
) {}
```

*Sift compiles the Pattern only once during initialization, ensuring zero performance overhead during validation.*