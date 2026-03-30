# Contributing to Sift

Thank you for your interest in contributing to Sift! This project is built with a strong focus on architectural elegance, reliability, and automation.

To maintain these high standards, we ask that you read and strictly follow the guidelines below before opening a Pull Request (PR).

## 0. Development Setup

Before writing any code, make sure the project builds and all tests pass on your machine.

**Requirements:** JDK 17+ (the toolchain handles cross-compilation to Java 8 bytecode automatically).
```bash
# Clone your fork
git clone https://github.com/<your-username>/Sift.git
cd Sift

# Run the full test suite across all modules
./gradlew test

# Run tests + generate the JaCoCo coverage report
./gradlew test jacocoTestReport
```

The coverage report for the core module is generated at:
```
sift-core/build/reports/jacoco/test/html/index.html
```

Before submitting a PR, open that file in your browser and verify that line and branch coverage remain at **100%**.

> **Tip:** Before diving into the code, read [ARCHITECTURE.md](ARCHITECTURE.md) — it explains the Lazy AST model and the Visitor Pattern that underpin the entire codebase. It will save you significant time.

## 1. Commit Messages (Conventional Commits)
This repository uses a fully automated CI/CD system based on `release-please`. Versions and changelogs are generated automatically by parsing the Git history.

**All commits MUST follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.**
If your commits do not follow this standard, your PR will not be merged.

* `feat: add support for a new quantifier` (triggers a MINOR release)
* `fix: resolve special character escaping` (triggers a PATCH release)
* `docs: update the README` (no release, documentation update only)
* `chore: update Gradle dependencies` (no release)
* `refactor: extract ConnectorStep interface` (no release)

## 2. Architecture and SOLID Principles
The Sift codebase is designed to be scalable, readable, and maintainable. Any new code introduced must strictly adhere to the **SOLID Principles** (inspired by Robert C. Martin, "Uncle Bob").

Before submitting a change, ensure your code answers the following:
* **Single Responsibility (SRP):** Does your class or method do exactly one thing?
* **Open/Closed (OCP):** Did you extend functionality by creating new interfaces/classes (e.g., a new `TypeStep`), or did you modify existing code? We prefer extension over modification.
* **Liskov Substitution (LSP):** Can your implementations replace base interfaces without breaking the expected behavior?
* **Interface Segregation (ISP):** Are your interfaces small and highly cohesive?
* **Dependency Inversion (DIP):** Does the code depend on abstractions (interfaces) rather than concrete implementations?

PRs introducing "God Classes", overly long methods, or tight coupling will be closed with a request for refactoring.

## 3. Test-Driven Development (TDD) and Coverage
Sift is a core library, which means bugs are unacceptable. We adopt a strict **Test-Driven Development (TDD)** approach.

* **100% Code Coverage Policy:** We enforce a strict 100% line and branch coverage rule. No PR will be accepted if it drops the coverage below this threshold.
* **Tooling:** We use **JUnit Jupiter** (`org.junit.jupiter`) for unit testing and **JaCoCo** for coverage reports. Please ensure all new tests are written using the JUnit Jupiter API.
* If you add a feature (e.g., `feat:`), you must include unit tests that prove its functionality and cover all new branches and edge cases.
* If you fix a bug (e.g., `fix:`), you must first write a failing test that reproduces the bug, and then write the code to make it pass.
* Ensure the entire local test suite passes and verify the coverage report by running `./gradlew test jacocoTestReport` before submitting your code.

## 4. The Pull Request Process
1. Fork the repository and create your feature branch (`git checkout -b feature/feature-name`).
2. Write your tests (TDD).
3. Write your code following the SOLID principles.
4. Commit your changes using Conventional Commits.
5. Open a Pull Request against the `main` branch and fill out the provided template.