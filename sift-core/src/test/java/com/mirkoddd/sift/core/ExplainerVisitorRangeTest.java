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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExplainerVisitorRangeTest {

    static Stream<Arguments> provideRangesForTesting() {
        return Stream.of(
                Arguments.of(RegexSyntax.RANGE_DIGITS, "Matches a digit (0-9)"),
                Arguments.of(RegexSyntax.NON_DIGITS, "Matches a non-digit (\\D)"),
                Arguments.of(RegexSyntax.RANGE_LETTERS, "Matches a letter (a-zA-Z)"),
                Arguments.of(RegexSyntax.NON_LETTERS, "Matches a non-letter (^a-zA-Z)"),
                Arguments.of(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY, "Matches an uppercase letter (A-Z)"),
                Arguments.of(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY, "Matches a lowercase letter (a-z)"),
                Arguments.of(RegexSyntax.RANGE_ALPHANUMERIC, "Matches an alphanumeric character (a-zA-Z0-9)"),
                Arguments.of(RegexSyntax.NON_ALPHANUMERIC, "Matches a non-alphanumeric character (^a-zA-Z0-9)"),
                Arguments.of(RegexSyntax.WORD_CHARACTERS, "Matches a word character (\\w)"),
                Arguments.of(RegexSyntax.NON_WORD_CHARACTERS, "Matches a non-word character (\\W)"),
                Arguments.of(RegexSyntax.WHITESPACE, "Matches a whitespace character (\\s)"),
                Arguments.of(RegexSyntax.NON_WHITESPACE, "Matches a non-whitespace character (\\S)"),
                Arguments.of(RegexSyntax.HORIZONTAL_WHITESPACE, "Matches a horizontal whitespace character (\\h)"),
                Arguments.of(RegexSyntax.VERTICAL_WHITESPACE, "Matches a vertical whitespace character (\\v)"),
                Arguments.of(RegexSyntax.RANGE_HEX_DIGITS, "Matches a hexadecimal digit (0-9a-fA-F)"),
                Arguments.of(RegexSyntax.PUNCTUATION, "Matches a punctuation character (\\p{Punct})"),
                Arguments.of(RegexSyntax.BLANK, "Matches a blank space or tab (\\p{Blank})"),

                Arguments.of(RegexSyntax.UNICODE_DIGITS, "Matches a Unicode digit (\\p{Nd})"),
                Arguments.of(RegexSyntax.NON_UNICODE_DIGITS, "Matches a Unicode non-digit (\\P{Nd})"),
                Arguments.of(RegexSyntax.UNICODE_LETTERS, "Matches a Unicode letter (\\p{L})"),
                Arguments.of(RegexSyntax.NON_UNICODE_LETTERS, "Matches a Unicode non-letter (\\P{L})"),
                Arguments.of(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY, "Matches a Unicode uppercase letter (\\p{Lu})"),
                Arguments.of(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY, "Matches a Unicode lowercase letter (\\p{Ll})"),
                Arguments.of(RegexSyntax.UNICODE_LETTERS_CASELESS, "Matches a Unicode caseless letter (\\p{Lo})"),
                Arguments.of(RegexSyntax.UNICODE_SYMBOLS, "Matches a Unicode symbol (\\p{S})"),
                Arguments.of(RegexSyntax.UNICODE_ALPHANUMERIC, "Matches a Unicode alphanumeric character (\\p{L}\\p{Nd})"),
                Arguments.of(RegexSyntax.NON_UNICODE_ALPHANUMERIC, "Matches a Unicode non-alphanumeric character (^\\p{L}\\p{Nd})"),
                Arguments.of(RegexSyntax.UNICODE_WORD_CHARACTERS, "Matches a Unicode word character (\\p{L}\\p{Nd}_)"),
                Arguments.of(RegexSyntax.NON_UNICODE_WORD_CHARACTERS, "Matches a Unicode non-word character (^\\p{L}\\p{Nd}_)"),
                Arguments.of(RegexSyntax.UNICODE_WHITESPACE, "Matches a Unicode whitespace character (\\p{IsWhite_Space})"),
                Arguments.of(RegexSyntax.NON_UNICODE_WHITESPACE, "Matches a Unicode non-whitespace character (\\P{IsWhite_Space})"),
                Arguments.of(RegexSyntax.UNICODE_PUNCTUATION, "Matches a Unicode punctuation character (\\p{P})"),
                Arguments.of(RegexSyntax.UNICODE_BLANK, "Matches a Unicode blank space or tab (\\p{Zs}\\t)"),

                Arguments.of(RegexSyntax.UNICODE_SCRIPT_GREEK, "Matches a Greek script character (\\p{IsGreek})"),
                Arguments.of(RegexSyntax.UNICODE_SCRIPT_CYRILLIC, "Matches a Cyrillic script character (\\p{IsCyrillic})"),
                Arguments.of(RegexSyntax.UNICODE_SCRIPT_ARABIC, "Matches an Arabic script character (\\p{IsArabic})"),
                Arguments.of(RegexSyntax.UNICODE_SCRIPT_HEBREW, "Matches a Hebrew script character (\\p{IsHebrew})"),
                Arguments.of(RegexSyntax.UNICODE_SCRIPT_HAN, "Matches a Han script character (\\p{IsHan})"),
                Arguments.of(RegexSyntax.UNICODE_SCRIPT_HIRAGANA, "Matches a Hiragana script character (\\p{IsHiragana})"),
                Arguments.of(RegexSyntax.UNICODE_SCRIPT_KATAKANA, "Matches a Katakana script character (\\p{IsKatakana})"),
                Arguments.of(RegexSyntax.UNICODE_SCRIPT_LATIN, "Matches a Latin script character (\\p{IsLatin})"),
                Arguments.of("A-Fa-f", "Matches a character from the custom range (A-Fa-f)")
        );
    }

    @ParameterizedTest(name = "Range {0} should translate properly")
    @MethodSource("provideRangesForTesting")
    void testFriendlyRangeTranslation(String rangeSyntax, String expectedText) {
        ResourceBundle.Control control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
        ResourceBundle bundle = ResourceBundle.getBundle("sift_messages", Locale.ENGLISH, control);
        ExplainerVisitor visitor = new ExplainerVisitor(bundle);

        visitor.visitClassRange(rangeSyntax);
        String output = visitor.getExplanation();

        assertEquals("└─ " + expectedText, output.trim());
    }
}