# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.1.0] - Unreleased
### Features
* **Core:** Add support for `COMMENTS` (`?x`) and `UNICODE_CASE` (`?u`) global flags.
* **Core:** Enhance Developer Experience with fail-fast guards and convenience methods.
* **Core:** Add `email`, `webUrl`, and `isoDate` to `SiftCatalog`.

### Refactoring
* **Core (Architecture):** Dismantle `SiftBuilder` God Class into segregated, type-safe immutable nodes.
* **Core (Architecture):** Rename `AbstractTypeStep` to `BaseTypeStep` for clearer semantics.
* **Core (API):** Rename type methods to be more readable and discoverable via auto-complete.
* **Core:** Encapsulate quantifier modifiers (`?`, `+`) into a stateful Enum to prevent bugs.

### Performance
* **Core:** Eager initialization of `HEX_CHAR` in `SiftCatalog` to avoid lock contention on boot.
* **Core:** Memoize `anyOf(List)` pattern generation to prevent redundant allocations.
* **Core:** Optimize hex allocations.

### Documentation
* **Core:** Document architectural decisions for "fat" connector classes and memory optimization.
* **Core:** Clarify limitations of anonymous capturing groups in `SiftPatterns`.
* **Core:** Clarify email validation documentation in `SiftCatalog`.
* **Core:** Remove legacy banner-style comments for cleaner code.
* **Docs:** Update README.md with recent architectural changes.

---

## [2.0.0] - 2026-03-02
### Features
* **Core:** Support underscores in capturing group names.
* **Core:** Add `sieve()` method to return cached compiled `Pattern`.
* **Core:** Introduce custom character range support.

### Refactoring
* **Core:** Enforce strict encapsulation for `PatternAssembler`.
* **Core:** Disambiguate regex modifier constants.
* **Core:** Implement SOLID delegates for the State Machine.
* **Core:** Remove public modifier from package-private constants.

### Performance
* **Core:** Memoize `SiftPatterns` lambdas.
* **Core:** Implement double-checked locking for thread-safe regex compilation and caching.
* **Core:** Optimize `anythingBut()` to be zero-allocation.

### Bug Fixes
* **Core:** Prevent meaningless zero-max quantifiers in the builder.
* **Core:** Add `volatile` keyword to cache fields for thread-safety.
* **Core:** Add fail-fast validation to `SiftBuilder.shake()`.
* **Core:** Memoize `shake()` and implement identity methods on `SiftBuilder`.
* **Core:** Remove circular dependency in `GroupName` static initialization.
* **Annotations:** Narrow exception scope in `SiftMatchValidator` and replace `RuntimeException` with `ValidationException`.

---

## [1.6.0] - Previous Release
### Features
* **Core:** Implement fail-fast validation for duplicate group names.

### Refactoring
* **Core:** Enforce compile-time safety for character class modifiers.

### Bug Fixes
* **Core:** Seal group collision loopholes and harden varargs validation.
* **Core:** Escape brackets and whitespace to prevent runtime regex crashes.

### Documentation
* **Core:** Add executable docs for literal null-concatenation anti-pattern.

---

## [1.5.1] - Previous Release
### Refactoring
* **Core:** Centralize build automation and enforce global null-safety.
* **Core:** Implement defensive null-safety and refine documentation.

### Bug Fixes
* **Core:** Prevent varargs null poisoning and empty string edge cases.

---

## [1.5.0] - Universal Compatibility & O(1) Optimizations
### Features
* **Core:** Implement type-safe pattern grouping.
* **Core:** Add lookahead and lookbehind support to `SiftPatterns`.
* **Core:** Implement secure named captures and backreferences with strict validation.
* **Core:** Add global regex flags and enforce varargs safety.
* **Core:** Implement Unicode character classes and full ASCII/UTF symmetry.
* **Core:** Add anti-ReDoS protections.

### Refactoring
* **Core:** Enforce compile-time safety for anti-backtracking operations.
* **Core:** Extract `PatternAssembler` to decouple state machine from string assembly (SRP).

---

## [1.2.0] - Initial Public Releases
### Features
* **Project:** Migrate to multi-module architecture.
* **Annotations:** Add Jakarta Validation support (`@SiftMatch`).
* **Core:** Initial core library structure.

### Bug Fixes
* **Core:** Resolve quantifier overwrite bug in `withOptional` syntactic sugar.

### Maintenance
* **CI/CD:** Add GitHub Actions pipeline for automated Maven Central publishing.
* **CI/CD:** Setup JaCoCo coverage reporting.