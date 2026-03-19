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

import com.mirkoddd.sift.core.dsl.CharacterConnector;
import com.mirkoddd.sift.core.dsl.Connector;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.dsl.Type;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base template class for evaluating type definitions in the Sift DSL.
 * <p>
 * This abstract class implements the <b>Template Method pattern</b> to centralize the
 * interaction with the regex engine. In the immutable AST architecture of Sift,
 * this class acts as a secure factory: every time a type is selected, it creates
 * a new immutable AST node describing the operation, guaranteeing thread-safety
 * and zero overhead.
 *
 * @param <Ctx> The Context (Fragment or Root) enforcing Type-Driven Design.
 * @param <T>   The specific {@link Connector} returned for standard type definitions.
 * @param <C>   The specific {@link CharacterConnector} returned for character classes.
 */
abstract class BaseType<Ctx extends SiftContext, T extends Connector<Ctx>, C extends CharacterConnector<Ctx>> implements Type<Ctx, T, C> {

    // The preceding node in the DSL chain.
    protected final BaseSiftPattern<?> parentNode;

    /**
     * Instantiates the base type step, linking it to the AST chain.
     *
     * @param parentNode The preceding node in the DSL chain.
     */
    protected BaseType(BaseSiftPattern<?> parentNode) {
        this.parentNode = parentNode;
    }

    /**
     * Helper to create the intermediate node representing the specific type instruction.
     */
    private BaseSiftPattern<?> createTypeNode(Consumer<PatternVisitor> operation) {
        return new SiftConnector<>(parentNode, operation);
    }

    /**
     * Factory method delegated to subclasses to instantiate the correct concrete step
     * for standard, non-character-class types.
     *
     * @param nextNode The newly created AST node representing this type.
     * @return The specific connector step defined by the subclass (Fixed or Variable).
     */
    protected abstract T getNormalConnector(BaseSiftPattern<?> nextNode);

    /**
     * Factory method delegated to subclasses to instantiate the correct concrete step
     * for character classes (enabling modifiers like {@code including()}).
     *
     * @param nextNode The newly created AST node representing this character class.
     * @return The specific character class connector step defined by the subclass.
     */
    protected abstract C getCharacterClassConnector(BaseSiftPattern<?> nextNode);

    /** {@inheritDoc} */
    @Override
    public T anyCharacter() {
        return getNormalConnector(createTypeNode(PatternVisitor::visitAnyChar));
    }

    /** {@inheritDoc} */
    @Override
    public T character(char literal) {
        return getNormalConnector(createTypeNode(visitor -> visitor.visitCharacter(literal)));
    }

    /** {@inheritDoc} */
    @Override
    public T of(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "SiftPattern cannot be null");
        return getNormalConnector(createTypeNode(visitor -> visitor.visitPattern(pattern)));
    }

    /** {@inheritDoc} */
    @Override
    public C digits() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.RANGE_DIGITS)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonDigits() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_DIGITS)));
    }

    /** {@inheritDoc} */
    @Override
    public C digitsUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_DIGITS)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonDigitsUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_UNICODE_DIGITS)));
    }

    /** {@inheritDoc} */
    @Override
    public C letters() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.RANGE_LETTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonLetters() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_LETTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C upperCaseLetters() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY)));
    }

    /** {@inheritDoc} */
    @Override
    public C lowerCaseLetters() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY)));
    }

    /** {@inheritDoc} */
    @Override
    public C lettersUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_LETTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonLettersUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_UNICODE_LETTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C upperCaseLettersUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY)));
    }

    /** {@inheritDoc} */
    @Override
    public C lowerCaseLettersUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY)));
    }

    /** {@inheritDoc} */
    @Override
    public C caselessLettersUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_LETTERS_CASELESS)));
    }

    /** {@inheritDoc} */
    @Override
    public C symbolsUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_SYMBOLS)));
    }

    /** {@inheritDoc} */
    @Override
    public C alphanumeric() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.RANGE_ALPHANUMERIC)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonAlphanumeric() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_ALPHANUMERIC)));
    }

    /** {@inheritDoc} */
    @Override
    public C alphanumericUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_ALPHANUMERIC)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonAlphanumericUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_UNICODE_ALPHANUMERIC)));
    }

    /** {@inheritDoc} */
    @Override
    public C wordCharacters() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.WORD_CHARACTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonWordCharacters() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_WORD_CHARACTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C wordCharactersUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_WORD_CHARACTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonWordCharactersUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_UNICODE_WORD_CHARACTERS)));
    }

    /** {@inheritDoc} */
    @Override
    public C whitespace() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.WHITESPACE)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonWhitespace() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_WHITESPACE)));
    }

    /** {@inheritDoc} */
    @Override
    public C whitespaceUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_WHITESPACE)));
    }

    /** {@inheritDoc} */
    @Override
    public C nonWhitespaceUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.NON_UNICODE_WHITESPACE)));
    }

    /** {@inheritDoc} */
    @Override
    public C whitespaceHorizontal() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.HORIZONTAL_WHITESPACE)));
    }

    /** {@inheritDoc} */
    @Override
    public C whitespaceVertical() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.VERTICAL_WHITESPACE)));
    }

    /** {@inheritDoc} */
    @Override
    public C range(char start, char end) {
        if (start > end) {
            throw new IllegalArgumentException("Invalid range: start character '" + start +
                    "' must be less than or equal to end character '" + end + "'.");
        }
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitCustomRange(start, end)));
    }

    /** {@inheritDoc} */
    @Override
    public T newline() {
        return getNormalConnector(createTypeNode(visitor -> visitor.visitCharacter('\n')));
    }

    /** {@inheritDoc} */
    @Override
    public T linebreakUnicode() {
        return getNormalConnector(createTypeNode(PatternVisitor::visitLinebreak));
    }

    /** {@inheritDoc} */
    @Override
    public T carriageReturn() {
        return getNormalConnector(createTypeNode(visitor -> visitor.visitCharacter('\r')));
    }

    /** {@inheritDoc} */
    @Override
    public T tab() {
        return getNormalConnector(createTypeNode(visitor -> visitor.visitCharacter('\t')));
    }

    /** {@inheritDoc} */
    @Override
    public C hexDigits() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.RANGE_HEX_DIGITS)));
    }

    /** {@inheritDoc} */
    @Override
    public C punctuation() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.PUNCTUATION)));
    }

    /** {@inheritDoc} */
    @Override
    public C punctuationUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_PUNCTUATION)));
    }

    /** {@inheritDoc} */
    @Override
    public C blank() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.BLANK)));
    }

    /** {@inheritDoc} */
    @Override
    public C blankUnicode() {
        return getCharacterClassConnector(createTypeNode(visitor -> visitor.visitClassRange(RegexSyntax.UNICODE_BLANK)));
    }
}