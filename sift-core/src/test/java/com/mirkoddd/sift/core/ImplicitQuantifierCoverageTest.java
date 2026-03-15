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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ensures that omitting the quantifier in the Sift DSL implicitly defaults
 * to exactly(1) for every single type definition available in the engine.
 * This guarantees 100% coverage on the internal SiftFixedType delegation.
 */
class ImplicitQuantifierCoverageTest {

    /**
     * Helper method to assert that the implicit path generates the exact same
     * bytecode/string as the explicitly quantified path.
     *
     * @param implicitRegex The regex generated without specifying a quantifier.
     * @param explicitRegex The regex generated using .exactly(1).
     */
    private void assertImplicitEqualsExplicit(String implicitRegex, String explicitRegex) {
        assertEquals(explicitRegex, implicitRegex,
                "The implicit quantifier generation did not match exactly(1)");
    }

    @Test
    @DisplayName("Implicit quantifier matches exactly(1) for core types, literals, and control characters")
    void verifyCoreTypes() {
        assertImplicitEqualsExplicit(
                Sift.fromStart().anyCharacter().shake(),
                Sift.fromStart().exactly(1).anyCharacter().shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().character('A').shake(),
                Sift.fromStart().exactly(1).character('A').shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().of(SiftPatterns.literal("test")).shake(),
                Sift.fromStart().exactly(1).of(SiftPatterns.literal("test")).shake()
        );

        // Control characters
        assertImplicitEqualsExplicit(
                Sift.fromStart().newline().shake(),
                Sift.fromStart().exactly(1).newline().shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().carriageReturn().shake(),
                Sift.fromStart().exactly(1).carriageReturn().shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().tab().shake(),
                Sift.fromStart().exactly(1).tab().shake()
        );
    }

    @Test
    @DisplayName("Implicit quantifier matches exactly(1) for all digit variants")
    void verifyDigitVariants() {
        assertImplicitEqualsExplicit(
                Sift.fromStart().digits().shake(),
                Sift.fromStart().exactly(1).digits().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonDigits().shake(),
                Sift.fromStart().exactly(1).nonDigits().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().digitsUnicode().shake(),
                Sift.fromStart().exactly(1).digitsUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonDigitsUnicode().shake(),
                Sift.fromStart().exactly(1).nonDigitsUnicode().shake()
        );
    }

    @Test
    @DisplayName("Implicit quantifier matches exactly(1) for all letter variants")
    void verifyLetterVariants() {
        assertImplicitEqualsExplicit(
                Sift.fromStart().letters().shake(),
                Sift.fromStart().exactly(1).letters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonLetters().shake(),
                Sift.fromStart().exactly(1).nonLetters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().lowerCaseLetters().shake(),
                Sift.fromStart().exactly(1).lowerCaseLetters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().upperCaseLetters().shake(),
                Sift.fromStart().exactly(1).upperCaseLetters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().lettersUnicode().shake(),
                Sift.fromStart().exactly(1).lettersUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonLettersUnicode().shake(),
                Sift.fromStart().exactly(1).nonLettersUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().lowerCaseLettersUnicode().shake(),
                Sift.fromStart().exactly(1).lowerCaseLettersUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().upperCaseLettersUnicode().shake(),
                Sift.fromStart().exactly(1).upperCaseLettersUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().caselessLettersUnicode().shake(),
                Sift.fromStart().exactly(1).caselessLettersUnicode().shake()
        );
    }

    @Test
    @DisplayName("Implicit quantifier matches exactly(1) for all alphanumeric variants")
    void verifyAlphanumericVariants() {
        assertImplicitEqualsExplicit(
                Sift.fromStart().alphanumeric().shake(),
                Sift.fromStart().exactly(1).alphanumeric().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonAlphanumeric().shake(),
                Sift.fromStart().exactly(1).nonAlphanumeric().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().alphanumericUnicode().shake(),
                Sift.fromStart().exactly(1).alphanumericUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonAlphanumericUnicode().shake(),
                Sift.fromStart().exactly(1).nonAlphanumericUnicode().shake()
        );
    }

    @Test
    @DisplayName("Implicit quantifier matches exactly(1) for word and whitespace variants")
    void verifyWordAndWhitespaceVariants() {
        // Word Characters
        assertImplicitEqualsExplicit(
                Sift.fromStart().wordCharacters().shake(),
                Sift.fromStart().exactly(1).wordCharacters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonWordCharacters().shake(),
                Sift.fromStart().exactly(1).nonWordCharacters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().wordCharactersUnicode().shake(),
                Sift.fromStart().exactly(1).wordCharactersUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonWordCharactersUnicode().shake(),
                Sift.fromStart().exactly(1).nonWordCharactersUnicode().shake()
        );

        // Whitespaces
        assertImplicitEqualsExplicit(
                Sift.fromStart().whitespace().shake(),
                Sift.fromStart().exactly(1).whitespace().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonWhitespace().shake(),
                Sift.fromStart().exactly(1).nonWhitespace().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().whitespaceUnicode().shake(),
                Sift.fromStart().exactly(1).whitespaceUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonWhitespaceUnicode().shake(),
                Sift.fromStart().exactly(1).nonWhitespaceUnicode().shake()
        );
    }

    @Test
    @DisplayName("Implicit quantifier matches exactly(1) for extended utility classes")
    void verifyExtendedUtilityClasses() {
        assertImplicitEqualsExplicit(
                Sift.fromStart().hexDigits().shake(),
                Sift.fromStart().exactly(1).hexDigits().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().punctuation().shake(),
                Sift.fromStart().exactly(1).punctuation().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().punctuationUnicode().shake(),
                Sift.fromStart().exactly(1).punctuationUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().blank().shake(),
                Sift.fromStart().exactly(1).blank().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().blankUnicode().shake(),
                Sift.fromStart().exactly(1).blankUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().symbolsUnicode().shake(),
                Sift.fromStart().exactly(1).symbolsUnicode().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().whitespaceHorizontal().shake(),
                Sift.fromStart().exactly(1).whitespaceHorizontal().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().whitespaceVertical().shake(),
                Sift.fromStart().exactly(1).whitespaceVertical().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().linebreakUnicode().shake(),
                Sift.fromStart().exactly(1).linebreakUnicode().shake()
        );
    }

    @Test
    @DisplayName("Implicit quantifier allows character class modifiers without explicit quantification")
    void verifyCharacterClassModifiersOnImplicitTypes() {
        // Ensures that methods like including() and excluding() work seamlessly
        // on implicit types and produce the same result as exactly(1)
        assertImplicitEqualsExplicit(
                Sift.fromStart().letters().including('A', 'B').shake(),
                Sift.fromStart().exactly(1).letters().including('A', 'B').shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().digits().excluding('0').shake(),
                Sift.fromStart().exactly(1).digits().excluding('0').shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().hexDigits().excluding('a', 'A').shake(),
                Sift.fromStart().exactly(1).hexDigits().excluding('a', 'A').shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().punctuation().including('-').shake(),
                Sift.fromStart().exactly(1).punctuation().including('-').shake()
        );
    }
}