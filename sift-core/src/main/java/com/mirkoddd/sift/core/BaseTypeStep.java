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
import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.dsl.TypeStep;

import java.util.Objects;

/**
 * Base template class for evaluating type definitions in the Sift DSL.
 * <p>
 * This abstract class implements the <b>Template Method pattern</b> to centralize the
 * interaction with the regex engine. In the immutable architecture of Sift,
 * this class acts as a secure bridge: every time a type is selected, it forces
 * the {@link PatternAssembler} to clone its state before applying the new mutation.
 * This ensures absolute Thread-Safety and prevents state poisoning when
 * reusing intermediate builder variables.
 *
 * @param <Ctx> The Context (Fragment or Root) enforcing Type-Driven Design.
 * @param <T>   The specific {@link ConnectorStep} returned for standard type definitions.
 * @param <C>   The specific {@link CharacterClassConnectorStep} returned for character classes.
 */
abstract class BaseTypeStep<Ctx extends SiftContext, T extends ConnectorStep<Ctx>, C extends CharacterClassConnectorStep<Ctx>> implements TypeStep<Ctx, T, C> {

    protected final PatternAssembler assembler;

    /**
     * Instantiates the base step with the current state of the pattern assembler.
     *
     * @param assembler The current internal state machine builder.
     */
    protected BaseTypeStep(PatternAssembler assembler) {
        this.assembler = assembler;
    }

    /**
     * Factory method delegated to subclasses to instantiate the correct concrete step
     * for standard, non-character-class types.
     *
     * @param nextAssembler The newly cloned and mutated assembler state.
     * @return The specific connector step defined by the subclass (Fixed or Variable).
     */
    protected abstract T getNormalConnector(PatternAssembler nextAssembler);

    /**
     * Factory method delegated to subclasses to instantiate the correct concrete step
     * for character classes (enabling modifiers like {@code including()}).
     *
     * @param nextAssembler The newly cloned and mutated assembler state.
     * @return The specific character class connector step defined by the subclass.
     */
    protected abstract C getCharacterClassConnector(PatternAssembler nextAssembler);

    /** {@inheritDoc} */
    @Override
    public T anyCharacter() {
        PatternAssembler next = assembler.copy();
        next.addAnyChar();
        return getNormalConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public T character(char literal) {
        PatternAssembler next = assembler.copy();
        next.addCharacter(literal);
        return getNormalConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public T pattern(SiftPattern<SiftContext.Fragment> pattern) {
        Objects.requireNonNull(pattern, "SiftPattern cannot be null");
        PatternAssembler next = assembler.copy();
        next.addPattern(pattern);
        return getNormalConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C digits() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_DIGITS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonDigits() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_DIGITS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C digitsUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_DIGITS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonDigitsUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_DIGITS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C letters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_LETTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonLetters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_LETTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C upperCaseLetters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C lowerCaseLetters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C lettersUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_LETTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonLettersUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_LETTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C upperCaseLettersUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C lowerCaseLettersUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C alphanumeric() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonAlphanumeric() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C alphanumericUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonAlphanumericUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_ALPHANUMERIC);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C wordCharacters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonWordCharacters() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C wordCharactersUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonWordCharactersUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_WORD_CHARACTERS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C whitespace() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.WHITESPACE);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonWhitespace() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_WHITESPACE);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C whitespaceUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_WHITESPACE);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C nonWhitespaceUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.NON_UNICODE_WHITESPACE);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C range(char start, char end) {
        PatternAssembler next = assembler.copy();
        next.addCustomRange(start, end);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public T newline() {
        PatternAssembler next = assembler.copy();
        next.addCharacter('\n');
        return getNormalConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public T carriageReturn() {
        PatternAssembler next = assembler.copy();
        next.addCharacter('\r');
        return getNormalConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public T tab() {
        PatternAssembler next = assembler.copy();
        next.addCharacter('\t');
        return getNormalConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C hexDigits() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.RANGE_HEX_DIGITS);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C punctuation() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.PUNCTUATION);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C punctuationUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_PUNCTUATION);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C blank() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.BLANK);
        return getCharacterClassConnector(next);
    }

    /** {@inheritDoc} */
    @Override
    public C blankUnicode() {
        PatternAssembler next = assembler.copy();
        next.addClassRange(RegexSyntax.UNICODE_BLANK);
        return getCharacterClassConnector(next);
    }
}