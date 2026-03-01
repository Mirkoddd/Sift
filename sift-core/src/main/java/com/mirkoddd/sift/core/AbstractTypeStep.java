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
 * the parent builder to clone its state before applying the new mutation.
 * This ensures absolute Thread-Safety and prevents state poisoning when
 * reusing intermediate builder variables.
 *
 * @param <T> The specific {@link ConnectorStep} returned for standard type definitions.
 * @param <C> The specific {@link CharacterClassConnectorStep} returned for character classes.
 */
abstract class AbstractTypeStep<T extends ConnectorStep, C extends CharacterClassConnectorStep> implements TypeStep<T, C> {

    protected final SiftBuilder parentBuilder;

    protected AbstractTypeStep(SiftBuilder parentBuilder) {
        this.parentBuilder = parentBuilder;
    }

    protected abstract T getNormalConnector(SiftBuilder clone);

    protected abstract C getCharacterClassConnector(SiftBuilder clone);

    @Override
    public T any() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addAnyChar();
        return getNormalConnector(clone);
    }

    @Override
    public T character(char literal) {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addCharacter(literal);
        return getNormalConnector(clone);
    }

    @Override
    public T pattern(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "SiftPattern cannot be null");
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addPattern(pattern);
        return getNormalConnector(clone);
    }

    @Override
    public C digits() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.RANGE_DIGITS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonDigits() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_DIGITS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C unicodeDigits() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.UNICODE_DIGITS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonUnicodeDigits() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_UNICODE_DIGITS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C letters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.RANGE_LETTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonLetters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_LETTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C lettersUppercaseOnly() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C lettersLowercaseOnly() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C unicodeLetters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.UNICODE_LETTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonUnicodeLetters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_UNICODE_LETTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C unicodeLettersUppercaseOnly() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C unicodeLettersLowercaseOnly() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C alphanumeric() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.RANGE_ALPHANUMERIC);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonAlphanumeric() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_ALPHANUMERIC);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C unicodeAlphanumeric() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonUnicodeAlphanumeric() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C wordCharacters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.WORD_CHARACTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonWordCharacters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_WORD_CHARACTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C unicodeWordCharacters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonUnicodeWordCharacters() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C whitespace() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.WHITESPACE);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonWhitespace() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_WHITESPACE);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C unicodeWhitespace() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.UNICODE_WHITESPACE);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C nonUnicodeWhitespace() {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addClassRange(RegexSyntax.NON_UNICODE_WHITESPACE);
        return getCharacterClassConnector(clone);
    }

    @Override
    public C range(char start, char end) {
        SiftBuilder clone = parentBuilder.cloneBuilder();
        clone.getAssembler().addCustomRange(start, end);
        return getCharacterClassConnector(clone);
    }
}