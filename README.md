# Sift
[![sift-core](https://img.shields.io/maven-central/v/com.mirkoddd/sift-core?label=sift-core)](https://central.sonatype.com/artifact/com.mirkoddd/sift-core)
[![sift-annotations](https://img.shields.io/maven-central/v/com.mirkoddd/sift-annotations?label=sift-annotations)](https://central.sonatype.com/artifact/com.mirkoddd/sift-annotations)
[![Java 8+](https://img.shields.io/badge/Java-8+-blue.svg)](https://adoptium.net/) [![Tests](https://github.com/mirkoddd/Sift/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mirkoddd/Sift/actions)
[![Coverage](https://raw.githubusercontent.com/mirkoddd/Sift/main/.github/badges/jacoco.svg)](https://github.com/mirkoddd/Sift/actions)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![sift-core](https://javadoc.io/badge2/com.mirkoddd/sift-core/javadoc_sift--core.svg)](https://javadoc.io/doc/com.mirkoddd/sift-core)
[![sift-annotations](https://javadoc.io/badge2/com.mirkoddd/sift-annotations/javadoc_sift--annotations.svg)](https://javadoc.io/doc/com.mirkoddd/sift-annotations)

**The Type-Safe, SOLID, and Secure Regular Expression Builder for Java.**

Writing raw Regular Expressions in Java is error-prone, hard to read, and difficult to maintain. Sift solves this by providing a fluent, state-machine-driven Domain Specific Language (DSL) that guarantees valid syntax at compile time.

Build your rules step-by-step, filter out the noise, and when your pattern is ready, just `.shake()` the sieve!

# Quick Start

Add Sift to your project dependencies:

**Maven:**

```XML
   <!-- Replace <latest-version> with the version shown in the Maven Central badge above -->
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

* **Type-Safe by Design:** Sift is built on a rigid State Machine architecture (`QuantifierStep` -> `TypeStep` -> `ConnectorStep` / `VariableConnectorStep`). It is physically impossible to build structurally invalid regex sequences (like applying possessive modifiers to fixed-length strings) using the builder.
* **Advanced Regex Capabilities:** Don't hit a wall when requirements get complex. Sift fully supports advanced engine features like Named Capturing Groups, Backreferences, and Lookarounds (Lookahead/Lookbehind), perfectly integrated into the fluent DSL.
* **Self-Contained Global Flags:** Apply Case-Insensitive, Multiline, or DotAll modes effortlessly via `Sift.filteringWith(...)`. Sift uses inline flags (e.g., `(?i)`), making the resulting `String` 100% portable across databases, JSON payloads, or other languages without relying on external configurations.
* **ASCII by Default:** Predictability is key. Standard methods like `letters()` or `digits()` default strictly to ASCII (`[a-zA-Z]`, `[0-9]`), preventing unexpected matches with foreign alphabets or alternative numbering systems.
* **Global Ready:** Need internationalization? Switch explicitly to the `unicode...()` counterparts (e.g., `unicodeLetters()`) to instantly support characters from any language safely.
* **ReDoS Mitigation Tools:** Java's default regex engine is vulnerable to catastrophic backtracking. Sift helps you write safer patterns without memorizing obscure syntax by exposing possessive quantifiers via `.withoutBacktracking()` and atomic groups via `.preventBacktracking()`.
* **High Performance & Zero Allocation:** Internally optimized with pre-computed O(1) cache lookups and careful memory management. Sift avoids unnecessary object allocations, ensuring lightning-fast regex generation even in tight loops.
* **Zero Dependencies:** The `sift-core` engine is pure Java. It doesn't pull in any bloated transitive dependencies, keeping your final artifact size incredibly small.
* **Android & Enterprise Ready:** Compiled to Java 8 bytecode for maximum compatibility. It ships with built-in Proguard/R8 rules, guaranteeing flawless integration out-of-the-box for both Android applications and modern/legacy Spring Boot servers.
* **100% Test Coverage:** Rock-solid reliability tested against edge cases, negations, and complex nested group scenarios.


# Usage Examples

# 1. Fluent, Type-Safe Regex Generation

Forget about counting backslashes or memorizing obscure symbols. Sift guides your hand using your IDE's auto-completion.

```Java

import static com.mirkoddd.sift.core.Sift.fromStart;

// Goal: Match an international username securely
String regex = fromStart()
        .exactly(1).unicodeLettersUppercaseOnly() // Must start with an uppercase letter
        .then()
        .between(3, 15).unicodeWordCharacters().withoutBacktracking() // Secure against ReDoS
        .then()
        .optional().digits() // May end with an ASCII number
        .andNothingElse()
        .shake();

// Result: ^\p{Lu}[\p{L}\p{Nd}_]{3,15}+[0-9]?$

```

# 2. Seamless Jakarta Validation

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

