# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.4.4](https://github.com/Mirkoddd/Sift/compare/sift-v2.4.3...sift-v2.4.4) (2026-03-06)


### Bug Fixes

* **core:** enforce absolute boundary validation in factories ([1558848](https://github.com/Mirkoddd/Sift/commit/155884849882cfb2253b01a59e8e65bb4ddeead8))

## [2.4.3](https://github.com/Mirkoddd/Sift/compare/sift-v2.4.2...sift-v2.4.3) (2026-03-05)


### Bug Fixes

* **core:** prevent raw regex injection by sealing SiftPattern ([23fdd06](https://github.com/Mirkoddd/Sift/commit/23fdd06789f5ab635d1258ee28c747347fa79ab4))

## [2.4.2](https://github.com/Mirkoddd/Sift/compare/sift-v2.4.1...sift-v2.4.2) (2026-03-04)


### Bug Fixes

* **core:** implement lazy backreference validation ([2853a9a](https://github.com/Mirkoddd/Sift/commit/2853a9a9101892788261226213c8c783096e5013))

## [2.4.1](https://github.com/Mirkoddd/Sift/compare/sift-v2.4.0...sift-v2.4.1) (2026-03-04)


### Performance Improvements

* optimize UUID and MAC address patterns in SiftCatalog using native hexDigits class ([bc8932c](https://github.com/Mirkoddd/Sift/commit/bc8932c176b6e0f632c0232115c4efdd5984386b))

## [2.4.0](https://github.com/Mirkoddd/Sift/compare/sift-v2.3.2...sift-v2.4.0) (2026-03-04)


### Features

* add control characters and utility classes to DSL ([e406ecd](https://github.com/Mirkoddd/Sift/commit/e406ecdcb4d4b56cc8b71531a496c2787e5e85cd))

## [2.3.2](https://github.com/Mirkoddd/Sift/compare/sift-v2.3.1...sift-v2.3.2) (2026-03-03)


### Bug Fixes

* **docs:** remove extra space in javadoc to test pipeline ([47038e3](https://github.com/Mirkoddd/Sift/commit/47038e3f07ecf0d7451ebe73da7e5699228ddc51))

## [2.3.1](https://github.com/Mirkoddd/Sift/compare/sift-v2.3.0...sift-v2.3.1) (2026-03-03)


### Bug Fixes

* **docs:** correct spacing in documentation ([32229a2](https://github.com/Mirkoddd/Sift/commit/32229a2adfddc32c977fc6e26392fa6f09289279))

## [2.3.0](https://github.com/Mirkoddd/Sift/compare/sift-v2.2.0...sift-v2.3.0) (2026-03-03)


### Features

* **api:** add global regex flags and enforce varargs safety ([4b9c494](https://github.com/Mirkoddd/Sift/commit/4b9c4946c2e0ea41b63d34f608d869a6954e45a9))
* **core:** add anti-ReDoS protection and achieve 100% test coverage ([eaac349](https://github.com/Mirkoddd/Sift/commit/eaac34901ee6c90ae80978207af741e9aa137664))
* **core:** add email, webUrl and isoDate to SiftCatalog ([d75cd63](https://github.com/Mirkoddd/Sift/commit/d75cd63cafd71020a04e5870d3fbf5b155ea4ee2))
* **core:** add lookahead and lookbehind support to SiftPatterns ([58d0601](https://github.com/Mirkoddd/Sift/commit/58d0601779f03bcb7ccf0a64a0e4093252cfafb7))
* **core:** add sieve() method to return cached compiled Pattern ([e018b78](https://github.com/Mirkoddd/Sift/commit/e018b78d1dcbdd8466c152f2838dce324fa66fe8))
* **core:** add support for COMMENTS and UNICODE_CASE global flags ([4b0a26b](https://github.com/Mirkoddd/Sift/commit/4b0a26bb9d077d3f8b1c49311633ece2142f7c75))
* **core:** enhance Developer Experience with fail-fast guards and convenience methods ([31cc23f](https://github.com/Mirkoddd/Sift/commit/31cc23f04d7eafa3d846494e803771c120165f6b))
* **core:** implement fail-fast validation for duplicate group names ([80f66dc](https://github.com/Mirkoddd/Sift/commit/80f66dc7e9999ed59ad5669ec2b38f93ef577138))
* **core:** implement secure named captures and backreferences with strict validation ([5106940](https://github.com/Mirkoddd/Sift/commit/5106940f23ec72c427712143f777bd2d8b6bc388))
* **core:** implement type-safe pattern grouping and complete API documentation ([1f45453](https://github.com/Mirkoddd/Sift/commit/1f45453395e79baa3c9986d40b9b8ab92a2e69eb))
* **core:** introduce custom character range support and align JavaDoc ([4d3e71b](https://github.com/Mirkoddd/Sift/commit/4d3e71b4d51b4e934f3c1899211bd4e7dc2eada7))
* implement Unicode character classes, full ASCII/UTF symmetry, and architecture cleanup ([d89756e](https://github.com/Mirkoddd/Sift/commit/d89756e79c0dcbeee5252d1b0a82445ced0e3033))
* migrate to multi-module architecture with Jakarta Validation support ([1fd98fd](https://github.com/Mirkoddd/Sift/commit/1fd98fd826bc773384a8d579917176636271d757))


### Bug Fixes

* **annotations:** narrow exception scope in SiftMatchValidator ([1a365c1](https://github.com/Mirkoddd/Sift/commit/1a365c12da72251e4c432d21c0b83c2c84b08c3e))
* **annotations:** replace RuntimeException with ValidationException in SiftMatchValidator ([2bcbda9](https://github.com/Mirkoddd/Sift/commit/2bcbda94e0964be42b46226cad61043f33781de4))
* CI failing when coverage is unmodified ([625cfeb](https://github.com/Mirkoddd/Sift/commit/625cfeb6049cbef0f33fe5c5e800da29e661b7bd))
* **core:** add fail-fast validation to SiftBuilder.shake() ([e14faf0](https://github.com/Mirkoddd/Sift/commit/e14faf083dc7d519a699efc2a3de7d9505a58a38))
* **core:** add volatile to SiftBuilder cache fields ([e161674](https://github.com/Mirkoddd/Sift/commit/e16167435fb7c34e8481b3bf1283d962056375ab))
* **core:** escape brackets and whitespace to prevent runtime regex crashes ([8bee547](https://github.com/Mirkoddd/Sift/commit/8bee547343982cbf0b720557b72896313b619f1b))
* **core:** memoize shake() and implement identity methods on SiftBuilder ([8594222](https://github.com/Mirkoddd/Sift/commit/85942227a7dd2ade204cdcc181737146b3dbdf42))
* **core:** prevent meaningless zero-max quantifiers in builder ([cffb73c](https://github.com/Mirkoddd/Sift/commit/cffb73cfc54e098cd06669f3982be93836073e62))
* **core:** prevent varargs null poisoning and empty string edge cases ([4575231](https://github.com/Mirkoddd/Sift/commit/457523133ac1bab25c0f4e9fc08a1778a4415cb0))
* **core:** remove circular dependency in GroupName static init ([f12bde6](https://github.com/Mirkoddd/Sift/commit/f12bde6232bc6933cffa9a71a34755a2d6601706))
* **core:** seal group collision loopholes and harden varargs validation ([b6a9648](https://github.com/Mirkoddd/Sift/commit/b6a96489f5be40d8b9e9a46e3a26a1b6b3d20055))
* **core:** support underscores in group names ([22cd832](https://github.com/Mirkoddd/Sift/commit/22cd832d24c0b6dfb2878c56d914a163957b18dd))
* maven badges ([59a41ff](https://github.com/Mirkoddd/Sift/commit/59a41ff2e26a394188e5059875f6f0b7c8b649ac))
* resolve quantifier overwrite bug in withOptional syntactic sugar ([b4c74a2](https://github.com/Mirkoddd/Sift/commit/b4c74a2335d0a025f288651695c7907b8d4078aa))


### Performance Improvements

* **core:** eager initialization of HEX_CHAR in SiftCatalog ([1551305](https://github.com/Mirkoddd/Sift/commit/155130580523b553655f909c8afd3cd9f9d4579b))
* **core:** implement double-checked locking and concurrent coverage tests ([c130136](https://github.com/Mirkoddd/Sift/commit/c13013644a7d9a9bd9f8275b6268b216c2f8609f))
* **core:** memoize anyOf(List) pattern generation to prevent redundant allocations ([3d2d0bd](https://github.com/Mirkoddd/Sift/commit/3d2d0bded46716a7acc44323ddae48bff56cdec7))
* **core:** memoize SiftPatterns lambdas and add caching tests ([401fece](https://github.com/Mirkoddd/Sift/commit/401fece8c236f5820d0fcdc6490b9d25c88276a8))
* optimize anythingBut() to be zero-allocation ([cd0115f](https://github.com/Mirkoddd/Sift/commit/cd0115ff7428764c7ef02c088945bc818543b6d5))

## [2.2.0](https://github.com/Mirkoddd/Sift/compare/sift-v2.1.0...sift-v2.2.0) (2026-03-03)


### Features

* **api:** add global regex flags and enforce varargs safety ([4b9c494](https://github.com/Mirkoddd/Sift/commit/4b9c4946c2e0ea41b63d34f608d869a6954e45a9))
* **core:** add anti-ReDoS protection and achieve 100% test coverage ([eaac349](https://github.com/Mirkoddd/Sift/commit/eaac34901ee6c90ae80978207af741e9aa137664))
* **core:** add email, webUrl and isoDate to SiftCatalog ([d75cd63](https://github.com/Mirkoddd/Sift/commit/d75cd63cafd71020a04e5870d3fbf5b155ea4ee2))
* **core:** add lookahead and lookbehind support to SiftPatterns ([58d0601](https://github.com/Mirkoddd/Sift/commit/58d0601779f03bcb7ccf0a64a0e4093252cfafb7))
* **core:** add sieve() method to return cached compiled Pattern ([e018b78](https://github.com/Mirkoddd/Sift/commit/e018b78d1dcbdd8466c152f2838dce324fa66fe8))
* **core:** add support for COMMENTS and UNICODE_CASE global flags ([4b0a26b](https://github.com/Mirkoddd/Sift/commit/4b0a26bb9d077d3f8b1c49311633ece2142f7c75))
* **core:** enhance Developer Experience with fail-fast guards and convenience methods ([31cc23f](https://github.com/Mirkoddd/Sift/commit/31cc23f04d7eafa3d846494e803771c120165f6b))
* **core:** implement fail-fast validation for duplicate group names ([80f66dc](https://github.com/Mirkoddd/Sift/commit/80f66dc7e9999ed59ad5669ec2b38f93ef577138))
* **core:** implement secure named captures and backreferences with strict validation ([5106940](https://github.com/Mirkoddd/Sift/commit/5106940f23ec72c427712143f777bd2d8b6bc388))
* **core:** implement type-safe pattern grouping and complete API documentation ([1f45453](https://github.com/Mirkoddd/Sift/commit/1f45453395e79baa3c9986d40b9b8ab92a2e69eb))
* **core:** introduce custom character range support and align JavaDoc ([4d3e71b](https://github.com/Mirkoddd/Sift/commit/4d3e71b4d51b4e934f3c1899211bd4e7dc2eada7))
* implement Unicode character classes, full ASCII/UTF symmetry, and architecture cleanup ([d89756e](https://github.com/Mirkoddd/Sift/commit/d89756e79c0dcbeee5252d1b0a82445ced0e3033))
* migrate to multi-module architecture with Jakarta Validation support ([1fd98fd](https://github.com/Mirkoddd/Sift/commit/1fd98fd826bc773384a8d579917176636271d757))


### Bug Fixes

* **annotations:** narrow exception scope in SiftMatchValidator ([1a365c1](https://github.com/Mirkoddd/Sift/commit/1a365c12da72251e4c432d21c0b83c2c84b08c3e))
* **annotations:** replace RuntimeException with ValidationException in SiftMatchValidator ([2bcbda9](https://github.com/Mirkoddd/Sift/commit/2bcbda94e0964be42b46226cad61043f33781de4))
* CI failing when coverage is unmodified ([625cfeb](https://github.com/Mirkoddd/Sift/commit/625cfeb6049cbef0f33fe5c5e800da29e661b7bd))
* **core:** add fail-fast validation to SiftBuilder.shake() ([e14faf0](https://github.com/Mirkoddd/Sift/commit/e14faf083dc7d519a699efc2a3de7d9505a58a38))
* **core:** add volatile to SiftBuilder cache fields ([e161674](https://github.com/Mirkoddd/Sift/commit/e16167435fb7c34e8481b3bf1283d962056375ab))
* **core:** escape brackets and whitespace to prevent runtime regex crashes ([8bee547](https://github.com/Mirkoddd/Sift/commit/8bee547343982cbf0b720557b72896313b619f1b))
* **core:** memoize shake() and implement identity methods on SiftBuilder ([8594222](https://github.com/Mirkoddd/Sift/commit/85942227a7dd2ade204cdcc181737146b3dbdf42))
* **core:** prevent meaningless zero-max quantifiers in builder ([cffb73c](https://github.com/Mirkoddd/Sift/commit/cffb73cfc54e098cd06669f3982be93836073e62))
* **core:** prevent varargs null poisoning and empty string edge cases ([4575231](https://github.com/Mirkoddd/Sift/commit/457523133ac1bab25c0f4e9fc08a1778a4415cb0))
* **core:** remove circular dependency in GroupName static init ([f12bde6](https://github.com/Mirkoddd/Sift/commit/f12bde6232bc6933cffa9a71a34755a2d6601706))
* **core:** seal group collision loopholes and harden varargs validation ([b6a9648](https://github.com/Mirkoddd/Sift/commit/b6a96489f5be40d8b9e9a46e3a26a1b6b3d20055))
* **core:** support underscores in group names ([22cd832](https://github.com/Mirkoddd/Sift/commit/22cd832d24c0b6dfb2878c56d914a163957b18dd))
* maven badges ([59a41ff](https://github.com/Mirkoddd/Sift/commit/59a41ff2e26a394188e5059875f6f0b7c8b649ac))
* resolve quantifier overwrite bug in withOptional syntactic sugar ([b4c74a2](https://github.com/Mirkoddd/Sift/commit/b4c74a2335d0a025f288651695c7907b8d4078aa))


### Performance Improvements

* **core:** eager initialization of HEX_CHAR in SiftCatalog ([1551305](https://github.com/Mirkoddd/Sift/commit/155130580523b553655f909c8afd3cd9f9d4579b))
* **core:** implement double-checked locking and concurrent coverage tests ([c130136](https://github.com/Mirkoddd/Sift/commit/c13013644a7d9a9bd9f8275b6268b216c2f8609f))
* **core:** memoize anyOf(List) pattern generation to prevent redundant allocations ([3d2d0bd](https://github.com/Mirkoddd/Sift/commit/3d2d0bded46716a7acc44323ddae48bff56cdec7))
* **core:** memoize SiftPatterns lambdas and add caching tests ([401fece](https://github.com/Mirkoddd/Sift/commit/401fece8c236f5820d0fcdc6490b9d25c88276a8))
* optimize anythingBut() to be zero-allocation ([cd0115f](https://github.com/Mirkoddd/Sift/commit/cd0115ff7428764c7ef02c088945bc818543b6d5))

## [2.1.0](https://github.com/Mirkoddd/Sift/compare/v2.0.0...v2.1.0) (2026-03-03)


### Features

* **core:** add support for COMMENTS and UNICODE_CASE global flags ([4b0a26b](https://github.com/Mirkoddd/Sift/commit/4b0a26bb9d077d3f8b1c49311633ece2142f7c75))


### Performance Improvements

* **core:** eager initialization of HEX_CHAR in SiftCatalog ([1551305](https://github.com/Mirkoddd/Sift/commit/155130580523b553655f909c8afd3cd9f9d4579b))
* **core:** memoize anyOf(List) pattern generation to prevent redundant allocations ([3d2d0bd](https://github.com/Mirkoddd/Sift/commit/3d2d0bded46716a7acc44323ddae48bff56cdec7))

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
