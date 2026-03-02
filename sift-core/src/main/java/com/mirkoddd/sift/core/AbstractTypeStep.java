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

import com.mirkoddd.sift.core.dsl.CharacterClassConnectorStep;
import com.mirkoddd.sift.core.dsl.ConnectorStep;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.dsl.TypeStep;

import java.util.Objects;

/**
 * Base template class for evaluating type definitions in the Sift DSL.
 * <p>
 * This abstract class implements the Template Method pattern to centralize the
 * interaction with the regex engine. In the immutable architecture of Sift,
 * this class acts as a secure bridge: every time a type is selected, it forces
 * the PatternAssembler to clone its state before applying the new mutation.
 * This ensures absolute Thread-Safety and prevents state poisoning when
 * reusing intermediate builder variables.
 *
 * @param <T> The specific {@link ConnectorStep} returned for standard type definitions.
 * @param <C> The specific {@link CharacterClassConnectorStep} returned for character classes.
 */
abstract class AbstractTypeStep<T extends ConnectorStep, C extends CharacterClassConnectorStep> implements TypeStep<T, C> {

    protected final PatternAssembler assembler;

    protected AbstractTypeStep(PatternAssembler assembler) {
        this.assembler = assembler;
    }

    protected abstract T getNormalConnector(PatternAssembler nextAssembler);

    protected abstract C getCharacterClassConnector(PatternAssembler nextAssembler);

    @Override
    public T any() {
        PatternAssembler next = assembler.copy();
        next.addAnyChar();
        return getNormalConnector(next);
    }

    @Override
    public T character(char literal) {
        PatternAssembler next = assembler.copy();
        next.addCharacter(literal);
        return getNormalConnector(next);
    }

    @Override
    public T pattern(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "SiftPattern cannot be null");
        PatternAssembler next = assembler.copy();
        next.addPattern(pattern);
        return getNormalConnector(next);
    }

    @Override
    public C digits() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_DIGITS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonDigits() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_DIGITS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C unicodeDigits() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_DIGITS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonUnicodeDigits() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_DIGITS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C letters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_LETTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonLetters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_LETTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C lettersUppercaseOnly() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    @Override
    public C lettersLowercaseOnly() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    @Override
    public C unicodeLetters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_LETTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonUnicodeLetters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_LETTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C unicodeLettersUppercaseOnly() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    @Override
    public C unicodeLettersLowercaseOnly() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    @Override
    public C alphanumeric() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonAlphanumeric() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    @Override
    public C unicodeAlphanumeric() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonUnicodeAlphanumeric() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    @Override
    public C wordCharacters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonWordCharacters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C unicodeWordCharacters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonUnicodeWordCharacters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    @Override
    public C whitespace() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.WHITESPACE);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonWhitespace() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_WHITESPACE);
        return getCharacterClassConnector(next);
    }

    @Override
    public C unicodeWhitespace() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_WHITESPACE);
        return getCharacterClassConnector(next);
    }

    @Override
    public C nonUnicodeWhitespace() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_WHITESPACE);
        return getCharacterClassConnector(next);
    }

    @Override
    public C range(char start, char end) {
        PatternAssembler next = assembler.copy();
        next.addCustomRange(start, end);
        return getCharacterClassConnector(next);
    }
}