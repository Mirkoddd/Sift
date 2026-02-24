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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static com.mirkoddd.sift.core.Sift.fromAnywhere;
import static com.mirkoddd.sift.core.Sift.fromStart;
import static com.mirkoddd.sift.core.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mirkoddd.sift.core.dsl.SiftPattern;

/**
 * Test suite for Sift library.
 * Verifies both generated regex strings and actual matching behavior.
 */
@DisplayName("Sift Library Tests")
class SiftTest {

    private void assertRegexMatches(String regex, String input) {
        assertTrue(Pattern.compile(regex).matcher(input).find(),
                "Expected regex [" + regex + "] to match input [" + input + "]");
    }

    private void assertRegexDoesNotMatch(String regex, String input) {
        assertFalse(Pattern.compile(regex).matcher(input).find(),
                "Expected regex [" + regex + "] NOT to match input [" + input + "]");
    }

    @Nested
    @DisplayName("1. Basic Quantifiers & Types")
    class BasicQuantifiers {

        @Test
        @DisplayName("Should generate exact quantity matcher")
        void exactQuantity() {
            String regex = fromStart().exactly(3).digits().andNothingElse().shake();

            assertEquals("^[0-9]{3}$", regex);
            assertRegexMatches(regex, "123");
            assertRegexDoesNotMatch(regex, "12");
            assertRegexDoesNotMatch(regex, "1234");
        }

        @Test
        @DisplayName("Should generate 'one or more' (+)")
        void oneOrMoreCheck() {
            String regex = fromAnywhere().oneOrMore().letters().shake();

            assertEquals("[a-zA-Z]+", regex);
            assertRegexMatches(regex, "a");
            assertRegexMatches(regex, "Z");
            assertRegexDoesNotMatch(regex, "123");
        }

        @Test
        @DisplayName("Should generate 'optional' (?)")
        void optionalCheck() {
            String regex = fromStart().optional().digits().andNothingElse().shake();

            assertEquals("^[0-9]?$", regex);
            assertRegexMatches(regex, "1");
            assertRegexMatches(regex, "");
            assertRegexDoesNotMatch(regex, "12");
        }

        @Test
        @DisplayName("Should generate 'zero or more' (*)")
        void anyTimesCheck() {
            String regex = fromStart().zeroOrMore().digits().andNothingElse().shake();

            assertEquals("^[0-9]*$", regex);
            assertRegexMatches(regex, "12345");
            assertRegexMatches(regex, "");
            assertRegexDoesNotMatch(regex, "12a3");
        }
    }

    @Nested
    @DisplayName("2. Character Class Manipulation")
    class CharacterClasses {

        @Test
        @DisplayName("Should include specific characters")
        void including() {
            // Target: Hex color component (0-9, a-f, A-F)
            String regex = fromStart()
                    .oneOrMore().digits()
                    .including('a', 'b', 'c', 'd', 'e', 'f')
                    .including('A', 'B', 'C', 'D', 'E', 'F')
                    .shake();

            assertEquals("^[0-9abcdefABCDEF]+", regex);
            assertRegexMatches(regex, "1aF");
        }

        @Test
        @DisplayName("Should exclude specific characters (Subtraction)")
        void excluding() {
            // Target: Consonants only (letters minus vowels)
            String regex = fromAnywhere()
                    .oneOrMore().lettersLowercaseOnly()
                    .excluding('a', 'e', 'i', 'o', 'u')
                    .andNothingElse()
                    .shake();

            assertEquals("[a-z&&[^aeiou]]+$", regex);
            assertRegexMatches(regex, "bcd");
            assertRegexDoesNotMatch(regex, "hello");
        }

        @Test
        @DisplayName("Should escape special chars inside class")
        void escapeInsideClass() {
            String regex = fromAnywhere().digits().including('-', ']').shake();

            assertEquals("[0-9\\-\\]]", regex);
            assertRegexMatches(regex, "-");
            assertRegexMatches(regex, "]");
        }

        @Test
        @DisplayName("Should match uppercase letters only")
        void uppercaseOnly() {
            String regex = fromAnywhere().oneOrMore().lettersUppercaseOnly().andNothingElse().shake();

            assertEquals("[A-Z]+$", regex);
            assertRegexMatches(regex, "HELLO");
            assertRegexMatches(regex, "USA");
            assertRegexDoesNotMatch(regex, "hello");
            assertRegexDoesNotMatch(regex, "HeLLo");
        }
    }

    @Nested
    @DisplayName("3. Logic & Groups (SiftPatterns)")
    class LogicAndGroups {

        @Test
        @DisplayName("Should handle Logical OR (anyOf)")
        void anyOfCheck() {
            String regex = fromStart()
                    .pattern(anyOf(
                            literal("cat"),
                            literal("dog"),
                            literal("mouse")
                    ))
                    .shake();

            assertEquals("^(?:cat|dog|mouse)", regex);
            assertRegexMatches(regex, "cat");
            assertRegexMatches(regex, "dog");
            assertRegexDoesNotMatch(regex, "bird");
        }

        @Test
        @DisplayName("Should handle Capturing Groups")
        void capturing() {
            String regex = fromAnywhere()
                    .pattern(capture(
                            literal("ID-")
                    ))
                    .then()
                    .exactly(4).digits()
                    .shake();

            assertEquals("(ID-)[0-9]{4}", regex);
        }

        @Test
        @DisplayName("Should escape literals automatically")
        void literals() {
            String regex = fromAnywhere().pattern(literal("1.50$")).shake();

            assertEquals("1\\.50\\$", regex);
            assertRegexMatches(regex, "Cost is 1.50$");
        }
    }

    @Nested
    @DisplayName("4. Edge Cases & Safety")
    class EdgeCases {

        @Test
        @DisplayName("Should throw exception for negative quantifier")
        void negativeQuantifier() {
            assertThrows(IllegalArgumentException.class, () ->
                    fromStart().exactly(-1)
            );
        }

        @Test
        @DisplayName("Should throw exception for negative quantifier in atLeast")
        void negativeQuantifierAtLeast() {
            assertThrows(IllegalArgumentException.class, () ->
                    fromStart().atLeast(-1)
            );
        }

        @Test
        @DisplayName("Should gracefully ignore including/excluding when not building a class")
        void ignoreModifiersWhenNotBuildingClass() {
            String regex = fromAnywhere().any().including('a').excluding('b').shake();
            assertEquals(".", regex);
        }

        @Test
        @DisplayName("Should handle 'any' (dot) correctly")
        void anyChar() {
            String regex = fromStart().exactly(3).any().shake();
            assertEquals("^.{3}", regex);
        }

        @Test
        @DisplayName("Should correctly handle quantifiers on groups")
        void quantifierOnGroup() {
            String regex = fromAnywhere()
                    .oneOrMore()
                    .pattern(literal("abc"))
                    .shake();

            assertEquals("(?:abc)+", regex);
            assertRegexMatches(regex, "abcabc");
        }

        @Test
        @DisplayName("Should throw exceptions for invalid bounds in new quantifiers")
        void testQuantifierExceptions() {
            // atMost exceptions
            assertThrows(IllegalArgumentException.class, () -> Sift.fromAnywhere().atMost(-1));

            // between exceptions
            assertThrows(IllegalArgumentException.class, () -> Sift.fromAnywhere().between(-1, 5));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromAnywhere().between(2, -1));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromAnywhere().between(5, 2)); // min > max
        }

        @Test
        @DisplayName("Should correctly generate regex for wordChars, whitespace, optionally, and andNothingElse")
        void testNewTypesAndTerminals() {
            String regex = Sift.fromStart()
                    .atMost(3).wordCharacters()
                    .then()
                    .between(1, 2).whitespace()
                    .then()
                    .optional().nonWordCharacters()
                    .then()
                    .exactly(1).nonWhitespace()
                    .andNothingElse()
                    .shake();

            // fromStart = ^
            // atMost(3).wordChars() = [\w]{0,3}
            // between(1, 2).whitespace() = [\s]{1,2}
            // optional().nonWordChars() = [\W]?
            // exactly(1).nonWhitespace() = [\S]
            // andNothingElse = $
            assertEquals("^[\\w]{0,3}[\\s]{1,2}[\\W]?[\\S]$", regex);

            // --- VALID MATCHES ---
            // 3 words ("abc"), 2 spaces (" \t"), 1 non-word ("@"), 1 non-whitespace ("X")
            assertRegexMatches(regex, "abc \t@X");
            // 0 words, 2 spaces ("  "), 0 non-words, 1 non-whitespace ("X")
            assertRegexMatches(regex, "  X");
            // 1 word ("a"), 1 space (" "), 1 non-word ("@"), 1 non-whitespace ("X")
            assertRegexMatches(regex, "a @X");

            // --- INVALID MATCHES ---
            // 4 word chars (fails atMost(3))
            assertRegexDoesNotMatch(regex, "abcd  X");
            // 0 whitespaces (fails lower bound of between(1, 2))
            assertRegexDoesNotMatch(regex, "aX");
            // 4 whitespaces. Even if [\W]? consumes one space,
            // the 4th space will fail against the final [\S].
            assertRegexDoesNotMatch(regex, "a    X");
            // 2 non-word chars "@@" (fails optional())
            assertRegexDoesNotMatch(regex, "a  @@X");
            // Ends with a space (fails exactly(1).nonWhitespace() at the end)
            assertRegexDoesNotMatch(regex, "a  ");
        }

        @Test
        @DisplayName("Should allow modifiers on shorthand character classes")
        void testModifiersOnShorthands() {
            String regex = Sift.fromAnywhere()
                    .wordCharacters().excluding('_')
                    .shake();

            assertEquals("[\\w&&[^_]]", regex);

            // --- VALID MATCHES ---
            assertRegexMatches(regex, "a");
            assertRegexMatches(regex, "Z");
            assertRegexMatches(regex, "9");

            // --- INVALID MATCHES ---
            // Should NOT match the excluded character
            assertRegexDoesNotMatch(regex, "_");
            // Should NOT match symbols (since they aren't \w to begin with)
            assertRegexDoesNotMatch(regex, "@");
        }
    }

    @Nested
    @DisplayName("5. Real World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Scenario: Validating a simple Username")
        void usernameValidation() {
            String regex = fromStart()
                    .letters()
                    .then()
                    .atLeast(3).alphanumeric()
                    .andNothingElse()
                    .shake();

            assertRegexMatches(regex, "User123");
            assertRegexDoesNotMatch(regex, "123User");
            assertRegexDoesNotMatch(regex, "Us");
        }

        @Test
        @DisplayName("Should handle Named Capturing Groups")
        void namedCapturing() {
            // Target: Extract "12345" from "Order: #12345" using group name "orderId"
            String regex = fromAnywhere()
                    .pattern(literal("Order: #"))
                    .followedBy(capture("orderId",
                            fromAnywhere().oneOrMore().digits()
                    ))
                    .shake();

            assertEquals("Order: #(?<orderId>[0-9]+)", regex);

            Matcher m = Pattern.compile(regex).matcher("Status for Order: #9988");
            assertTrue(m.find());
            assertEquals("9988", m.group("orderId"));
        }

        @Test
        @DisplayName("Scenario: Extracting Prices")
        void priceExtraction() {
            // Nested pattern for decimals: \.[0-9]{2}
            SiftPattern decimalPart = fromAnywhere()
                    .character('.')
                    .then()
                    .exactly(2).digits();

            String regex = fromStart()
                    .character('$')
                    .then()
                    .oneOrMore().digits()

                    .then()
                    .optional()
                    .pattern(decimalPart)
                    .andNothingElse()
                    .shake();

            assertEquals("^\\$[0-9]+(?:\\.[0-9]{2})?$", regex);

            assertRegexMatches(regex, "$10");
            assertRegexMatches(regex, "$10.99");
            assertRegexDoesNotMatch(regex, "$10.");
            assertRegexDoesNotMatch(regex, "10.99");
        }

        @ParameterizedTest
        @ValueSource(strings = {"test.email", "hello", "123"})
        @DisplayName("Scenario: Custom Domain logic")
        void parameterizedCheck(String input) {
            String regex = fromAnywhere()
                    .alphanumeric()
                    .followedBy('.')
                    .then()
                    .alphanumeric()
                    .shake();

            Pattern p = Pattern.compile(regex);
            if (input.contains(".")) {
                assertTrue(p.matcher(input).find());
            } else {
                assertFalse(p.matcher(input).find());
            }
        }
    }

    @Nested
    @DisplayName("6. Anchors & Boundaries")
    class AnchorsAndBoundaries {

        @Test
        @DisplayName("Should respect Word Boundaries (Static & Instance)")
        void wordBoundaries() {
            // Matches exact word "cat" (not "catalog" or "scatter")
            String regex = Sift.fromWordBoundary()
                    .followedBy(literal("cat"))
                    .wordBoundary()
                    .shake();

            assertEquals("\\bcat\\b", regex);
            assertRegexMatches(regex, "cat");
            assertRegexMatches(regex, "I have a cat.");
            assertRegexDoesNotMatch(regex, "catalog");
            assertRegexDoesNotMatch(regex, "scatter");
        }
    }

    @Nested
    @DisplayName("7. Security & Performance (Anti-ReDoS)")
    class SecurityAndPerformance {

        @Test
        @DisplayName("Should safely ignore withoutBacktracking if no quantifier exists")
        void possessiveWithoutQuantifier() {
            String classRegex = fromAnywhere().digits().withoutBacktracking().shake();
            assertEquals("[0-9]", classRegex);

            String charRegex = fromAnywhere().character('a').withoutBacktracking().shake();
            assertEquals("a", charRegex);

            String patternRegex = fromAnywhere().pattern(literal("abc")).withoutBacktracking().shake();
            assertEquals("abc", patternRegex);
        }

        @Test
        @DisplayName("Should prevent multiple possessive operators if called twice")
        void possessiveCalledTwice() {
            String classRegex = fromAnywhere()
                    .oneOrMore().digits()
                    .withoutBacktracking().withoutBacktracking()
                    .shake();
            assertEquals("[0-9]++", classRegex);

            String charRegex = fromAnywhere()
                    .oneOrMore().character('a')
                    .withoutBacktracking().withoutBacktracking()
                    .shake();
            assertEquals("a++", charRegex);
        }

        @Test
        @DisplayName("Should generate possessive quantifier for character classes (Lazy)")
        void possessiveOnClasses() {
            String regex = fromAnywhere().oneOrMore().digits().withoutBacktracking().shake();
            assertEquals("[0-9]++", regex);
        }

        @Test
        @DisplayName("Should generate possessive quantifier for single characters (Eager)")
        void possessiveOnCharacters() {
            String regex = fromAnywhere().zeroOrMore().character('a').withoutBacktracking().shake();
            assertEquals("a*+", regex);
        }

        @Test
        @DisplayName("Should generate possessive quantifier for patterns/groups (Eager)")
        void possessiveOnGroups() {
            String regex = fromAnywhere().optional().pattern(literal("abc")).withoutBacktracking().shake();
            assertEquals("(?:abc)?+", regex);
        }

        @Test
        @DisplayName("Should generate possessive for other quantifiers on character classes (Lazy)")
        void possessiveOnOtherQuantifiersForClasses() {
            String zeroOrMoreRegex = fromAnywhere().zeroOrMore().digits().withoutBacktracking().shake();
            assertEquals("[0-9]*+", zeroOrMoreRegex);

            String optionalRegex = fromAnywhere().optional().digits().withoutBacktracking().shake();
            assertEquals("[0-9]?+", optionalRegex);

            String exactlyRegex = fromAnywhere().exactly(3).digits().withoutBacktracking().shake();
            assertEquals("[0-9]{3}+", exactlyRegex);
        }

        @Test
        @DisplayName("Should actually prevent backtracking in the Java Regex engine")
        void possessiveBehaviorCheck() {
            // Case 1: GREEDY (Default).
            // ^[0-9]+[0-9]$ matches "123" because [0-9]+ consumes "123", fails to match the end,
            // backtracks (spits out the "3") and allows the second [0-9] to match.
            String greedyRegex = fromStart()
                    .oneOrMore().digits()
                    .then().exactly(1).digits()
                    .andNothingElse().shake();

            assertEquals("^[0-9]+[0-9]$", greedyRegex);
            assertTrue("123".matches(greedyRegex), "Greedy should backtrack and match");

            // Case 2: POSSESSIVE (No backtracking).
            // ^[0-9]++[0-9]$ does NOT match "123". [0-9]++ consumes "123", never gives it back,
            // and the second [0-9] finds nothing. Immediate failure (No ReDoS).
            String possessiveRegex = fromStart()
                    .oneOrMore().digits().withoutBacktracking()
                    .then().exactly(1).digits()
                    .andNothingElse().shake();

            assertEquals("^[0-9]++[0-9]$", possessiveRegex);
            assertFalse("123".matches(possessiveRegex), "Possessive should NOT backtrack and fail");
        }

        @Test
        @DisplayName("Should generate atomic group for standalone SiftPatterns")
        void atomicGroupSyntax() {
            // A logical OR wrapped in anti-backtracking protection
            SiftPattern cat = literal("cat");
            SiftPattern dog = literal("dog");
            SiftPattern animal = anyOf(cat, dog).withoutBacktracking();

            String regex = fromAnywhere().pattern(animal).shake();

            // Expects the atomic group (?>...) around the non-capturing group (?:...) of the anyOf
            assertEquals("(?>(?:cat|dog))", regex);
        }

        @Test
        @DisplayName("Should actually prevent backtracking using Atomic Groups")
        void atomicGroupBehaviorCheck() {
            SiftPattern a = literal("a");
            SiftPattern ab = literal("ab");
            SiftPattern aOrAb = anyOf(a, ab);

            // CASE 1: Normal (Greedy / Backtracking enabled)
            // Pattern: (?:a|ab)c
            // The engine tries the first option "a". Then looks for "c", but finds "b". Fails.
            // BACKTRACKS: Goes back to the OR, tries the second option "ab". Looks for "c", finds it! MATCH!
            String normalRegex = fromStart()
                    .pattern(aOrAb)
                    .followedBy('c')
                    .andNothingElse().shake();

            assertTrue("abc".matches(normalRegex), "Normal OR should backtrack and find 'ab'");

            // CASE 2: Atomic (No Backtracking)
            // Pattern: (?>a|ab)c
            // The engine tries the first option "a". It works. LOCKS THE BOX.
            // Looks for "c", but finds "b". Fails.
            // NO BACKTRACKING: It will never try the second option "ab". Immediate total failure.
            String atomicRegex = fromStart()
                    .pattern(aOrAb.withoutBacktracking())
                    .followedBy('c')
                    .andNothingElse().shake();

            assertFalse("abc".matches(atomicRegex), "Atomic group should NOT backtrack to 'ab'");
        }
    }

    @Nested
    @DisplayName("8. Extended & Unicode Character Classes")
    class ExtendedAndUnicodeTypes {

        @Test
        @DisplayName("Should properly restrict and negate ASCII classes")
        void asciiNegations() {
            // nonLetters: [^a-zA-Z]
            String nonLettersRegex = Sift.fromStart().exactly(1).nonLetters().andNothingElse().shake();
            assertEquals("^[^a-zA-Z]$", nonLettersRegex);
            assertRegexMatches(nonLettersRegex, "1");
            assertRegexMatches(nonLettersRegex, "!");
            assertRegexMatches(nonLettersRegex, "è"); // Valid! 'è' is not an ASCII letter
            assertRegexDoesNotMatch(nonLettersRegex, "a");
            assertRegexDoesNotMatch(nonLettersRegex, "Z");

            // nonAlphanumeric: [^a-zA-Z0-9]
            String nonAlphaRegex = Sift.fromStart().exactly(1).nonAlphanumeric().andNothingElse().shake();
            assertEquals("^[^a-zA-Z0-9]$", nonAlphaRegex);
            assertRegexMatches(nonAlphaRegex, " ");
            assertRegexMatches(nonAlphaRegex, "!");
            assertRegexDoesNotMatch(nonAlphaRegex, "1");
            assertRegexDoesNotMatch(nonAlphaRegex, "A");

            // nonDigits: [\D]
            String nonDigitsRegex = Sift.fromStart().exactly(1).nonDigits().andNothingElse().shake();
            assertEquals("^[\\D]$", nonDigitsRegex);
            assertRegexMatches(nonDigitsRegex, "A");
            assertRegexMatches(nonDigitsRegex, "!");
            assertRegexMatches(nonDigitsRegex, "è"); // Valid! 'è' is not an ASCII digit
            assertRegexDoesNotMatch(nonDigitsRegex, "5");
            assertRegexDoesNotMatch(nonDigitsRegex, "0");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Letters and Case Specifics")
        void unicodeLettersAndCases() {
            // unicodeLetters: [\p{L}]
            String uniLetters = Sift.fromStart().oneOrMore().unicodeLetters().andNothingElse().shake();
            assertEquals("^[\\p{L}]+$", uniLetters);
            assertRegexMatches(uniLetters, "Aimé");
            assertRegexMatches(uniLetters, "Müller");
            assertRegexDoesNotMatch(uniLetters, "123");

            // nonUnicodeLetters: [\P{L}]
            String nonUniLetters = Sift.fromStart().oneOrMore().nonUnicodeLetters().andNothingElse().shake();
            assertEquals("^[\\P{L}]+$", nonUniLetters);
            assertRegexMatches(nonUniLetters, "123 !!");
            assertRegexDoesNotMatch(nonUniLetters, "è");

            // unicodeLettersUppercaseOnly: [\p{Lu}]
            String uniUpper = Sift.fromStart().oneOrMore().unicodeLettersUppercaseOnly().andNothingElse().shake();
            assertEquals("^[\\p{Lu}]+$", uniUpper);
            assertRegexMatches(uniUpper, "È");
            assertRegexMatches(uniUpper, "Ñ");
            assertRegexDoesNotMatch(uniUpper, "è"); // Fails because lowercase
            assertRegexDoesNotMatch(uniUpper, "A1");

            // unicodeLettersLowercaseOnly: [\p{Ll}]
            String uniLower = Sift.fromStart().oneOrMore().unicodeLettersLowercaseOnly().andNothingElse().shake();
            assertEquals("^[\\p{Ll}]+$", uniLower);
            assertRegexMatches(uniLower, "è");
            assertRegexMatches(uniLower, "ñ");
            assertRegexDoesNotMatch(uniLower, "È");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Digits and Whitespaces")
        void unicodeDigitsAndWhitespace() {
            // unicodeDigits: [\p{Nd}]
            String uniDigits = Sift.fromStart().exactly(1).unicodeDigits().andNothingElse().shake();
            assertEquals("^[\\p{Nd}]$", uniDigits);
            assertRegexMatches(uniDigits, "5");
            assertRegexMatches(uniDigits, "٣"); // Arabic-Indic digit 3
            assertRegexDoesNotMatch(uniDigits, "a");

            // nonUnicodeDigits: [\P{Nd}]
            String nonUniDigits = Sift.fromStart().exactly(1).nonUnicodeDigits().andNothingElse().shake();
            assertEquals("^[\\P{Nd}]$", nonUniDigits);
            assertRegexMatches(nonUniDigits, "A");
            assertRegexDoesNotMatch(nonUniDigits, "٣");

            // unicodeWhitespace: [\p{IsWhite_Space}]
            String uniSpace = Sift.fromStart().exactly(1).unicodeWhitespace().andNothingElse().shake();
            assertEquals("^[\\p{IsWhite_Space}]$", uniSpace);
            assertRegexMatches(uniSpace, " ");
            assertRegexMatches(uniSpace, "\u00A0"); // Non-breaking space
            assertRegexDoesNotMatch(uniSpace, "a");

            // nonUnicodeWhitespace: [\P{IsWhite_Space}]
            String nonUniSpace = Sift.fromStart().exactly(1).nonUnicodeWhitespace().andNothingElse().shake();
            assertEquals("^[\\P{IsWhite_Space}]$", nonUniSpace);
            assertRegexMatches(nonUniSpace, "a");
            assertRegexDoesNotMatch(nonUniSpace, "\u00A0");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Word Characters and Alphanumeric")
        void unicodeAlphanumericAndWords() {
            // unicodeAlphanumeric: [\p{L}\p{Nd}]
            String uniAlpha = Sift.fromStart().exactly(1).unicodeAlphanumeric().andNothingElse().shake();
            assertEquals("^[\\p{L}\\p{Nd}]$", uniAlpha);
            assertRegexMatches(uniAlpha, "è");
            assertRegexMatches(uniAlpha, "٣");
            assertRegexDoesNotMatch(uniAlpha, "_"); // Does NOT include underscore

            // nonUnicodeAlphanumeric: [^\p{L}\p{Nd}]
            String nonUniAlpha = Sift.fromStart().exactly(1).nonUnicodeAlphanumeric().andNothingElse().shake();
            assertEquals("^[^\\p{L}\\p{Nd}]$", nonUniAlpha);
            assertRegexMatches(nonUniAlpha, "!");
            assertRegexMatches(nonUniAlpha, " ");
            assertRegexMatches(nonUniAlpha, "_"); // Underscore is NOT alphanumeric, so it matches the negation
            assertRegexDoesNotMatch(nonUniAlpha, "è");
            assertRegexDoesNotMatch(nonUniAlpha, "٣");

            // unicodeWordCharacters: [\p{L}\p{Nd}_]
            String uniWord = Sift.fromStart().exactly(1).unicodeWordCharacters().andNothingElse().shake();
            assertEquals("^[\\p{L}\\p{Nd}_]$", uniWord);
            assertRegexMatches(uniWord, "è");
            assertRegexMatches(uniWord, "٣");
            assertRegexMatches(uniWord, "_"); // Includes underscore
            assertRegexDoesNotMatch(uniWord, " ");

            // nonUnicodeWordCharacters: [^\p{L}\p{Nd}_]
            String nonUniWord = Sift.fromStart().exactly(1).nonUnicodeWordCharacters().andNothingElse().shake();
            assertEquals("^[^\\p{L}\\p{Nd}_]$", nonUniWord);
            assertRegexMatches(nonUniWord, "!");
            assertRegexMatches(nonUniWord, " ");
            assertRegexDoesNotMatch(nonUniWord, "è");
        }
    }

    @Nested
    @DisplayName("9. Global Inline Flags")
    class GlobalInlineFlags {

        @Test
        @DisplayName("Should correctly prepend a single flag (Case Insensitive)")
        void singleFlag() {
            String regex = Sift.filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
                    .fromStart()
                    .exactly(5).lettersUppercaseOnly()
                    .andNothingElse()
                    .shake();

            assertEquals("(?i)^[A-Z]{5}$", regex);

            // Should match both due to (?i)
            assertRegexMatches(regex, "PROMO");
            assertRegexMatches(regex, "promo");
            assertRegexMatches(regex, "PrOmO");

            // Should fail on invalid characters or length
            assertRegexDoesNotMatch(regex, "PROMO1");
            assertRegexDoesNotMatch(regex, "PROM");
        }

        @Test
        @DisplayName("Should correctly combine multiple flags")
        void multipleFlags() {
            String regex = Sift.filteringWith(SiftGlobalFlag.CASE_INSENSITIVE, SiftGlobalFlag.MULTILINE)
                    .fromStart()
                    .oneOrMore().letters()
                    .andNothingElse()
                    .shake();

            // Should concatenate the symbols inside the (?...) block
            assertEquals("(?im)^[a-zA-Z]+$", regex);

            // MULTILINE (?m) changes ^ and $ to match start/end of lines, not just the whole string
            String multilineInput = "HELLO\nworld\nTEST";

            Matcher matcher = Pattern.compile(regex).matcher(multilineInput);
            int matchCount = 0;
            while (matcher.find()) {
                matchCount++;
            }
            // It should find 3 distinct matches, one for each line
            assertEquals(3, matchCount, "Should match 3 separate lines");
        }

        @Test
        @DisplayName("Should handle DOTALL mode correctly")
        void dotallFlag() {
            String regex = Sift.filteringWith(SiftGlobalFlag.DOTALL)
                    .fromStart()
                    .exactly(3).any()
                    .andNothingElse()
                    .shake();

            assertEquals("(?s)^.{3}$", regex);

            // Without (?s), .any() does NOT match \n. With (?s), it does!
            assertRegexMatches(regex, "A\nB");
        }

        @Test
        @DisplayName("Should apply flags when starting from anywhere")
        void flagsFromAnywhere() {
            String regex = Sift.filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
                    .fromAnywhere()
                    .exactly(4).lettersUppercaseOnly()
                    .shake();

            assertEquals("(?i)[A-Z]{4}", regex);

            assertRegexMatches(regex, "some TEST string");
            assertRegexMatches(regex, "some test string");
        }

        @Test
        @DisplayName("Should apply flags when starting from a word boundary")
        void flagsFromWordBoundary() {
            String regex = Sift.filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
                    .fromWordBoundary()
                    .then()
                    .exactly(5).letters()
                    .shake();

            assertEquals("(?i)\\b[a-zA-Z]{5}", regex);

            assertRegexMatches(regex, "fatal ERROR occurred");
            assertRegexMatches(regex, "fatal error occurred");
        }
    }

    @Test
    @DisplayName("Utility class Sift should have a private constructor and be instantiable via reflection for coverage")
    void privateConstructorCoverage() throws Exception {
        java.lang.reflect.Constructor<Sift> constructor = Sift.class.getDeclaredConstructor();

        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                "Sift constructor must be private");

        constructor.setAccessible(true);
        constructor.newInstance();
    }
}