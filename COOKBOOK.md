# 🧑‍🍳 Sift Cookbook

Welcome to the Sift Cookbook! These recipes demonstrate the "LEGO brick" composition style of Sift: breaking down complex regular expressions into small, highly readable, and reusable semantic variables.

## 🧱 Key Concept: `fromAnywhere()` vs `fromStart()`
Before diving into the recipes, it is crucial to understand how Sift handles anchors:
- Use **`Sift.fromAnywhere()`** to build reusable intermediate blocks. It creates unanchored patterns that can be chained anywhere in the final expression.
- Use **`Sift.fromStart()`** (usually paired with `.andNothingElse()`) when defining the final, strict structure to anchor the match to the beginning and end of the string.

---

## Recipe 0: Hello, Sift (Your First Pattern)
If this is your first time, start here. A single fluent chain is all it takes.

```java
// Match one or more digits from start to end
String digitsOnly = Sift.fromStart()
        .oneOrMore().digits()
        .andNothingElse()
        .shake();

// Matches: "12345"
// Rejects: "abc", "12 34"
```

---

## Recipe 1: UUID Validator (Modular Blocks)
Break down a complex pattern into small, reusable components. Notice how `fromAnywhere()` ensures these blocks don't carry a `^` anchor, allowing them to be safely placed in the middle of our final chain.

```java
var hex8 = Sift.fromAnywhere().exactly(8).hexDigits();
var hex4 = Sift.fromAnywhere().exactly(4).hexDigits();
var hex12 = Sift.fromAnywhere().exactly(12).hexDigits();
var separator = Sift.fromAnywhere().character('-');

// Compose reusable intermediate blocks
var hex4andSeparator = hex4.followedBy(separator);

// Final assembly
String uuidRegex = hex8
        .followedBy(separator, hex4andSeparator, hex4andSeparator, hex4andSeparator, hex12)
        .shake();

// Matches: "123e4567-e89b-12d3-a456-426614174000"
```

## Recipe 2: Parsing TSV Log Files (Semantic Sentences)
Sift allows you to assemble patterns like natural language sentences.

```java
var year = Sift.fromAnywhere().exactly(4).digits();
var month = Sift.fromAnywhere().exactly(2).digits();
var day = Sift.fromAnywhere().exactly(2).digits();
var dash = Sift.fromAnywhere().character('-');

var dateBlock = year.followedBy(dash, month, dash, day);
var tab = Sift.fromAnywhere().tab();
var newline = Sift.fromAnywhere().newline();
var logLevel = Sift.fromAnywhere().oneOrMore().uppercaseLetters();
var message = Sift.fromAnywhere().oneOrMore().anyCharacter();

String logParserRegex = dateBlock
        .followedBy(tab, logLevel, tab, message, newline)
        .shake();
```

> 💡 **Tip:** Sift also supports a verbose fluent style (`then().exactly(1).tab()` etc.) that produces the same regex. Prefer the concise style shown above unless you're onboarding junior devs who benefit from maximum explicitness.

For reference:

```java
// Verbose/Fluent alternative (Produces the exact same regex)
String verboseLogParserRegex = dateBlock
        .then().exactly(1).tab()
        .then().exactly(1).pattern(logLevel)
        .then().tab() 
        .then().pattern(message)
        .then().newline()
        .shake();
```

## Recipe 3: Strict Token Validator (Prefixes and Suffixes)
We enforce that the token MUST begin with an exact prefix by anchoring the entire evaluation to the start of the string using `fromStart()`.

```java
var prefix = Sift.fromStart().exactly(2).uppercaseLetters();
var body = Sift.fromAnywhere().between(4, 6).alphanumeric();
var suffix = Sift.fromAnywhere().oneOrMore().punctuation();
var underscore = Sift.fromAnywhere().character('_');

var prefixWithUnderscore = prefix.followedBy(underscore);
var bodyWithUnderscore = body.followedBy(underscore);

// Fully strict validation
String securityTokenRegex = prefixWithUnderscore
        .followedBy(bodyWithUnderscore, suffix)
        .andNothingElse()
        .shake();

// Matches: "AK_aB9fE_!#*"
// Rejects: "DIRTY_AK_aB9fE_!#*" (caught by fromStart())
```

## Recipe 4: Reusing the Sift Catalog (IP Addresses)
Sift provides a catalog of built-in, highly tested patterns. You can easily wrap them in strict boundaries.

```java
// Built-in pattern for IPv4
var libIPv4 = SiftCatalog.ipv4();

// Enforcing anchor start and anchor end
String ipv4Regex = Sift.fromStart()
        .pattern(libIPv4)
        .andNothingElse()
        .shake();

// Matches: "192.168.1.1"
```

## Recipe 5: Data Extraction (Named Captures, Backreferences, Flags)
This demonstrates data extraction, modularity, and lazy validation of backreferences. `closeTag` can safely define a backreference to `groupTag` even though the capture actually happens inside `openTag`.

```java
char openBracket = '<';
char closeBracket = '>';
String closingTagPrefix = "</";

var tagName = Sift.fromAnywhere().oneOrMore().alphanumeric();
var tagContent = Sift.fromAnywhere().oneOrMore().anyCharacter();

var groupTag = SiftPatterns.capture("tag", tagName);
var groupContent = SiftPatterns.capture("content", tagContent);

var openTag = Sift.fromAnywhere()
        .character(openBracket)
        .then().namedCapture(groupTag)
        .then().character(closeBracket);

var content = Sift.fromAnywhere().namedCapture(groupContent);

var closeTag = Sift.fromAnywhere()
        .pattern(SiftPatterns.literal(closingTagPrefix))
        .then().backreference(groupTag) // Lazy Evaluation
        .then().character(closeBracket);

// SiftGlobalFlag is elegantly applied at the root!
var htmlTagPattern = Sift
        .filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
        .fromStart()
        .pattern(openTag)
        .followedBy(content, closeTag)
        .andNothingElse();

// Data extraction using Sift's sieve()
Matcher matcher = htmlTagPattern.sieve().matcher("<TITLE>My Awesome Cookbook</title>");
if (matcher.find()) {
    System.out.println(matcher.group("tag"));     // "TITLE"
    System.out.println(matcher.group("content")); // "My Awesome Cookbook"
}
```

## Recipe 6: Password Validation (Positive Lookaheads)
Lookarounds are notoriously difficult to read in raw Regex. Sift makes them declarative.

```java
var requiresUppercase = Sift.fromAnywhere()
        .pattern(SiftPatterns.positiveLookahead(
                Sift.fromAnywhere().zeroOrMore().anyCharacter().then().exactly(1).uppercaseLetters()
        ));

var requiresDigit = Sift.fromAnywhere()
        .pattern(SiftPatterns.positiveLookahead(
                Sift.fromAnywhere().zeroOrMore().anyCharacter().then().exactly(1).digits()
        ));

var passwordPattern = Sift.fromStart()
        .pattern(requiresUppercase)
        .then().pattern(requiresDigit)
        .then().between(8, 64).anyCharacter()
        .andNothingElse()
        .shake();

// Matches: "Hello123"
// Rejects: "alllower1", "NoDigitsHere"
```

## Recipe 7: HTTP Method Matching (Alternation)
Use `anyOf` to match one value from a fixed set of literals.

```java
var httpMethodPattern = Sift.fromStart()
        .pattern(SiftPatterns.anyOf(
                SiftPatterns.literal("GET"),
                SiftPatterns.literal("POST"),
                SiftPatterns.literal("PUT"),
                SiftPatterns.literal("DELETE"),
                SiftPatterns.literal("PATCH")
        ))
        .andNothingElse()
        .shake();

// Matches: "GET", "POST", "DELETE"
// Rejects: "get", "OPTIONS"
```

## Recipe 8: Security & ReDoS Mitigation
Prevent Catastrophic Backtracking on complex inputs using Sift's possessive and lazy modifiers.

```java
// POSSESSIVE MATCHING (withoutBacktracking)
// Tells the engine to NEVER give back matched characters, preventing infinite loops.
var safePayloadExtractor = Sift.fromStart()
        .character('{')
        .then().oneOrMore().wordCharacters().withoutBacktracking()
        .then().character('}')
        .shake();

// Matches: "{secured_payload_data}" without risking ReDoS on invalid inputs.

// LAZY MATCHING (asFewAsPossible)
// Finds the shortest path instead of the greedy default.
var lazyTagExtractor = Sift.fromStart()
        .character('<')
        .then().oneOrMore().anyCharacter().asFewAsPossible()
        .then().character('>')
        .shake();

// On input "<first>...<second>", it stops at the first closing '>' instead of the last one.
```
