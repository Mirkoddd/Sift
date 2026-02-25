/*
 * Copyright 2026 Mirko Dimartino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mirkoddd.sift.core;

import com.mirkoddd.sift.core.dsl.*;

import java.util.Objects;

/**
 * Internal implementation of the State Machine.
 * Orchestrates the DSL flow and delegates regex construction to the PatternAssembler.
 */
class SiftBuilder implements QuantifierStep, ConnectorStep, VariableConnectorStep {

    private final PatternAssembler assembler;

    /**
     * Default constructor for standard builder.
     */
    SiftBuilder() {
        this.assembler = new PatternAssembler();
    }

    /**
     * Constructor that initializes the pattern with global inline flags.
     *
     * @param flags The flags to apply (guaranteed to be at least one by the public API).
     */
    SiftBuilder(SiftGlobalFlag... flags) {
        this.assembler = new PatternAssembler(flags);
    }

    public SiftBuilder anchorStart() {
        assembler.addAnchor(RegexSyntax.START_OF_LINE);
        return this;
    }

    // ===================================================================================
    // QUANTIFIERS (Fixed length returns ConnectorStep, Variable returns VariableConnectorStep)
    // ===================================================================================

    @Override
    public TypeStep<ConnectorStep> exactly(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        assembler.setQuantifier((n == 1) ? RegexSyntax.EMPTY :
                RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.QUANTIFIER_CLOSE);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep> atLeast(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.COMMA + RegexSyntax.QUANTIFIER_CLOSE);
        return (TypeStep<VariableConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep> oneOrMore() {
        assembler.setQuantifier(RegexSyntax.ONE_OR_MORE);
        return (TypeStep<VariableConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep> zeroOrMore() {
        assembler.setQuantifier(RegexSyntax.ZERO_OR_MORE);
        return (TypeStep<VariableConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep> optional() {
        assembler.setQuantifier(RegexSyntax.OPTIONAL);
        return (TypeStep<VariableConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep> atMost(int max) {
        if (max < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + max);
        assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + "0" + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return (TypeStep<VariableConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep> between(int min, int max) {
        if (min < 0 || max < 0) throw new IllegalArgumentException("Quantities cannot be negative");
        if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
        assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + min + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return (TypeStep<VariableConnectorStep>) (Object) this;
    }

    // ===================================================================================
    // NAMED CAPTURES & BACKREFERENCES
    // ===================================================================================

    @Override
    public ConnectorStep namedCapture(NamedCapture group) {
        Objects.requireNonNull(group, "NamedCapture cannot be null.");
        assembler.addNamedCapture(group);
        return this;
    }

    @Override
    public ConnectorStep backreference(NamedCapture group) {
        Objects.requireNonNull(group, "Backreference group cannot be null.");
        assembler.addBackreference(group);
        return this;
    }

    // ===================================================================================
    // TYPE DEFINITIONS (These return whatever ConnectorStep was passed down)
    // ===================================================================================

    @Override
    public ConnectorStep any() {
        assembler.addAnyChar();
        return this;
    }

    @Override
    public ConnectorStep character(char literal) {
        assembler.addCharacter(literal);
        return this;
    }

    @Override
    public ConnectorStep pattern(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "SiftPattern cannot be null");
        assembler.addPattern(pattern);
        return this;
    }

    @Override
    public ConnectorStep digits() {
        assembler.addClassRange(RegexSyntax.RANGE_DIGITS);
        return this;
    }

    @Override
    public ConnectorStep nonDigits() {
        assembler.addClassRange(RegexSyntax.NON_DIGITS);
        return this;
    }

    @Override
    public ConnectorStep unicodeDigits() {
        assembler.addClassRange(RegexSyntax.UNICODE_DIGITS);
        return this;
    }

    @Override
    public ConnectorStep nonUnicodeDigits() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_DIGITS);
        return this;
    }

    @Override
    public ConnectorStep letters() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS);
        return this;
    }

    @Override
    public ConnectorStep nonLetters() {
        assembler.addClassRange(RegexSyntax.NON_LETTERS);
        return this;
    }

    @Override
    public ConnectorStep lettersUppercaseOnly() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY);
        return this;
    }

    @Override
    public ConnectorStep lettersLowercaseOnly() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY);
        return this;
    }

    @Override
    public ConnectorStep unicodeLetters() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS);
        return this;
    }

    @Override
    public ConnectorStep nonUnicodeLetters() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_LETTERS);
        return this;
    }

    @Override
    public ConnectorStep unicodeLettersUppercaseOnly() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY);
        return this;
    }

    @Override
    public ConnectorStep unicodeLettersLowercaseOnly() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY);
        return this;
    }

    @Override
    public ConnectorStep alphanumeric() {
        assembler.addClassRange(RegexSyntax.RANGE_ALPHANUMERIC);
        return this;
    }

    @Override
    public ConnectorStep nonAlphanumeric() {
        assembler.addClassRange(RegexSyntax.NON_ALPHANUMERIC);
        return this;
    }

    @Override
    public ConnectorStep unicodeAlphanumeric() {
        assembler.addClassRange(RegexSyntax.UNICODE_ALPHANUMERIC);
        return this;
    }

    @Override
    public ConnectorStep nonUnicodeAlphanumeric() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_ALPHANUMERIC);
        return this;
    }

    @Override
    public ConnectorStep wordCharacters() {
        assembler.addClassRange(RegexSyntax.WORD_CHARACTERS);
        return this;
    }

    @Override
    public ConnectorStep nonWordCharacters() {
        assembler.addClassRange(RegexSyntax.NON_WORD_CHARACTERS);
        return this;
    }

    @Override
    public ConnectorStep unicodeWordCharacters() {
        assembler.addClassRange(RegexSyntax.UNICODE_WORD_CHARACTERS);
        return this;
    }

    @Override
    public ConnectorStep nonUnicodeWordCharacters() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_WORD_CHARACTERS);
        return this;
    }

    @Override
    public ConnectorStep whitespace() {
        assembler.addClassRange(RegexSyntax.WHITESPACE);
        return this;
    }

    @Override
    public ConnectorStep nonWhitespace() {
        assembler.addClassRange(RegexSyntax.NON_WHITESPACE);
        return this;
    }

    @Override
    public ConnectorStep unicodeWhitespace() {
        assembler.addClassRange(RegexSyntax.UNICODE_WHITESPACE);
        return this;
    }

    @Override
    public ConnectorStep nonUnicodeWhitespace() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_WHITESPACE);
        return this;
    }

    // ===================================================================================
    // CONNECTOR METHODS
    // ===================================================================================

    @Override
    public ConnectorStep followedBy(char c) {
        return this.then().exactly(1).character(c);
    }

    @Override
    public ConnectorStep followedBy(SiftPattern pattern, SiftPattern... additionalPatterns) {
        Objects.requireNonNull(pattern, "First SiftPattern cannot be null");
        Objects.requireNonNull(additionalPatterns, "Additional SiftPatterns array cannot be null");

        ConnectorStep current = this.then().exactly(1).pattern(pattern);

        for (SiftPattern p : additionalPatterns) {
            current = current.then().exactly(1).pattern(p);
        }

        return current;
    }

    @Override
    public QuantifierStep then() {
        assembler.flush();
        return this;
    }

    @Override
    public ConnectorStep including(char extra, char... additionalExtras) {
        assembler.addClassInclusion(extra, additionalExtras);
        return this;
    }

    @Override
    public ConnectorStep excluding(char excluded, char... additionalExcluded) {
        assembler.addClassExclusion(excluded, additionalExcluded);
        return this;
    }

    @Override
    public ConnectorStep wordBoundary() {
        assembler.addWordBoundary();
        return this;
    }

    @Override
    public ConnectorStep withoutBacktracking() {
        assembler.applyPossessiveModifier();
        return this;
    }

    @Override
    public SiftPattern andNothingElse() {
        assembler.addAnchor(RegexSyntax.END_OF_LINE);
        return this;
    }

    @Override
    public String shake() {
        return assembler.build();
    }
}