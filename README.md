# Sift: A Fluent Regex Builder for Java

Sift is a lightweight, type-safe Java library designed to construct complex Regular Expressions through a readable, object-oriented API. It eliminates the maintenance burden of cryptic string-based regex by applying SOLID principles to pattern construction.

## Overview

Regular expressions are powerful but often become "write-only" code. Sift transforms regex creation into a structured coding process, ensuring that patterns are:

* **Readable:** The syntax mirrors natural language, making the intent of the pattern obvious.
* **Safe:** The fluent interface enforces grammatical correctness at compile-time (e.g., preventing invalid quantifier sequences).
* **Composable:** Complex patterns are built by combining smaller, reusable `SiftPattern` objects.
* **Extensible:** Adheres to the Open/Closed Principle, allowing the definition of custom domain grammars without modifying the core library.

## Installation

### Gradle

Add the dependency to your `build.gradle` file:

```gradle
dependencies {
    implementation 'com.mirkoddd:sift:1.0.0'
}
```

## TODO

### Usage 