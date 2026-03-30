# Sift — Architecture Guide

This document explains the internal design of Sift for contributors and curious readers.
It is intentionally short: the goal is to give you the mental model you need before reading
the source code, not to replace the source code.

---

## The Core Idea: a Lazy Linked-List AST

When you write a Sift chain like this:

```java
Sift.fromStart()
    .oneOrMore().digits()
    .andNothingElse();
```

you are not building a string. You are building a **linked list of immutable nodes**, where
each node holds a reference to the previous one and a single semantic operation.

```
null ← [fromStart anchor] ← [quantifier: +] ← [type: digits] ← [anchor: $]
                                                                      ↑
                                                               this is what you hold
```

Nothing is computed at this point. The regex string is produced **lazily** — only when you
call `shake()` or `sieve()`.

This design gives you three things for free:
- **Thread safety** — nodes are immutable, nothing is shared
- **Reusability** — intermediate steps can be safely stored and branched from
- **Multiple outputs** — the same AST can be walked by different visitors

---

## The Two Key Classes

### `BaseSiftPattern`

Every node in the chain extends `BaseSiftPattern`. It holds:
- `parentNode` — a reference to the previous node (null for the root)
- `cachedRegex`, `cachedFeatures`, etc. — lazily computed and cached with double-checked locking

The `traverse(PatternVisitor visitor)` method walks the chain from root to leaf recursively,
calling `accept(visitor)` on each node:

```java
protected final void traverse(PatternVisitor visitor) {
    if (parentNode != null) {
        parentNode.traverse(visitor); // walk to the root first
    }
    this.accept(visitor);             // then process this node
}
```

When you call `shake()`, it triggers `evaluateAst()`, which runs a `PatternAssembler` visitor
through the entire chain exactly once. The result is cached. Subsequent calls return the cache.

### `SiftConnector`

The concrete node class. It holds a `Consumer<PatternVisitor>` — a lambda that describes
what this node does when visited:

```java
// Example: what happens when .digits() is called
new SiftConnector<>(parentNode, visitor -> visitor.visitClassRange(RegexSyntax.RANGE_DIGITS));
```

Every DSL method call creates a new `SiftConnector` pointing to the current node as its parent.

---

## The Visitor Pattern

`PatternVisitor` is the interface that decouples the AST structure from what you do with it.
Two implementations ship with Sift:

| Visitor | Purpose |
|---|---|
| `PatternAssembler` | Walks the AST and builds the regex string + feature set |
| `ExplainerVisitor` | Walks the AST and builds the human-readable ASCII tree |

Adding a new output — a future dialect translator, a complexity analyzer, a formatter — means
implementing `PatternVisitor`. No existing node needs to change.

---

## The Type-State Pattern

The DSL uses Java's generic type system to enforce grammatical correctness at compile time.
The state machine has three main states:

```
Quantifier<Ctx>  →  Type<Ctx, T, C>  →  Connector<Ctx>
```

- `Quantifier` — defines HOW MANY times (`exactly`, `oneOrMore`, `between`, etc.)
- `Type` — defines WHAT to match (`digits`, `letters`, `of(pattern)`, etc.)
- `Connector` — connects to the next element or terminates the chain

The `Ctx` type parameter (`Fragment` or `Root`) propagates through the entire chain.
A `Root` pattern cannot be embedded inside another pattern — the compiler rejects it.

Fixed quantifiers (`exactly(n)`) return `Connector` — no lazy/possessive modifiers available.
Variable quantifiers (`oneOrMore()`, `between()`) return `VariableConnector` — which additionally
exposes `asFewAsPossible()` and `withoutBacktracking()`. These methods are physically absent
on fixed quantifiers, not just runtime-guarded.

---

## The Engine SPI

`SiftEngine` is a two-method interface. `AbstractSiftEngine` provides the base implementation
with the Template Method pattern — `compile()` and `checkSupport()` are both `final`, so no
engine can bypass feature validation.

When a pattern is compiled via `sieveWith(engine)`:
1. `shake()` evaluates the AST and collects `Set<RegexFeature>`
2. `engine.compile(rawRegex, features)` is called
3. `AbstractSiftEngine.checkSupport(features)` runs first — it fails fast if the engine
   doesn't support a required feature
4. `doCompile(rawRegex, features)` runs only if validation passes

The `RegexFeature` enum has 10 entries, one per advanced construct. Each engine declares
which features it rejects via `getUnsupportedFeatures()` — an `EnumMap<RegexFeature, String>`
where the value is the error message shown to the user.

---

## Package Layout

```
sift-core/src/main/java/com/mirkoddd/sift/core/
│
├── Sift.java                   ← Public entry point (fromStart, fromAnywhere, etc.)
├── SiftPatterns.java           ← Factory methods (anyOf, literal, capture, lookarounds, etc.)
├── SiftCatalog.java            ← Ready-made patterns (uuid, email, iban, etc.)
├── SiftExplainer.java          ← Public explain() entry point
│
├── BaseSiftPattern.java        ← Base class for all AST nodes (lazy eval, caching)
├── SiftConnector.java          ← Concrete node: holds a Consumer<PatternVisitor>
├── SiftQuantifier.java         ← Quantifier state: routes to Fixed or Variable type
├── BaseType.java               ← Template for type evaluation steps
├── SiftFixedType.java          ← Concrete type for exact quantifiers
├── SiftVariableType.java       ← Concrete type for variable quantifiers
├── SiftVariableConnector.java  ← Connector that exposes lazy/possessive modifiers
├── AtomicPattern.java          ← Decorator that wraps a pattern in (?>...)
│
├── PatternVisitor.java         ← Visitor interface (all visit* methods)
├── PatternAssembler.java       ← Visitor: builds regex string + tracks features
├── ExplainerVisitor.java       ← Visitor: builds human-readable ASCII tree
├── ExplainerTranslator.java    ← i18n helper for ExplainerVisitor
├── AsciiTreeRenderer.java      ← Renders ExplanationNode list into ASCII tree
│
├── PatternMetadata.java        ← Internal interface: exposes groups, features, raw regex
├── RegexNode.java              ← Internal interface: accept(PatternVisitor)
├── RegexSyntax.java            ← All raw regex string constants
├── RegexEscaper.java           ← Escaping logic for literals and character classes
│
├── ConditionalAssembler.java   ← State machine for if/then/else patterns
├── NestingAssembler.java       ← Builder for recursive nested structures
├── NamedCapture.java           ← Typed reference to a named capturing group
├── GroupName.java              ← Validated group name wrapper
├── Delimiter.java              ← Symmetric delimiter pairs for nesting
│
├── dsl/                        ← All public DSL interfaces
│   ├── SiftPattern.java        ← Main public interface (shake, sieve, explain, extract...)
│   ├── Connector.java          ← Standard connector interface
│   ├── VariableConnector.java  ← Connector + lazy/possessive modifiers
│   ├── Quantifier.java         ← Quantifier interface
│   ├── Type.java               ← Type interface (all character/pattern types)
│   ├── Fragment.java           ← Phantom type: embeddable pattern
│   ├── Root.java               ← Phantom type: sealed, non-embeddable pattern
│   └── ...
│
└── engine/
    ├── SiftEngine.java         ← Engine SPI interface
    ├── AbstractSiftEngine.java ← Base with Template Method enforcement
    ├── JdkEngine.java          ← Default JDK implementation
    ├── SiftCompiledPattern.java← Execution interface (match, extract, replace, stream)
    └── RegexFeature.java       ← Enum of 10 trackable advanced features
```

---

## Adding a New DSL Method

To add a new character type (e.g., `.emoticons()`):

1. Add the regex constant to `RegexSyntax.java`
2. Add `visitEmoticons()` to `PatternVisitor.java`
3. Implement `visitEmoticons()` in `PatternAssembler.java` (appends the regex constant)
4. Implement `visitEmoticons()` in `ExplainerVisitor.java` (adds a human-readable node)
5. Add the method to `Type.java` (the interface)
6. Implement it in `BaseType.java` (calls `getCharacterClassConnector(createTypeNode(...))`)
7. Add the implicit fallback in `SiftQuantifier.java`
8. Add i18n keys to all three `.properties` files
9. Write tests

---

## Adding a New Engine

1. Create a new Gradle module (e.g., `sift-engine-myre2`)
2. Extend `AbstractSiftEngine`
3. Override `getUnsupportedFeatures()` — return an `EnumMap` of features your engine rejects
4. Override `doCompile(rawRegex, features)` — compile with your engine, return a `SiftCompiledPattern`
5. Implement `SiftCompiledPattern` — wrap your engine's matcher with the 10-method execution API
6. Write tests — verify all unsupported features throw, and all supported ones execute correctly