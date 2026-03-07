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
package com.mirkoddd.sift.core.dsl;

/**
 * Defines the <b>TYPE</b> of character or pattern to match.
 * <p>
 * This interface represents the state where the exact token to be matched is selected.
 * It can be reached in two ways:
 * <ul>
 * <li><b>Explicitly:</b> Immediately after a quantifier has been set (e.g., {@code .exactly(3).digits()}).</li>
 * <li><b>Implicitly:</b> Directly from a state that expects a token (e.g., {@code .digits()}), which defaults to matching exactly once.</li>
 * </ul>
 * <p>
 * Selecting a type consumes the pending quantifier and transitions the builder to the
 * next structural link, depending on the generic types {@code T} and {@code C}.
 *
 * @param <T> The standard connector step returned for non-character-class types.
 * @param <C> The specialized connector step returned for character classes, which exposes class modifiers like {@code including()}.
 */
public interface TypeStep<Ctx extends SiftContext, T extends ConnectorStep<Ctx>, C extends CharacterClassConnectorStep<Ctx>> {

    /**
     * Matches <b>ANY</b> single character (the Dot {@code .}).
     * <p>
     * <b>Note:</b> This usually includes whitespace and symbols, but excludes line terminators
     * (like {@code \n}) unless specific matcher flags are enabled.
     *
     * @return The standard connector step to continue building.
     */
    T anyCharacter();

    /**
     * Applies the pending quantifier to a specific literal character.
     * <p>
     * Example: {@code .exactly(3).character('a')} will match "aaa".
     * Special regex characters are automatically escaped.
     *
     * @param literal The character to match.
     * @return The standard connector step to continue building.
     */
    T character(char literal);

    /**
     * Applies the pending quantifier to a complex SiftPattern.
     * <p>
     * Example: {@code .optional().pattern(myGroup)} makes the entire group optional.
     * Internally, this wraps the pattern in a non-capturing group {@code (?:...)} if necessary.
     *
     * @param pattern The sub-pattern to apply the quantifier to.
     * @return The standard connector step to continue building.
     * @throws IllegalStateException if the provided pattern contains absolute boundaries
     * (e.g., created with {@code fromStart()} or closed with {@code andNothingElse()}).
     * Reusable blocks must be unanchored.
     */
    T pattern(SiftPattern<SiftContext.Fragment> pattern);

    /**
     * Matches any ASCII numeric digit.
     * <p>
     * Equivalent to the regex range {@code [0-9]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C digits();

    /**
     * Matches any character that is NOT an ASCII digit.
     * <p>
     * Equivalent to the regex {@code \D}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonDigits();

    /**
     * Matches any Unicode decimal digit.
     * <p>
     * Equivalent to the regex {@code \p{Nd}}.
     * Unlike {@link #digits()} which is strictly ASCII {@code [0-9]},
     * this matches digits from other scripts (e.g., Arabic-Indic digits).
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C digitsUnicode();

    /**
     * Matches any character that is NOT a Unicode decimal digit.
     * <p>
     * Equivalent to the regex {@code \P{Nd}}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonDigitsUnicode();

    /**
     * Matches any ASCII letter from the English alphabet (both uppercase and lowercase).
     * <p>
     * Equivalent to the regex range {@code [a-zA-Z]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C letters();

    /**
     * Matches any character that is NOT an ASCII letter.
     * <p>
     * Equivalent to the regex {@code [^a-zA-Z]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonLetters();

    /**
     * Matches only uppercase ASCII letters.
     * <p>
     * Equivalent to the regex range {@code [A-Z]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C upperCaseLetters();

    /**
     * Matches only lowercase ASCII letters.
     * <p>
     * Equivalent to the regex range {@code [a-z]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C lowerCaseLetters();

    /**
     * Matches any Unicode letter from any language or script.
     * <p>
     * Equivalent to the regex {@code \p{L}}.
     * Unlike {@link #letters()} which is strictly ASCII {@code [a-zA-Z]},
     * this correctly matches international characters like 'è', 'ñ', or 'ç'.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C lettersUnicode();

    /**
     * Matches any character that is NOT a Unicode letter.
     * <p>
     * Equivalent to the regex {@code \P{L}}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonLettersUnicode();

    /**
     * Matches any uppercase Unicode letter from any language.
     * <p>
     * Equivalent to the regex {@code \p{Lu}}.
     * Unlike {@link #upperCaseLetters()} which is strictly ASCII {@code [A-Z]},
     * this correctly matches uppercase international characters like 'È' or 'Ñ'.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C upperCaseLettersUnicode();

    /**
     * Matches any lowercase Unicode letter from any language.
     * <p>
     * Equivalent to the regex {@code \p{Ll}}.
     * Unlike {@link #lowerCaseLetters()} which is strictly ASCII {@code [a-z]},
     * this correctly matches lowercase international characters like 'è' or 'ñ'.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C lowerCaseLettersUnicode();

    /**
     * Matches any ASCII alphanumeric character (letters and digits).
     * <p>
     * Equivalent to the regex range {@code [a-zA-Z0-9]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C alphanumeric();

    /**
     * Matches any character that is NOT an ASCII alphanumeric character.
     * <p>
     * Equivalent to the regex {@code [^a-zA-Z0-9]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonAlphanumeric();

    /**
     * Matches any Unicode alphanumeric character (Unicode letters and Unicode digits).
     * <p>
     * Equivalent to the regex {@code [\p{L}\p{Nd}]}.
     * Unlike {@link #wordCharactersUnicode()}, this does NOT include the underscore.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C alphanumericUnicode();

    /**
     * Matches any character that is NOT a Unicode alphanumeric character.
     * <p>
     * Equivalent to the regex {@code [^\p{L}\p{Nd}]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonAlphanumericUnicode();

    /**
     * Matches any ASCII word character (ASCII letters, digits, and underscores).
     * <p>
     * Equivalent to the regex {@code \w}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C wordCharacters();

    /**
     * Matches any character that is NOT an ASCII word character.
     * <p>
     * Equivalent to the regex {@code \W}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonWordCharacters();

    /**
     * Matches any Unicode word character (Unicode letters, Unicode digits, and the underscore).
     * <p>
     * Equivalent to the regex {@code [\p{L}\p{Nd}_]}.
     * Unlike {@link #wordCharacters()} which is strictly ASCII {@code \w},
     * this correctly identifies words in international texts.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C wordCharactersUnicode();

    /**
     * Matches any character that is NOT a Unicode word character.
     * <p>
     * Equivalent to the regex {@code [^\p{L}\p{Nd}_]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonWordCharactersUnicode();

    /**
     * Matches any ASCII whitespace character (spaces, tabs, line breaks).
     * <p>
     * Equivalent to the regex {@code \s}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C whitespace();

    /**
     * Matches any character that is NOT an ASCII whitespace.
     * <p>
     * Equivalent to the regex {@code \S}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonWhitespace();

    /**
     * Matches any Unicode whitespace character (including non-breaking spaces, em-spaces, etc.).
     * <p>
     * Equivalent to the regex {@code \p{IsWhite_Space}}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C whitespaceUnicode();

    /**
     * Matches any character that is NOT a Unicode whitespace.
     * <p>
     * Equivalent to the regex {@code \P{IsWhite_Space}}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C nonWhitespaceUnicode();

    /**
     * Matches any single character within the specified custom range (inclusive).
     * <p>
     * Equivalent to the regex {@code [start-end]}.
     * <br>Example: {@code range('a', 'f')} compiles to the regex {@code [a-f]}.
     * <p>
     * <b>Unicode Note:</b> This method supports all characters in the Basic Multilingual Plane (BMP),
     * which covers almost all standard alphabets, symbols, and common international characters.
     * However, characters requiring Unicode surrogate pairs (such as Emojis or certain rare
     * ideograms) cannot be passed as primitive {@code char} boundaries.
     *
     * @param start The starting character of the range.
     * @param end   The ending character of the range.
     * @return The specialized character class step to allow further class modifications.
     */
    C range(char start, char end);

    /**
     * Matches the newline character (Line Feed).
     * <p>
     * Equivalent to the literal {@code '\n'}.
     *
     * @return The standard connector step to continue building.
     */
    T newline();

    /**
     * Matches the Carriage Return character.
     * <p>
     * Equivalent to the literal {@code '\r'}.
     *
     * @return The standard connector step to continue building.
     */
    T carriageReturn();

    /**
     * Matches the horizontal tab character.
     * <p>
     * Equivalent to the literal {@code '\t'}.
     *
     * @return The standard connector step to continue building.
     */
    T tab();

    /**
     * Matches any hexadecimal digit (case-insensitive).
     * <p>
     * Equivalent to the regex range {@code [0-9a-fA-F]}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C hexDigits();

    /**
     * Matches any ASCII punctuation character.
     * <p>
     * Equivalent to the regex {@code \p{Punct}} (one of {@code !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~}).
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C punctuation();

    /**
     * Matches any Unicode punctuation character.
     * <p>
     * Equivalent to the regex {@code \p{P}}.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C punctuationUnicode();

    /**
     * Matches any ASCII blank character (space or tab).
     * <p>
     * Equivalent to the regex {@code \p{Blank}}. This does not include line terminators.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C blank();

    /**
     * Matches any Unicode blank character (Unicode space separators and tab).
     * <p>
     * This includes characters like non-breaking spaces but excludes line terminators.
     *
     * @return The specialized character class step to allow further class modifications.
     */
    C blankUnicode();
}