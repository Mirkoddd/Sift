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

/**
 * Type-safe enumeration of standard character sets and Unicode blocks.
 * <p>
 * This enum provides a clean, auto-completable dictionary for safely filtering
 * and intersecting character classes without relying on raw magic strings.
 */
public enum CharacterSubset {

    /** Matches any standard ASCII letter (a-z, A-Z). */
    ASCII_LETTERS(RegexSyntax.RANGE_LETTERS),

    /** Matches only uppercase ASCII letters (A-Z). */
    ASCII_UPPERCASE_LETTERS(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY),

    /** Matches only lowercase ASCII letters (a-z). */
    ASCII_LOWERCASE_LETTERS(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY),

    /** Matches any standard ASCII digit (0-9). */
    ASCII_DIGITS(RegexSyntax.RANGE_DIGITS),

    /** Matches characters in the Greek script and its extensions. */
    GREEK(RegexSyntax.UNICODE_SCRIPT_GREEK),

    /** Matches characters in the Cyrillic script (e.g., Russian, Bulgarian). */
    CYRILLIC(RegexSyntax.UNICODE_SCRIPT_CYRILLIC),

    /** Matches characters in the Arabic script. */
    ARABIC(RegexSyntax.UNICODE_SCRIPT_ARABIC),

    /** Matches characters in the Hebrew script. */
    HEBREW(RegexSyntax.UNICODE_SCRIPT_HEBREW),

    /** Matches Han ideographs (Chinese characters, Kanji, Hanja). */
    HAN(RegexSyntax.UNICODE_SCRIPT_HAN),

    /** Matches characters in the Japanese Hiragana syllabary. */
    HIRAGANA(RegexSyntax.UNICODE_SCRIPT_HIRAGANA),

    /** Matches characters in the Japanese Katakana syllabary. */
    KATAKANA(RegexSyntax.UNICODE_SCRIPT_KATAKANA),

    /** Matches characters in the Latin script (includes extensions and accents). */
    LATIN(RegexSyntax.UNICODE_SCRIPT_LATIN);

    private final String pattern;

    CharacterSubset(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Retrieves the raw regex pattern associated with this subset.
     *
     * @return The string representation of the character class or Unicode property.
     */
    public String getPattern() {
        return pattern;
    }
}