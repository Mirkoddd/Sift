# Sift
<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=e931dfa9-02e9-406d-bde7-56f9e0000464" alt=""/>[![Java 8+](https://img.shields.io/badge/Java-8+-blue.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

[![Tests](https://github.com/mirkoddd/Sift/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mirkoddd/Sift/actions)
[![Coverage](https://raw.githubusercontent.com/mirkoddd/Sift/main/.github/badges/jacoco.svg)](https://github.com/mirkoddd/Sift/actions)

**The Type-Safe Regex Builder for Java. If it compiles, it works.**

---

## The Problem

You've seen this before. Someone writes a regex, it works, and six months later nobody — including the author — can read it:

```java
// What does this even do?
Pattern p = Pattern.compile("^(?=[\\p{Lu}])[\\p{L}\\p{Nd}_]{3,15}+[0-9]?$");
```

You add a character class, break the balance of brackets, and find out at runtime. You copy a regex from Stack Overflow, miss an escape, and watch it fail silently in production. You duplicate the same validation pattern across DTOs and forget to update one of them.

**There is a better way.**

---

## The Solution

Sift is a fluent DSL that turns regex construction into readable, self-documenting Java code. Its state machine enforces grammatical correctness at **compile time** — if your pattern compiles, it is structurally valid.

```java
// The same pattern, written with Sift:
String regex = Sift.fromStart()
                .exactly(1).upperCaseLettersUnicode()   // Must start with an uppercase letter
                .then()
                .between(3, 15).wordCharactersUnicode().withoutBacktracking() // ReDoS-safe
                .then()
                .optional().digits()                    // May end with a digit
                .andNothingElse()
                .shake();

// Result: ^[\p{Lu}][\p{L}\p{Nd}_]{3,15}+[0-9]?$
```

Your IDE guides every step. Wrong transitions simply do not exist as methods.

---

## Installation
[![sift-core](https://img.shields.io/maven-central/v/com.mirkoddd/sift-core?label=sift-core)](https://central.sonatype.com/artifact/com.mirkoddd/sift-core)
[![sift-annotations](https://img.shields.io/maven-central/v/com.mirkoddd/sift-annotations?label=sift-annotations)](https://central.sonatype.com/artifact/com.mirkoddd/sift-annotations)

[![sift-engine-re2j](https://img.shields.io/maven-central/v/com.mirkoddd/sift-engine-re2j?label=sift-engine-re2j)](https://central.sonatype.com/artifact/com.mirkoddd/sift-engine-re2j)
[![sift-engine-graalvm](https://img.shields.io/maven-central/v/com.mirkoddd/sift-engine-graalvm?label=sift-engine-graalvm)](https://central.sonatype.com/artifact/com.mirkoddd/sift-engine-graalvm)

**Gradle:**
```groovy
// Core engine — zero external dependencies
implementation 'com.mirkoddd:sift-core:<latest>'

// Optional: Jakarta Validation / Hibernate Validator integration
implementation 'com.mirkoddd:sift-annotations:<latest>'


// Optional: Engine RE2J
implementation 'com.mirkoddd:sift-engine-re2j:<latest>'


// Optional: Engine GraalVM
implementation 'com.mirkoddd:sift-engine-graalvm:<latest>'
```

**Maven:**
```xml
<dependency>
    <groupId>com.mirkoddd</groupId>
    <artifactId>sift-core</artifactId>
    <version>latest</version>
</dependency>

    <!-- Optional: Jakarta Validation / Hibernate Validator integration -->
<dependency>
<groupId>com.mirkoddd</groupId>
<artifactId>sift-annotations</artifactId>
<version>latest</version>
</dependency>


    <!-- Optional: Engine GraalVM -->
<dependency>
<groupId>com.mirkoddd</groupId>
<artifactId>sift-engine-graalvm</artifactId>
<version>latest</version>
</dependency>


    <!-- Optional: Engine RE2J -->
<dependency>
<groupId>com.mirkoddd</groupId>
<artifactId>sift-engine-re2j</artifactId>
<version>latest</version>
</dependency>
```

> Sift targets **Java 8 bytecode** for maximum compatibility — including legacy Spring Boot 2.x and Android.

---

## Core Concepts

### Entry Points

| Method | Generates | Use when |
|---|---|---|
| `Sift.fromStart()` | `^...` | Validating from the start of a line (affected by `MULTILINE` flag) |
| `Sift.fromAbsoluteStart()` | `\A...` | Validating from the absolute start of the string (CRLF/Multi-Line safe) |
| `Sift.fromAnywhere()` | `...` | Building reusable fragments or searching within text |
| `Sift.fromWordBoundary()` | `\b...` | Matching whole words |
| `Sift.fromPreviousMatchEnd()` | `\G...` | Iterative parsing |
| `Sift.filteringWith(flag)` | `(?i)...` | Global flags (case-insensitive, multiline, dotall) |

### Terminal Methods

| Method | Effect |
|---|---|
| `.shake()` | Returns the raw regex `String` |
| `.sieve()` | Compiles with the default JDK engine → `SiftCompiledPattern` |
| `.sieveWith(engine)` | Compiles with a custom engine → `SiftCompiledPattern` |
| `.andNothingElse()` | Appends `$` and seals the pattern — affected by `MULTILINE` flag and trailing newlines |
| `.andNothingElseAbsolutely()` | Appends `\z` — absolute end of string, completely immune to multi-line and CRLF injection bypasses |
| `.andNothingElseBeforeFinalNewline()` | Appends `\Z` — end of string, or just before a final `\n` |
---

## Examples

### 1. Modular Composition — The LEGO Brick Approach

The real power of Sift is the ability to name your building blocks and compose them. Every `Sift.fromAnywhere()` call returns a reusable `SiftPattern<Fragment>` that can be embedded anywhere without carrying unwanted anchors.

```java
// Define named building blocks
SiftPattern<Fragment> year     = Sift.fromAnywhere().exactly(4).digits();
SiftPattern<Fragment> month    = Sift.fromAnywhere().exactly(2).digits();
SiftPattern<Fragment> day      = Sift.fromAnywhere().exactly(2).digits();
SiftPattern<Fragment> dash     = Sift.fromAnywhere().character('-');

// Compose them into a date block
SiftPattern<Fragment> dateBlock = year.followedBy(dash, month, dash, day);

// Embed inside a larger pattern
String logRegex = Sift.fromStart()
        .of(dateBlock)
        .followedBy(' ')
        .then().oneOrMore().anyCharacter()
        .andNothingElse()
        .shake();

// Result: ^[0-9]{4}-[0-9]{2}-[0-9]{2} .+$
```

> **Root vs Fragment:** Patterns built with `fromStart()` or `fromAbsoluteStart()`, or closed with `andNothingElse()`, `andNothingElseAbsolutely()`, or `andNothingElseBeforeFinalNewline()` become `SiftPattern<Root>` — they are sealed and cannot be embedded.
---

### 2. Data Extraction — Beyond Pattern Matching

Sift patterns are not just validators. They are fully equipped extraction tools.

```java
// Define a structured pattern with named groups
NamedCapture yearGroup  = SiftPatterns.capture("year",  Sift.exactly(4).digits());
NamedCapture monthGroup = SiftPatterns.capture("month", Sift.exactly(2).digits());
NamedCapture dayGroup   = SiftPatterns.capture("day",   Sift.exactly(2).digits());

SiftPattern<?> datePattern = Sift.fromStart()
        .namedCapture(yearGroup)
        .followedBy('-')
        .then().namedCapture(monthGroup)
        .followedBy('-')
        .then().namedCapture(dayGroup)
        .andNothingElse();

// Extract structured data directly — no Matcher boilerplate
Map<String, String> fields = datePattern.extractGroups("2026-03-13");
// → { "year": "2026", "month": "03", "day": "13" }

// Extract all matches from a larger text
List<String> prices = Sift.fromAnywhere()
        .oneOrMore().digits()
        .sieve()
        .extractAll("Order: 3 items at 25 and 40 euros");
// → ["3", "25", "40"]

// Stream results lazily for large inputs
Sift.fromAnywhere().oneOrMore().lettersUnicode()
    .streamMatches(largeText)
    .filter(word -> word.length() > 5)
        .forEach(System.out::println);
```

**Full extraction API:**

| Method | Returns | Description |
|---|---|---|
| `containsMatchIn(input)` | `boolean` | Is there at least one match? |
| `matchesEntire(input)` | `boolean` | Does the entire string match? |
| `extractFirst(input)` | `Optional<String>` | First match, or empty |
| `extractAll(input)` | `List<String>` | All matches |
| `extractGroups(input)` | `Map<String, String>` | Named groups from first match |
| `extractAllGroups(input)` | `List<Map<String, String>>` | Named groups from all matches |
| `replaceFirst(input, replacement)` | `String` | Replace first match |
| `replaceAll(input, replacement)` | `String` | Replace all matches |
| `splitBy(input)` | `List<String>` | Split around matches |
| `streamMatches(input)` | `Stream<String>` | Lazy stream of all matches |

---

### 3. Jakarta Validation Integration

Stop duplicating regex logic across your DTOs. Define a rule once, reuse it everywhere with `@SiftMatch`.

```java
// 1. Define a reusable rule
public class PromoCodeRule implements SiftRegexProvider {
    @Override
    public String getRegex() {
        return Sift.fromStart()
                .atLeast(4).letters()
                .then()
                .exactly(3).digits()
                .andNothingElse()
                .shake();
    }
}

// 2. Apply it declaratively — pattern is compiled once at bootstrap
public record ApplyPromoRequest(
        @SiftMatch(
                value   = PromoCodeRule.class,
                flags   = { SiftMatchFlag.CASE_INSENSITIVE },
                message = "Invalid promo code format"
        )
        String promoCode
) {}
```

---

### 4. ReDoS Mitigation

Sift makes performance-safe patterns easy to express without memorizing obscure syntax.

```java
// Possessive quantifier — prevents catastrophic backtracking
Sift.fromAnywhere()
    .oneOrMore().wordCharacters().withoutBacktracking(); // generates \w++

// Atomic group — locks a sub-pattern once matched
SiftPattern<Fragment> safe = Sift.fromAnywhere()
        .oneOrMore().digits()
        .preventBacktracking(); // wraps in (?>...)

// Lazy quantifier — matches as few characters as possible
Sift.fromAnywhere()
    .oneOrMore().anyCharacter().asFewAsPossible(); // generates .+?
```
---
### 5. Alternative Regex Engines

By default, Sift compiles patterns using the standard `java.util.regex` engine via `.sieve()`.
For use cases where the JDK engine is not suitable — such as environments requiring
linear-time guarantees or GraalVM native images — Sift exposes a `SiftEngine` SPI that
accepts any compatible backend.
```java
// Default — uses java.util.regex internally
SiftCompiledPattern pattern = Sift.fromAnywhere()
                .oneOrMore().digits()
                .sieve();

// Custom engine — e.g. RE2J for linear-time, ReDoS-immune matching
SiftCompiledPattern pattern = Sift.fromAnywhere()
        .oneOrMore().digits()
        .sieveWith(Re2jEngine.INSTANCE); // sift-engine-re2j module
```

Sift tracks the advanced features used during pattern construction as a `Set<RegexFeature>`
and passes it to the engine at compile time. If an engine doesn't support a requested
feature — for example, RE2J does not support lookarounds or backreferences — it throws
`UnsupportedOperationException` immediately, before any input is processed.

Available engine modules:
- `sift-core` — includes `JdkEngine` (default, zero dependencies)
- `sift-engine-re2j` — RE2J backend, guarantees linear-time O(n) matching, immune to ReDoS
- `sift-engine-graalvm` — GraalVM TRegex backend, AOT-ready for native images

---

### 6. Lookarounds — Zero-Width Assertions

Lookarounds let you match based on what surrounds a position without consuming those characters. Sift exposes them as readable methods directly on `Connector`, so they flow naturally in the chain.
```java
// Positive lookahead — match "file" only if followed by ".pdf"
SiftPattern<Fragment> pdfFile = Sift.fromAnywhere()
                .oneOrMore().wordCharacters()
                .mustBeFollowedBy(SiftPatterns.literal(".pdf"));

// Negative lookahead — match a number NOT followed by "%"
SiftPattern<Fragment> absoluteValue = Sift.fromAnywhere()
        .oneOrMore().digits()
        .notFollowedBy(SiftPatterns.literal("%"));

// Positive lookbehind — match digits only if preceded by "$"
SiftPattern<Fragment> dollarAmount = Sift.fromAnywhere()
        .oneOrMore().digits()
        .mustBePrecededBy(SiftPatterns.literal("$"));

// Negative lookbehind — match "port" NOT preceded by "pass"
SiftPattern<Fragment> networkPort = Sift.fromAnywhere()
        .of(SiftPatterns.literal("port"))
        .notPrecededBy(SiftPatterns.literal("pass"));
```

All four lookaround types are available both on `Connector` — for inline chaining — and as standalone factories in `SiftPatterns` for use in composition with `followedByAssertion()` and `precededByAssertion()`.

---

### 7. Ready-Made Patterns — SiftCatalog

`SiftCatalog` provides a curated set of production-ready, ReDoS-safe patterns for common formats. All patterns are `Fragment`-typed — they compose cleanly with your own Sift chains.

```java
// Use standalone
boolean valid = SiftCatalog.email().matchesEntire("user@example.com");

// Or embed inside a larger pattern
String regex = Sift.fromStart()
        .of(SiftCatalog.uuid())
        .followedBy('/')
        .then().of(SiftCatalog.isoDate())
        .andNothingElse()
        .shake();
```

Available patterns: `uuid()`, `ipv4()`, `macAddress()`, `email()`, `webUrl()`, `isoDate()`,
`iban()`, `jwt()`, `creditCard()`, `base64()`, `base64Url()`.

---

### 8. Recursive & Nested Structures

Parse arbitrarily deep balanced structures with `SiftPatterns.nesting()`.

```java
// Match nested parentheses: ((a)(b))
SiftPattern<Fragment> nested = SiftPatterns.nesting(5)
                .using(Delimiter.PARENTHESES)
                .containing(Sift.fromAnywhere().oneOrMore().lettersUnicode());

nested.containsMatchIn("((hello)(world))"); // true
```

---

### 9. Conditional Patterns

Sift exposes regex conditional logic — `if/then/else` branches — as a fully type-safe fluent API via `SiftPatterns`.
```java
// Match a price: if preceded by "USD" consume digits only,
// otherwise consume digits followed by a currency symbol
SiftPattern<Fragment> price = SiftPatterns
                .ifPrecededBy(SiftPatterns.literal("USD"))
                .thenUse(Sift.fromAnywhere().oneOrMore().digits())
                .otherwiseUse(
                        Sift.fromAnywhere().oneOrMore().digits()
                                .followedBy(Sift.fromAnywhere().character('€'))
                );

// Else-if chaining is also supported
SiftPattern<Fragment> format = SiftPatterns
        .ifFollowedBy(SiftPatterns.literal("px"))
        .thenUse(Sift.fromAnywhere().oneOrMore().digits())
        .otherwiseIfFollowedBy(SiftPatterns.literal("%"))
        .thenUse(Sift.fromAnywhere().between(1, 3).digits())
        .otherwiseNothing(); // No else branch — engine moves forward silently
```

The state machine enforces the correct declaration order at compile time: `ifXxx` → `thenUse` → `otherwiseUse` / `otherwiseIfFollowedBy` / `otherwiseNothing`. An incomplete conditional is not expressible.

---

### 10. Pattern Explanation

Sift can translate any pattern into a human-readable ASCII tree — useful for debugging,
documentation, and onboarding. The explainer supports multiple languages via i18n.
```java
SiftPattern pattern = Sift.fromStart()
        .oneOrMore().digits()
        .andNothingElse();

// English (default)
System.out.println(pattern.explain());

// Italian
System.out.println(pattern.explain(Locale.ITALIAN));

// Spanish
System.out.println(pattern.explain(new Locale("es")));
```

Output (English):
```
┌─ Starts at the beginning of the line
├─ a digit one or more times
└─ Ends at the end of the line
```

`explain()` is available directly on every `SiftPattern` and delegates to `SiftExplainer`,
which can also be called standalone for more control over the locale resolution.

---

## Why Sift?

| | Raw Java Regex | Sift |
|---|---|---|
| Syntax errors | Discovered at runtime | Impossible to express |
| Readability | Cryptic symbols | Self-documenting method names |
| Reusability | Copy-paste | Named `SiftPattern` fragments |
| Thread safety | Manual | Guaranteed, all patterns are immutable |
| ReDoS protection | Requires expert knowledge | Built-in API |
| Jakarta Validation | Manual `@Pattern` duplication | `@SiftMatch` + `SiftRegexProvider` |
| Regex engine | JDK only | Pluggable (JDK, RE2J, GraalVM, etc...) |
| Dependencies | — | Zero (sift-core) |
| Human-readable explanation | Not possible | `pattern.explain()` with i18n support |
| CRLF / Header Injection | Manual anchor management (`\z`) | Native API (`andNothingElseAbsolutely()`) |
---

## Going Further

- **[Sift Cookbook](COOKBOOK.md)** — Advanced recipes: TSV log parsing, UUID validation, lookarounds, data extraction with named captures, conditional patterns, and more.
- **[Javadoc — sift-core](https://javadoc.io/doc/com.mirkoddd/sift-core)**
- **[Javadoc — sift-annotations](https://javadoc.io/doc/com.mirkoddd/sift-annotations)**
- **[Javadoc — sift-engine-re2j](https://javadoc.io/doc/com.mirkoddd/sift-engine-re2j)**
- **[Javadoc — sift-engine-graalvm](https://javadoc.io/doc/com.mirkoddd/sift-engine-graalvm)**
- **[Changelog](CHANGELOG.md)**
- **[Contributing](CONTRIBUTING.md)**

