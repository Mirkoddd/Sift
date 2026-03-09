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
 * <p>
 * <b>Architectural Role:</b><br>
 * This class acts as the central router for type generation. Depending on the quantifier
 * chosen, it securely transitions the state machine to either a fixed-length constraint
 * ({@link SiftFixedType}) or a variable-length constraint ({@link SiftVariableType}).
 * <p>
 * Additionally, it implements the "Implicit Quantity" fallback: if a user directly requests
 * a character class (e.g., {@code .digits()}) without specifying a quantifier, this node
 * automatically intercepts the call and delegates it to {@code .exactly(1)}.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
class SiftQuantifier<Ctx extends SiftContext> implements QuantifierStep<Ctx> {

    protected final PatternAssembler assembler;

    /**
     * Instantiates the quantifier step with the current state of the pattern assembler.
     *
     * @param assembler The internal state machine builder.
     */
    SiftQuantifier(PatternAssembler assembler) {
        this.assembler = assembler;
    }

    /** {@inheritDoc} */
    @Override
    public TypeStep<Ctx, ConnectorStep<Ctx>, CharacterClassConnectorStep<Ctx>> exactly(int n) {
        if (n <= 0) throw new IllegalArgumentException("Quantity must be strictly positive: " + n);
        PatternAssembler next = assembler.copy();
        next.setQuantifier((n == 1) ? RegexSyntax.EMPTY : RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftFixedType<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public TypeStep<Ctx, VariableConnectorStep<Ctx>, VariableCharacterClassConnectorStep<Ctx>> atLeast(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.COMMA + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftVariableType<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public TypeStep<Ctx, VariableConnectorStep<Ctx>, VariableCharacterClassConnectorStep<Ctx>> oneOrMore() {
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.ONE_OR_MORE);
        return new SiftVariableType<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public TypeStep<Ctx, VariableConnectorStep<Ctx>, VariableCharacterClassConnectorStep<Ctx>> zeroOrMore() {
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.ZERO_OR_MORE);
        return new SiftVariableType<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public TypeStep<Ctx, VariableConnectorStep<Ctx>, VariableCharacterClassConnectorStep<Ctx>> optional() {
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.OPTIONAL);
        return new SiftVariableType<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public TypeStep<Ctx, VariableConnectorStep<Ctx>, VariableCharacterClassConnectorStep<Ctx>> atMost(int max) {
        if (max == 0) throw new IllegalArgumentException("atMost(0) is invalid as it always matches an empty string.");
        if (max < 0) throw new IllegalArgumentException("Max quantity cannot be negative: " + max);
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + "0" + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftVariableType<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public TypeStep<Ctx, VariableConnectorStep<Ctx>, VariableCharacterClassConnectorStep<Ctx>> between(int min, int max) {
        if (min == 0 && max == 0) throw new IllegalArgumentException("between(0, 0) is invalid as it always matches an empty string.");
        if (min < 0) throw new IllegalArgumentException("Min quantity cannot be negative: " + min);
        if (max <= 0) throw new IllegalArgumentException("Max quantity must be strictly positive: " + max);
        if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
        PatternAssembler next = assembler.copy();
        next.setQuantifier(RegexSyntax.QUANTIFIER_OPEN + min + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE);
        return new SiftVariableType<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> namedCapture(NamedCapture group) {
        Objects.requireNonNull(group, "NamedCapture cannot be null.");
        PatternAssembler next = assembler.copy();
        next.addNamedCapture(group);
        return new SiftConnector<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> backreference(NamedCapture group) {
        Objects.requireNonNull(group, "Backreference group cannot be null.");
        PatternAssembler next = assembler.copy();
        next.addBackreference(group);
        return new SiftConnector<>(next);
    }

    // --- Implicit Quantity Fallbacks (Defaults to exactly 1) ---

    @Override public ConnectorStep<Ctx> anyCharacter() { return exactly(1).anyCharacter(); }
    @Override public ConnectorStep<Ctx> character(char literal) { return exactly(1).character(literal); }
    @Override public ConnectorStep<Ctx> of(SiftPattern<SiftContext.Fragment> pattern) { return exactly(1).of(pattern); }
    @Override public CharacterClassConnectorStep<Ctx> digits() { return exactly(1).digits(); }
    @Override public CharacterClassConnectorStep<Ctx> nonDigits() { return exactly(1).nonDigits(); }
    @Override public CharacterClassConnectorStep<Ctx> digitsUnicode() { return exactly(1).digitsUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> nonDigitsUnicode() { return exactly(1).nonDigitsUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> letters() { return exactly(1).letters(); }
    @Override public CharacterClassConnectorStep<Ctx> nonLetters() { return exactly(1).nonLetters(); }
    @Override public CharacterClassConnectorStep<Ctx> upperCaseLetters() { return exactly(1).upperCaseLetters(); }
    @Override public CharacterClassConnectorStep<Ctx> lowerCaseLetters() { return exactly(1).lowerCaseLetters(); }
    @Override public CharacterClassConnectorStep<Ctx> lettersUnicode() { return exactly(1).lettersUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> nonLettersUnicode() { return exactly(1).nonLettersUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> upperCaseLettersUnicode() { return exactly(1).upperCaseLettersUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> lowerCaseLettersUnicode() { return exactly(1).lowerCaseLettersUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> alphanumeric() { return exactly(1).alphanumeric(); }
    @Override public CharacterClassConnectorStep<Ctx> nonAlphanumeric() { return exactly(1).nonAlphanumeric(); }
    @Override public CharacterClassConnectorStep<Ctx> alphanumericUnicode() { return exactly(1).alphanumericUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> nonAlphanumericUnicode() { return exactly(1).nonAlphanumericUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> wordCharacters() { return exactly(1).wordCharacters(); }
    @Override public CharacterClassConnectorStep<Ctx> nonWordCharacters() { return exactly(1).nonWordCharacters(); }
    @Override public CharacterClassConnectorStep<Ctx> wordCharactersUnicode() { return exactly(1).wordCharactersUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> nonWordCharactersUnicode() { return exactly(1).nonWordCharactersUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> whitespace() { return exactly(1).whitespace(); }
    @Override public CharacterClassConnectorStep<Ctx> nonWhitespace() { return exactly(1).nonWhitespace(); }
    @Override public CharacterClassConnectorStep<Ctx> whitespaceUnicode() { return exactly(1).whitespaceUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> nonWhitespaceUnicode() { return exactly(1).nonWhitespaceUnicode(); }
    @Override public CharacterClassConnectorStep<Ctx> range(char start, char end) { return exactly(1).range(start, end); }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> newline() {
        return exactly(1).newline();
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> carriageReturn() {
        return exactly(1).carriageReturn();
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> tab() {
        return exactly(1).tab();
    }

    /** {@inheritDoc} */
    @Override
    public CharacterClassConnectorStep<Ctx> hexDigits() {
        return exactly(1).hexDigits();
    }

    /** {@inheritDoc} */
    @Override
    public CharacterClassConnectorStep<Ctx> punctuation() {
        return exactly(1).punctuation();
    }

    /** {@inheritDoc} */
    @Override
    public CharacterClassConnectorStep<Ctx> punctuationUnicode() {
        return exactly(1).punctuationUnicode();
    }

    /** {@inheritDoc} */
    @Override
    public CharacterClassConnectorStep<Ctx> blank() {
        return exactly(1).blank();
    }

    /** {@inheritDoc} */
    @Override
    public CharacterClassConnectorStep<Ctx> blankUnicode() {
        return exactly(1).blankUnicode();
    }
}