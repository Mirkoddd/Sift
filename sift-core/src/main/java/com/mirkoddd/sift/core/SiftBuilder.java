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
import java.util.regex.Pattern;

/**
 * Internal implementation of the State Machine.
 * Orchestrates the DSL flow and delegates regex construction to the PatternAssembler.
 */
class SiftBuilder implements QuantifierStep, ConnectorStep, VariableConnectorStep, VariableCharacterClassConnectorStep {

    private final PatternAssembler assembler;
    private final FixedType fixedType;
    private final VariableType variableType;
    private volatile String cachedRegex = null;
    private volatile Pattern cachedPattern = null;
    /**
     * Default constructor for standard builder.
     */
    SiftBuilder() {
        this.assembler = new PatternAssembler();
        this.fixedType = new FixedType(this);
        this.variableType = new VariableType(this);
    }

    /**
     * Constructor that initializes the pattern with global inline flags.
     *
     * @param flags The flags to apply (guaranteed to be at least one by the public API).
     */
    SiftBuilder(SiftGlobalFlag... flags) {
        this.assembler = new PatternAssembler(flags);
        this.fixedType = new FixedType(this);
        this.variableType = new VariableType(this);
    }

    private SiftBuilder(PatternAssembler clonedAssembler) {
        this.assembler = clonedAssembler;
        this.fixedType = new FixedType(this);
        this.variableType = new VariableType(this);
    }

    SiftBuilder cloneBuilder() {
        return new SiftBuilder(this.assembler.copy());
    }

    PatternAssembler getAssembler() {
        return this.assembler;
    }

    public SiftBuilder anchorStart() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.addAnchor(RegexSyntax.START_OF_LINE);
        return clone;
    }

    @Override
    public TypeStep<ConnectorStep, CharacterClassConnectorStep> exactly(int n) {
        if (n <= 0) throw new IllegalArgumentException("Quantity must be strictly positive: " + n);
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.setQuantifier((n == 1) ? RegexSyntax.EMPTY :
                RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.QUANTIFIER_CLOSE);
        return clone.fixedType;
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> atLeast(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.COMMA + RegexSyntax.QUANTIFIER_CLOSE);
        return clone.variableType;
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> oneOrMore() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.setQuantifier(RegexSyntax.ONE_OR_MORE);
        return clone.variableType;
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> zeroOrMore() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.setQuantifier(RegexSyntax.ZERO_OR_MORE);
        return clone.variableType;
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> optional() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.setQuantifier(RegexSyntax.OPTIONAL);
        return clone.variableType;
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> atMost(int max) {
        if (max < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + max);
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + "0" + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return clone.variableType;
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> between(int min, int max) {
        if (min < 0 || max < 0) throw new IllegalArgumentException("Quantities cannot be negative");
        if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + min + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return clone.variableType;
    }

    @Override
    public ConnectorStep namedCapture(NamedCapture group) {
        Objects.requireNonNull(group, "NamedCapture cannot be null.");
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.addNamedCapture(group);
        return clone;
    }

    @Override
    public ConnectorStep backreference(NamedCapture group) {
        Objects.requireNonNull(group, "Backreference group cannot be null.");
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.addBackreference(group);
        return clone;
    }

    @Override
    public ConnectorStep any() {
        return fixedType.any();
    }

    @Override
    public ConnectorStep character(char literal) {
        return fixedType.character(literal);
    }

    @Override
    public ConnectorStep pattern(SiftPattern pattern) {
        return fixedType.pattern(pattern);
    }

    @Override
    public CharacterClassConnectorStep digits() {
        return fixedType.digits();
    }

    @Override
    public CharacterClassConnectorStep nonDigits() {
        return fixedType.nonDigits();
    }

    @Override
    public CharacterClassConnectorStep unicodeDigits() {
        return fixedType.unicodeDigits();
    }

    @Override
    public CharacterClassConnectorStep nonUnicodeDigits() {
        return fixedType.nonUnicodeDigits();
    }

    @Override
    public CharacterClassConnectorStep letters() {
        return fixedType.letters();
    }

    @Override
    public CharacterClassConnectorStep nonLetters() {
        return fixedType.nonLetters();
    }

    @Override
    public CharacterClassConnectorStep lettersUppercaseOnly() {
        return fixedType.lettersUppercaseOnly();
    }

    @Override
    public CharacterClassConnectorStep lettersLowercaseOnly() {
        return fixedType.lettersLowercaseOnly();
    }

    @Override
    public CharacterClassConnectorStep unicodeLetters() {
        return fixedType.unicodeLetters();
    }

    @Override
    public CharacterClassConnectorStep nonUnicodeLetters() {
        return fixedType.nonUnicodeLetters();
    }

    @Override
    public CharacterClassConnectorStep unicodeLettersUppercaseOnly() {
        return fixedType.unicodeLettersUppercaseOnly();
    }

    @Override
    public CharacterClassConnectorStep unicodeLettersLowercaseOnly() {
        return fixedType.unicodeLettersLowercaseOnly();
    }

    @Override
    public CharacterClassConnectorStep alphanumeric() {
        return fixedType.alphanumeric();
    }

    @Override
    public CharacterClassConnectorStep nonAlphanumeric() {
        return fixedType.nonAlphanumeric();
    }

    @Override
    public CharacterClassConnectorStep unicodeAlphanumeric() {
        return fixedType.unicodeAlphanumeric();
    }

    @Override
    public CharacterClassConnectorStep nonUnicodeAlphanumeric() {
        return fixedType.nonUnicodeAlphanumeric();
    }

    @Override
    public CharacterClassConnectorStep wordCharacters() {
        return fixedType.wordCharacters();
    }

    @Override
    public CharacterClassConnectorStep nonWordCharacters() {
        return fixedType.nonWordCharacters();
    }

    @Override
    public CharacterClassConnectorStep unicodeWordCharacters() {
        return fixedType.unicodeWordCharacters();
    }

    @Override
    public CharacterClassConnectorStep nonUnicodeWordCharacters() {
        return fixedType.nonUnicodeWordCharacters();
    }

    @Override
    public CharacterClassConnectorStep whitespace() {
        return fixedType.whitespace();
    }

    @Override
    public CharacterClassConnectorStep nonWhitespace() {
        return fixedType.nonWhitespace();
    }

    @Override
    public CharacterClassConnectorStep unicodeWhitespace() {
        return fixedType.unicodeWhitespace();
    }

    @Override
    public CharacterClassConnectorStep nonUnicodeWhitespace() {
        return fixedType.nonUnicodeWhitespace();
    }

    @Override
    public CharacterClassConnectorStep range(char start, char end) {
        return fixedType.range(start, end);
    }

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
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.flush();
        return clone;
    }

    @Override
    public VariableCharacterClassConnectorStep including(char extra, char... additionalExtras) {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.addClassInclusion(extra, additionalExtras);
        return clone;
    }

    @Override
    public VariableCharacterClassConnectorStep excluding(char excluded, char... additionalExcluded) {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.addClassExclusion(excluded, additionalExcluded);
        return clone;
    }

    @Override
    public ConnectorStep wordBoundary() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.addWordBoundary();
        return clone;
    }

    @Override
    public ConnectorStep withoutBacktracking() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.applyPossessiveModifier();
        return clone;
    }

    @Override
    public ConnectorStep asFewAsPossible() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.applyLazyModifier();
        return clone;
    }

    @Override
    public SiftPattern andNothingElse() {
        SiftBuilder clone = this.cloneBuilder();
        clone.assembler.addAnchor(RegexSyntax.END_OF_LINE);
        return clone;
    }

    @Override
    public String shake() {
        if (cachedRegex == null) {

            PatternAssembler tempAssembler = this.assembler.copy();
            String generatedRegex = tempAssembler.build();

            try {
                cachedPattern = Pattern.compile(generatedRegex);
                cachedRegex = generatedRegex;
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new IllegalStateException("Sift generated an invalid regex pattern: " + generatedRegex +
                        ". Please report this bug to the library maintainers.", e);
            }
        }
        return cachedRegex;
    }

    @Override
    public Pattern sieve() {
        shake();
        return cachedPattern;
    }

    Set<String> getRegisteredGroupNames() {
        return assembler.getRegisteredGroups();
    }

    @Override
    public String toString() {
        return shake();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiftBuilder that = (SiftBuilder) o;
        return this.shake().equals(that.shake());
    }

    @Override
    public int hashCode() {
        return Objects.hash(shake());
    }
}