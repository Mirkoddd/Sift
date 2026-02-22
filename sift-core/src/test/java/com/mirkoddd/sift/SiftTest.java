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
package com.mirkoddd.sift;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static com.mirkoddd.sift.Sift.anywhere;
import static com.mirkoddd.sift.Sift.fromStart;
import static com.mirkoddd.sift.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mirkoddd.sift.dsl.SiftPattern;

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
            String regex = fromStart().exactly(3).digits().untilEnd().shake();

            assertEquals("^[0-9]{3}$", regex);
            assertRegexMatches(regex, "123");
            assertRegexDoesNotMatch(regex, "12");
            assertRegexDoesNotMatch(regex, "1234");
        }

        @Test
        @DisplayName("Should generate 'one or more' (+)")
        void oneOrMoreCheck() {
            String regex = anywhere().oneOrMore().letters().shake();

            assertEquals("[a-zA-Z]+", regex);
            assertRegexMatches(regex, "a");
            assertRegexMatches(regex, "Z");
            assertRegexDoesNotMatch(regex, "123");
        }

        @Test
        @DisplayName("Should generate 'optional' (?)")
        void optionalCheck() {
            String regex = fromStart().optional().digits().untilEnd().shake();

            assertEquals("^[0-9]?$", regex);
            assertRegexMatches(regex, "1");
            assertRegexMatches(regex, "");
            assertRegexDoesNotMatch(regex, "12");
        }

        @Test
        @DisplayName("Should generate 'zero or more' (*)")
        void anyTimesCheck() {
            String regex = fromStart().zeroOrMore().digits().untilEnd().shake();

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
            String regex = anywhere()
                    .oneOrMore().lettersLowercaseOnly()
                    .excluding('a', 'e', 'i', 'o', 'u')
                    .untilEnd()
                    .shake();

            assertEquals("[a-z&&[^aeiou]]+$", regex);
            assertRegexMatches(regex, "bcd");
            assertRegexDoesNotMatch(regex, "hello");
        }

        @Test
        @DisplayName("Should escape special chars inside class")
        void escapeInsideClass() {
            String regex = anywhere().digits().including('-', ']').shake();

            assertEquals("[0-9\\-\\]]", regex);
            assertRegexMatches(regex, "-");
            assertRegexMatches(regex, "]");
        }

        @Test
        @DisplayName("Should match uppercase letters only")
        void uppercaseOnly() {
            String regex = anywhere().oneOrMore().lettersUppercaseOnly().untilEnd().shake();

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
                            literal("dog")
                    ))
                    .shake();

            assertEquals("^(?:cat|dog)", regex);
            assertRegexMatches(regex, "cat");
            assertRegexMatches(regex, "dog");
            assertRegexDoesNotMatch(regex, "bird");
        }

        @Test
        @DisplayName("Should handle Capturing Groups")
        void capturing() {
            String regex = anywhere()
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
            String regex = anywhere().pattern(literal("1.50$")).shake();

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
            String regex = anywhere().any().including('a').excluding('b').shake();
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
            String regex = anywhere()
                    .oneOrMore()
                    .pattern(literal("abc"))
                    .shake();

            assertEquals("(?:abc)+", regex);
            assertRegexMatches(regex, "abcabc");
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
                    .untilEnd()
                    .shake();

            assertRegexMatches(regex, "User123");
            assertRegexDoesNotMatch(regex, "123User");
            assertRegexDoesNotMatch(regex, "Us");
        }

        @Test
        @DisplayName("Should handle Named Capturing Groups")
        void namedCapturing() {
            // Target: Extract "12345" from "Order: #12345" using group name "orderId"
            String regex = anywhere()
                    .pattern(literal("Order: #"))
                    .followedBy(capture("orderId",
                            anywhere().oneOrMore().digits()
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
            SiftPattern decimalPart = anywhere()
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
                    .untilEnd()
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
            String regex = anywhere()
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
            String regex = Sift.wordBoundary()
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
            String classRegex = anywhere().digits().withoutBacktracking().shake();
            assertEquals("[0-9]", classRegex);

            String charRegex = anywhere().character('a').withoutBacktracking().shake();
            assertEquals("a", charRegex);

            String patternRegex = anywhere().pattern(literal("abc")).withoutBacktracking().shake();
            assertEquals("abc", patternRegex);
        }

        @Test
        @DisplayName("Should prevent multiple possessive operators if called twice")
        void possessiveCalledTwice() {
            String classRegex = anywhere()
                    .oneOrMore().digits()
                    .withoutBacktracking().withoutBacktracking()
                    .shake();
            assertEquals("[0-9]++", classRegex);

            String charRegex = anywhere()
                    .oneOrMore().character('a')
                    .withoutBacktracking().withoutBacktracking()
                    .shake();
            assertEquals("a++", charRegex);
        }

        @Test
        @DisplayName("Should generate possessive quantifier for character classes (Lazy)")
        void possessiveOnClasses() {
            String regex = anywhere().oneOrMore().digits().withoutBacktracking().shake();
            assertEquals("[0-9]++", regex);
        }

        @Test
        @DisplayName("Should generate possessive quantifier for single characters (Eager)")
        void possessiveOnCharacters() {
            String regex = anywhere().zeroOrMore().character('a').withoutBacktracking().shake();
            assertEquals("a*+", regex);
        }

        @Test
        @DisplayName("Should generate possessive quantifier for patterns/groups (Eager)")
        void possessiveOnGroups() {
            String regex = anywhere().optional().pattern(literal("abc")).withoutBacktracking().shake();
            assertEquals("(?:abc)?+", regex);
        }

        @Test
        @DisplayName("Should generate possessive for other quantifiers on character classes (Lazy)")
        void possessiveOnOtherQuantifiersForClasses() {
            String zeroOrMoreRegex = anywhere().zeroOrMore().digits().withoutBacktracking().shake();
            assertEquals("[0-9]*+", zeroOrMoreRegex);

            String optionalRegex = anywhere().optional().digits().withoutBacktracking().shake();
            assertEquals("[0-9]?+", optionalRegex);

            String exactlyRegex = anywhere().exactly(3).digits().withoutBacktracking().shake();
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
                    .untilEnd().shake();

            assertEquals("^[0-9]+[0-9]$", greedyRegex);
            assertTrue("123".matches(greedyRegex), "Greedy should backtrack and match");

            // Case 2: POSSESSIVE (No backtracking).
            // ^[0-9]++[0-9]$ does NOT match "123". [0-9]++ consumes "123", never gives it back,
            // and the second [0-9] finds nothing. Immediate failure (No ReDoS).
            String possessiveRegex = fromStart()
                    .oneOrMore().digits().withoutBacktracking()
                    .then().exactly(1).digits()
                    .untilEnd().shake();

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

            String regex = anywhere().pattern(animal).shake();

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
                    .untilEnd().shake();

            assertTrue("abc".matches(normalRegex), "Normal OR should backtrack and find 'ab'");

            // CASE 2: Atomic (No Backtracking)
            // Pattern: (?>a|ab)c
            // The engine tries the first option "a". It works. LOCKS THE BOX.
            // Looks for "c", but finds "b". Fails.
            // NO BACKTRACKING: It will never try the second option "ab". Immediate total failure.
            String atomicRegex = fromStart()
                    .pattern(aOrAb.withoutBacktracking())
                    .followedBy('c')
                    .untilEnd().shake();

            assertFalse("abc".matches(atomicRegex), "Atomic group should NOT backtrack to 'ab'");
        }
    }
}