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
 * interaction with the {@link PatternAssembler}. It prevents code duplication
 * across different quantification states (e.g., fixed length vs. variable length)
 * by delegating the decision of the return type to its concrete subclasses.
 *
 * @param <T> The specific {@link ConnectorStep} returned for standard type definitions.
 * @param <C> The specific {@link CharacterClassConnectorStep} returned for character classes.
 */
abstract class AbstractTypeStep<T extends ConnectorStep, C extends CharacterClassConnectorStep> implements TypeStep<T, C> {

    protected final PatternAssembler assembler;

    /**
     * Constructs the base step with the shared regex assembler.
     *
     * @param assembler The stateful assembler building the regex string.
     */
    protected AbstractTypeStep(PatternAssembler assembler) {
        this.assembler = assembler;
    }

    /**
     * Provides the appropriate step for standard type definitions (e.g., any, literal).
     * @return The next step in the DSL chain.
     */
    protected abstract T getNormalConnector();

    /**
     * Provides the appropriate step for character class definitions (e.g., digits, letters).
     * @return The next step in the DSL chain, exposing character class modifiers.
     */
    protected abstract C getCharacterClassConnector();

    @Override
    public T any() {
        assembler.addAnyChar();
        return getNormalConnector();
    }

    @Override
    public T character(char literal) {
        assembler.addCharacter(literal);
        return getNormalConnector();
    }

    @Override
    public T pattern(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "SiftPattern cannot be null");
        assembler.addPattern(pattern);
        return getNormalConnector();
    }

    @Override
    public C digits() {
        assembler.addClassRange(RegexSyntax.RANGE_DIGITS);
        return getCharacterClassConnector();
    }

    @Override
    public C nonDigits() {
        assembler.addClassRange(RegexSyntax.NON_DIGITS);
        return getCharacterClassConnector();
    }

    @Override
    public C unicodeDigits() {
        assembler.addClassRange(RegexSyntax.UNICODE_DIGITS);
        return getCharacterClassConnector();
    }

    @Override
    public C nonUnicodeDigits() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_DIGITS);
        return getCharacterClassConnector();
    }

    @Override
    public C letters() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C nonLetters() {
        assembler.addClassRange(RegexSyntax.NON_LETTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C lettersUppercaseOnly() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector();
    }

    @Override
    public C lettersLowercaseOnly() {
        assembler.addClassRange(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector();
    }

    @Override
    public C unicodeLetters() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C nonUnicodeLetters() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_LETTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C unicodeLettersUppercaseOnly() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector();
    }

    @Override
    public C unicodeLettersLowercaseOnly() {
        assembler.addClassRange(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector();
    }

    @Override
    public C alphanumeric() {
        assembler.addClassRange(RegexSyntax.RANGE_ALPHANUMERIC);
        return getCharacterClassConnector();
    }

    @Override
    public C nonAlphanumeric() {
        assembler.addClassRange(RegexSyntax.NON_ALPHANUMERIC);
        return getCharacterClassConnector();
    }

    @Override
    public C unicodeAlphanumeric() {
        assembler.addClassRange(RegexSyntax.UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector();
    }

    @Override
    public C nonUnicodeAlphanumeric() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector();
    }

    @Override
    public C wordCharacters() {
        assembler.addClassRange(RegexSyntax.WORD_CHARACTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C nonWordCharacters() {
        assembler.addClassRange(RegexSyntax.NON_WORD_CHARACTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C unicodeWordCharacters() {
        assembler.addClassRange(RegexSyntax.UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C nonUnicodeWordCharacters() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector();
    }

    @Override
    public C whitespace() {
        assembler.addClassRange(RegexSyntax.WHITESPACE);
        return getCharacterClassConnector();
    }

    @Override
    public C nonWhitespace() {
        assembler.addClassRange(RegexSyntax.NON_WHITESPACE);
        return getCharacterClassConnector();
    }

    @Override
    public C unicodeWhitespace() {
        assembler.addClassRange(RegexSyntax.UNICODE_WHITESPACE);
        return getCharacterClassConnector();
    }

    @Override
    public C nonUnicodeWhitespace() {
        assembler.addClassRange(RegexSyntax.NON_UNICODE_WHITESPACE);
        return getCharacterClassConnector();
    }
}