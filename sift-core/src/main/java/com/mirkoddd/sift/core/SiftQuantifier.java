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
 * Node responsible exclusively for handling Quantifiers in the State Machine.
 */
class SiftQuantifier implements QuantifierStep {

    protected final PatternAssembler assembler;

    SiftQuantifier(PatternAssembler assembler) {
        this.assembler = assembler;
    }

    @Override
    public TypeStep<ConnectorStep, CharacterClassConnectorStep> exactly(int n) {
        if (n <= 0) throw new IllegalArgumentException("Quantity must be strictly positive: " + n);
        PatternAssembler next = assembler.copy();
        next.setQuantifier((n == 1) ? RegexSyntax.EMPTY : RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftFixedType(next);
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> atLeast(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.COMMA + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftVariableType(next);
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> oneOrMore() {
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.ONE_OR_MORE);
        return new SiftVariableType(next);
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> zeroOrMore() {
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.ZERO_OR_MORE);
        return new SiftVariableType(next);
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> optional() {
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.OPTIONAL);
        return new SiftVariableType(next);
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> atMost(int max) {
        if (max == 0) throw new IllegalArgumentException("atMost(0) is invalid as it always matches an empty string.");
        if (max < 0) throw new IllegalArgumentException("Max quantity cannot be negative: " + max);
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + "0" + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftVariableType(next);
    }

    @Override
    public TypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> between(int min, int max) {
        if (min == 0 && max == 0) throw new IllegalArgumentException("between(0, 0) is invalid as it always matches an empty string.");
        if (min < 0) throw new IllegalArgumentException("Min quantity cannot be negative: " + min);
        if (max <= 0) throw new IllegalArgumentException("Max quantity must be strictly positive: " + max);
        if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + min + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftVariableType(next);
    }

    @Override
    public ConnectorStep namedCapture(NamedCapture group) {
        Objects.requireNonNull(group, "NamedCapture cannot be null.");
        PatternAssembler next = assembler.copy();
        next.addNamedCapture(group);
        return new SiftConnector(next);
    }

    @Override
    public ConnectorStep backreference(NamedCapture group) {
        Objects.requireNonNull(group, "Backreference group cannot be null.");
        PatternAssembler next = assembler.copy();
        next.addBackreference(group);
        return new SiftConnector(next);
    }

    @Override public ConnectorStep anyCharacter() { return exactly(1).anyCharacter(); }
    @Override public ConnectorStep character(char literal) { return exactly(1).character(literal); }
    @Override public ConnectorStep pattern(SiftPattern pattern) { return exactly(1).pattern(pattern); }
    @Override public CharacterClassConnectorStep digits() { return exactly(1).digits(); }
    @Override public CharacterClassConnectorStep nonDigits() { return exactly(1).nonDigits(); }
    @Override public CharacterClassConnectorStep digitsUnicode() { return exactly(1).digitsUnicode(); }
    @Override public CharacterClassConnectorStep nonDigitsUnicode() { return exactly(1).nonDigitsUnicode(); }
    @Override public CharacterClassConnectorStep letters() { return exactly(1).letters(); }
    @Override public CharacterClassConnectorStep nonLetters() { return exactly(1).nonLetters(); }
    @Override public CharacterClassConnectorStep uppercaseLetters() { return exactly(1).uppercaseLetters(); }
    @Override public CharacterClassConnectorStep lowercaseLetters() { return exactly(1).lowercaseLetters(); }
    @Override public CharacterClassConnectorStep lettersUnicode() { return exactly(1).lettersUnicode(); }
    @Override public CharacterClassConnectorStep nonLettersUnicode() { return exactly(1).nonLettersUnicode(); }
    @Override public CharacterClassConnectorStep uppercaseLettersUnicode() { return exactly(1).uppercaseLettersUnicode(); }
    @Override public CharacterClassConnectorStep lowercaseLettersUnicode() { return exactly(1).lowercaseLettersUnicode(); }
    @Override public CharacterClassConnectorStep alphanumeric() { return exactly(1).alphanumeric(); }
    @Override public CharacterClassConnectorStep nonAlphanumeric() { return exactly(1).nonAlphanumeric(); }
    @Override public CharacterClassConnectorStep alphanumericUnicode() { return exactly(1).alphanumericUnicode(); }
    @Override public CharacterClassConnectorStep nonAlphanumericUnicode() { return exactly(1).nonAlphanumericUnicode(); }
    @Override public CharacterClassConnectorStep wordCharacters() { return exactly(1).wordCharacters(); }
    @Override public CharacterClassConnectorStep nonWordCharacters() { return exactly(1).nonWordCharacters(); }
    @Override public CharacterClassConnectorStep wordCharactersUnicode() { return exactly(1).wordCharactersUnicode(); }
    @Override public CharacterClassConnectorStep nonWordCharactersUnicode() { return exactly(1).nonWordCharactersUnicode(); }
    @Override public CharacterClassConnectorStep whitespace() { return exactly(1).whitespace(); }
    @Override public CharacterClassConnectorStep nonWhitespace() { return exactly(1).nonWhitespace(); }
    @Override public CharacterClassConnectorStep whitespaceUnicode() { return exactly(1).whitespaceUnicode(); }
    @Override public CharacterClassConnectorStep nonWhitespaceUnicode() { return exactly(1).nonWhitespaceUnicode(); }
    @Override public CharacterClassConnectorStep range(char start, char end) { return exactly(1).range(start, end); }
}