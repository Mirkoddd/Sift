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
import java.util.Set;

/**
 * Internal implementation of the State Machine.
 * Orchestrates the DSL flow and delegates regex construction to the PatternAssembler.
 */
class SiftBuilder implements QuantifierStep, ConnectorStep, VariableConnectorStep, VariableCharacterClassConnectorStep {

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
    // QUANTIFIERS
    // ===================================================================================

    @Override
    public TypeStep<ConnectorStep, CharacterClassConnectorStep> exactly(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        assembler.setQuantifier((n == 1) ? RegexSyntax.EMPTY :
                RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.QUANTIFIER_CLOSE);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> atLeast(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.COMMA + RegexSyntax.QUANTIFIER_CLOSE);
        return (TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> oneOrMore() {
        assembler.setQuantifier(RegexSyntax.ONE_OR_MORE);
        return (TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> zeroOrMore() {
        assembler.setQuantifier(RegexSyntax.ZERO_OR_MORE);
        return (TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> optional() {
        assembler.setQuantifier(RegexSyntax.OPTIONAL);
        return (TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> atMost(int max) {
        if (max < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + max);
        assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + "0" + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return (TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep>) (Object) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> between(int min, int max) {
        if (min < 0 || max < 0) throw new IllegalArgumentException("Quantities cannot be negative");
        if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
        assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + min + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return (TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep>) (Object) this;
    }

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
    // TYPE DEFINITIONS (Non-Class -> VariableConnectorStep)
    // ===================================================================================

    @Override
    public VariableConnectorStep any() {
        assembler.addAnyChar();
        return this;
    }

    @Override
    public VariableConnectorStep character(char literal) {
        assembler.addCharacter(literal);
        return this;
    }

    @Override
    public VariableConnectorStep pattern(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "SiftPattern cannot be null");
        assembler.addPattern(pattern);
        return this;
    }

    // ===================================================================================
    // TYPE DEFINITIONS (Class -> VariableCharacterClassConnectorStep)
    // ===================================================================================

    @Override
    public VariableCharacterClassConnectorStep digits() {
        assembler.addClassRange(RegexSyntax.RANGE_DIGITS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonDigits() {
        assembler.addClassRange(RegexSyntax.NON_DIGITS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep unicodeDigits() {
        assembler.addClassRange(RegexSyntax.UNICODE_DIGITS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonUnicodeDigits() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_DIGITS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep letters() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonLetters() {
        assembler.addClassRange(RegexSyntax.NON_LETTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep lettersUppercaseOnly() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep lettersLowercaseOnly() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep unicodeLetters() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonUnicodeLetters() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_LETTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep unicodeLettersUppercaseOnly() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep unicodeLettersLowercaseOnly() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep alphanumeric() {
        assembler.addClassRange(RegexSyntax.RANGE_ALPHANUMERIC);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonAlphanumeric() {
        assembler.addClassRange(RegexSyntax.NON_ALPHANUMERIC);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep unicodeAlphanumeric() {
        assembler.addClassRange(RegexSyntax.UNICODE_ALPHANUMERIC);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonUnicodeAlphanumeric() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_ALPHANUMERIC);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep wordCharacters() {
        assembler.addClassRange(RegexSyntax.WORD_CHARACTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonWordCharacters() {
        assembler.addClassRange(RegexSyntax.NON_WORD_CHARACTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep unicodeWordCharacters() {
        assembler.addClassRange(RegexSyntax.UNICODE_WORD_CHARACTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonUnicodeWordCharacters() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_WORD_CHARACTERS);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep whitespace() {
        assembler.addClassRange(RegexSyntax.WHITESPACE);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonWhitespace() {
        assembler.addClassRange(RegexSyntax.NON_WHITESPACE);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep unicodeWhitespace() {
        assembler.addClassRange(RegexSyntax.UNICODE_WHITESPACE);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep nonUnicodeWhitespace() {
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

        for (int i = 0; i < additionalPatterns.length; i++) {
            Objects.requireNonNull(additionalPatterns[i], "SiftPattern at index " + i + " cannot be null");
        }

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
    public VariableCharacterClassConnectorStep including(char extra, char... additionalExtras) {
        assembler.addClassInclusion(extra, additionalExtras);
        return this;
    }

    @Override
    public VariableCharacterClassConnectorStep excluding(char excluded, char... additionalExcluded) {
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

    Set<String> getRegisteredGroupNames() {
        return assembler.getRegisteredGroups();
    }
}