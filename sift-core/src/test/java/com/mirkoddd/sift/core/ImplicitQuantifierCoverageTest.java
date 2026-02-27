package com.mirkoddd.sift.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ensures that omitting the quantifier in the Sift DSL implicitly defaults
 * to exactly(1) for every single type definition available in the engine.
 * This guarantees 100% coverage on the internal FixedType delegation.
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
    @DisplayName("Implicit quantifier matches exactly(1) for core types and literals")
    void verifyCoreTypes() {
        assertImplicitEqualsExplicit(
                Sift.fromStart().any().shake(),
                Sift.fromStart().exactly(1).any().shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().character('A').shake(),
                Sift.fromStart().exactly(1).character('A').shake()
        );

        assertImplicitEqualsExplicit(
                Sift.fromStart().pattern(SiftPatterns.literal("test")).shake(),
                Sift.fromStart().exactly(1).pattern(SiftPatterns.literal("test")).shake()
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
                Sift.fromStart().unicodeDigits().shake(),
                Sift.fromStart().exactly(1).unicodeDigits().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonUnicodeDigits().shake(),
                Sift.fromStart().exactly(1).nonUnicodeDigits().shake()
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
                Sift.fromStart().lettersLowercaseOnly().shake(),
                Sift.fromStart().exactly(1).lettersLowercaseOnly().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().lettersUppercaseOnly().shake(),
                Sift.fromStart().exactly(1).lettersUppercaseOnly().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().unicodeLetters().shake(),
                Sift.fromStart().exactly(1).unicodeLetters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonUnicodeLetters().shake(),
                Sift.fromStart().exactly(1).nonUnicodeLetters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().unicodeLettersLowercaseOnly().shake(),
                Sift.fromStart().exactly(1).unicodeLettersLowercaseOnly().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().unicodeLettersUppercaseOnly().shake(),
                Sift.fromStart().exactly(1).unicodeLettersUppercaseOnly().shake()
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
                Sift.fromStart().unicodeAlphanumeric().shake(),
                Sift.fromStart().exactly(1).unicodeAlphanumeric().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonUnicodeAlphanumeric().shake(),
                Sift.fromStart().exactly(1).nonUnicodeAlphanumeric().shake()
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
                Sift.fromStart().unicodeWordCharacters().shake(),
                Sift.fromStart().exactly(1).unicodeWordCharacters().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonUnicodeWordCharacters().shake(),
                Sift.fromStart().exactly(1).nonUnicodeWordCharacters().shake()
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
                Sift.fromStart().unicodeWhitespace().shake(),
                Sift.fromStart().exactly(1).unicodeWhitespace().shake()
        );
        assertImplicitEqualsExplicit(
                Sift.fromStart().nonUnicodeWhitespace().shake(),
                Sift.fromStart().exactly(1).nonUnicodeWhitespace().shake()
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
    }
}