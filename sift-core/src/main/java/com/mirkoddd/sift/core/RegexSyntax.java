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

import com.mirkoddd.sift.core.dsl.Type;

/**
 * Dictionary of all special Regular Expression characters used by Sift.
 * <p>
 * This class centralizes the syntax, removing "magic strings" from the builder logic.
 * The constants are organized to perfectly mirror the methods in
 * {@link Type}.
 */
final class RegexSyntax {
    static final String EMPTY = "";
    static final String ANY_CHAR = ".";
    static final String COMMA = ",";
    static final String START_OF_LINE = "^";
    static final String END_OF_LINE = "$";
    static final String WORD_BOUNDARY = "\\b";
    static final String ZERO_OR_MORE = "*";
    static final String ONE_OR_MORE = "+";
    static final String OPTIONAL = "?";
    static final String POSSESSIVE_MODIFIER = "+";
    static final String LAZY_MODIFIER = "?";
    static final String QUANTIFIER_OPEN = "{";
    static final String QUANTIFIER_CLOSE = "}";
    public static final String ATOMIC_GROUP_OPEN = "(?>";
    static final String NON_CAPTURING_GROUP_OPEN = "(?:";
    static final String GROUP_OPEN = "(";
    static final String GROUP_CLOSE = ")";
    static final String NAMED_GROUP_OPEN = "(?<";
    static final String NAMED_GROUP_NAME_CLOSE = ">";
    static final String OR = "|";
    static final String INLINE_FLAG_OPEN = "(?";
    static final String NAMED_BACKREFERENCE_OPEN = "\\k<";
    static final String NAMED_BACKREFERENCE_CLOSE = ">";
    static final String POSITIVE_LOOKAHEAD_OPEN = "(?=";
    static final String NEGATIVE_LOOKAHEAD_OPEN = "(?!";
    static final String POSITIVE_LOOKBEHIND_OPEN = "(?<=";
    static final String NEGATIVE_LOOKBEHIND_OPEN = "(?<!";
    static final String CLASS_OPEN = "[";
    static final String CLASS_CLOSE = "]";
    static final String CLASS_INTERSECTION_NEGATION = "&&[^";
    static final String NEGATION = "^";
    static final String RANGE_DIGITS = "0-9";
    static final String NON_DIGITS = "\\D";
    static final String RANGE_LETTERS = "a-zA-Z";
    static final String NON_LETTERS = "^a-zA-Z"; // Stripped outer brackets
    static final String RANGE_LETTERS_UPPERCASE_ONLY = "A-Z";
    static final String RANGE_LETTERS_LOWERCASE_ONLY = "a-z";
    static final String RANGE_ALPHANUMERIC = "a-zA-Z0-9";
    static final String NON_ALPHANUMERIC = "^a-zA-Z0-9"; // Stripped outer brackets
    static final String WORD_CHARACTERS = "\\w";
    static final String NON_WORD_CHARACTERS = "\\W";
    static final String WHITESPACE = "\\s";
    static final String NON_WHITESPACE = "\\S";
    static final String UNICODE_DIGITS = "\\p{Nd}";
    static final String NON_UNICODE_DIGITS = "\\P{Nd}";
    static final String UNICODE_LETTERS = "\\p{L}";
    static final String NON_UNICODE_LETTERS = "\\P{L}";
    static final String UNICODE_LETTERS_UPPERCASE_ONLY = "\\p{Lu}";
    static final String UNICODE_LETTERS_LOWERCASE_ONLY = "\\p{Ll}";
    static final String UNICODE_LETTERS_CASELESS = "\\p{Lo}";
    static final String UNICODE_SYMBOLS = "\\p{S}";
    static final String UNICODE_ALPHANUMERIC = "\\p{L}\\p{Nd}"; // Stripped outer brackets
    static final String NON_UNICODE_ALPHANUMERIC = "^\\p{L}\\p{Nd}"; // Stripped outer brackets
    static final String UNICODE_WORD_CHARACTERS = "\\p{L}\\p{Nd}_"; // Stripped outer brackets
    static final String NON_UNICODE_WORD_CHARACTERS = "^\\p{L}\\p{Nd}_"; // Stripped outer brackets
    static final String UNICODE_WHITESPACE = "\\p{IsWhite_Space}";
    static final String NON_UNICODE_WHITESPACE = "\\P{IsWhite_Space}";
    static final String RANGE_HEX_DIGITS = "0-9a-fA-F";
    static final String PUNCTUATION = "\\p{Punct}";
    static final String UNICODE_PUNCTUATION = "\\p{P}";
    static final String BLANK = "\\p{Blank}";
    static final String UNICODE_BLANK = "\\p{Zs}\\t"; // Stripped outer brackets
    static final String UNICODE_SCRIPT_GREEK = "\\p{IsGreek}";
    static final String UNICODE_SCRIPT_CYRILLIC = "\\p{IsCyrillic}";
    static final String UNICODE_SCRIPT_ARABIC = "\\p{IsArabic}";
    static final String UNICODE_SCRIPT_HEBREW = "\\p{IsHebrew}";
    static final String UNICODE_SCRIPT_HAN = "\\p{IsHan}";
    static final String UNICODE_SCRIPT_HIRAGANA = "\\p{IsHiragana}";
    static final String UNICODE_SCRIPT_KATAKANA = "\\p{IsKatakana}";
    static final String UNICODE_SCRIPT_LATIN = "\\p{IsLatin}";
    static final String PREVIOUS_MATCH_END = "\\G";
    static final String LINEBREAK = "\\R";
    static final String HORIZONTAL_WHITESPACE = "\\h";
    static final String VERTICAL_WHITESPACE = "\\v";
    static final String CLASS_INTERSECTION = "&&[";
    static final String INLINE_FLAG_SEPARATOR = ":";
    private RegexSyntax() {
        // Prevent instantiation of utility class
    }
}