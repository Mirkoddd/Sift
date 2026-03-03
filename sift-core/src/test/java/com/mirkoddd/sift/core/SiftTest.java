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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static com.mirkoddd.sift.core.Sift.filteringWith;
import static com.mirkoddd.sift.core.Sift.fromAnywhere;
import static com.mirkoddd.sift.core.Sift.fromStart;
import static com.mirkoddd.sift.core.SiftGlobalFlag.CASE_INSENSITIVE;
import static com.mirkoddd.sift.core.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mirkoddd.sift.core.dsl.ConnectorStep;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.dsl.VariableConnectorStep;

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
                    .oneOrMore().lowercaseLetters()
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
            String regex = fromAnywhere().oneOrMore().uppercaseLetters().andNothingElse().shake();

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
        @DisplayName("Should handle 'any' (dot) correctly")
        void anyChar() {
            String regex = fromStart().exactly(3).anyCharacter().shake();
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
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().atMost(-1));

            // between exceptions
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().between(-1, 5));
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().between(2, -1));
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().between(5, 2)); // min > max
        }

        @Test
        @DisplayName("atMost() and between() should exhaustively validate boundaries and throw IllegalArgumentException")
        void testQuantifierBoundaryGuards() {
            // --- atMost() exhaustive branches ---
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().atMost(0),
                    "Should block atMost(0)");
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().atMost(-1),
                    "Should block negative max values in atMost()");

            // --- between() exhaustive branches ---
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(0, 0),
                    "Should block between(0, 0) specifically");

            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(-1, 5),
                    "Should block negative min values");

            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(2, 0),
                    "Should block max <= 0 (e.g., 0 with a valid min)");

            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(2, -1),
                    "Should block max <= 0 (e.g., negative max with a valid min)");

            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(5, 2),
                    "Should block when min is strictly greater than max");

            assertDoesNotThrow(() -> Sift.fromStart().between(0, 5),
                    "Should successfully allow min=0 as long as max > 0 (e.g., {0,5})");
        }

        @Test
        @DisplayName("Should correctly generate regex for wordChars, whitespace, optionally, and andNothingElse")
        void testNewTypesAndTerminals() {
            String regex = fromStart()
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
            String regex = fromAnywhere()
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

        @Test
        @DisplayName("Should skip possessive modifier if quantifier already ends with + (Coverage Booster)")
        void coverageDuplicatePossessiveCondition() {
            VariableConnectorStep step1 = Sift.fromAnywhere().zeroOrMore().digits();

            ConnectorStep step2 = step1.withoutBacktracking();

            ((VariableConnectorStep) step2).withoutBacktracking();

            assertEquals("[0-9]*+", step2.shake());
        }

        @Test
        @DisplayName("Should throw NullPointerException when a null flag is passed in varargs")
        void filteringWith_withNullElementInVarargs_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                    filteringWith(CASE_INSENSITIVE, (SiftGlobalFlag) null)
            );
        }

        @Test
        @DisplayName("Should throw IllegalStateException when defining duplicate group names")
        void throwOnDuplicateGroupNames() {
            String duplicateName = "user";

            SiftPattern digits = fromAnywhere().digits();
            SiftPattern letters = fromAnywhere().letters();

            NamedCapture group1 = capture(duplicateName, digits);
            NamedCapture group2 = capture(duplicateName, letters);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    Sift.fromAnywhere()
                            .namedCapture(group1)
                            .then()
                            .namedCapture(group2)
                            .shake()
            );

            assertTrue(exception.getMessage().contains(duplicateName));
        }

        @Test
        @DisplayName("Should throw NullPointerException when a null pattern is passed in followedBy varargs")
        void followedByNullVarargs() {
            SiftPattern validPattern = SiftPatterns.literal("valid");

            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                    Sift.fromAnywhere().letters().followedBy(validPattern, validPattern, null, validPattern)
            );

            assertTrue(exception.getMessage().contains("cannot be null"));
        }

        @Test
        @DisplayName("shake() should be idempotent and cache the generated regex")
        void testShakeIdempotency() {
            SiftPattern pattern =
                    Sift.fromStart().oneOrMore().digits();

            String firstShake = pattern.shake();
            String secondShake = pattern.shake();

            assertEquals(firstShake, secondShake,
                    "Multiple calls to shake() should return the exact same string");

            assertEquals("^[0-9]+", firstShake,
                    "The cached regex must perfectly match the expected output");
        }

        @Test
        @DisplayName("sieve() should return a cached compiled Pattern that correctly matches the input")
        void testSieveReturnsCachedPattern() {
            SiftPattern pattern = Sift.fromStart().exactly(3).digits();

            java.util.regex.Pattern firstPattern = pattern.sieve();

            assertNotNull(firstPattern, "sieve() should not return a null Pattern");
            assertEquals("^[0-9]{3}", firstPattern.pattern(), "The compiled pattern should match the DSL logic");

            assertTrue(firstPattern.matcher("123").matches(), "The Pattern should match '123'");
            assertFalse(firstPattern.matcher("12").matches(), "The Pattern should NOT match '12'");
            assertFalse(firstPattern.matcher("abc").matches(), "The Pattern should NOT match letters");

            java.util.regex.Pattern secondPattern = pattern.sieve();

            assertSame(firstPattern, secondPattern,
                    "sieve() should return the exactly same Pattern instance from cache on subsequent calls");
        }

        @Test
        @DisplayName("equals() and hashCode() should evaluate structural identity based on the regex")
        void testPatternIdentity() {
            com.mirkoddd.sift.core.dsl.SiftPattern patternA =
                    Sift.fromStart().optional().letters();

            com.mirkoddd.sift.core.dsl.SiftPattern patternB =
                    Sift.fromStart().optional().letters();

            com.mirkoddd.sift.core.dsl.SiftPattern patternDifferent =
                    Sift.fromStart().optional().digits();

            assertEquals(patternA, patternB, "Identical patterns should be equal");
            assertEquals(patternA.hashCode(), patternB.hashCode(), "Identical patterns should have the same hash code");

            assertNotEquals(patternA, patternDifferent, "Different patterns should not be equal");

            assertEquals(patternA, patternA, "A pattern should be equal to itself");

            assertNotEquals(patternA, null, "A pattern should never be equal to null");

            assertNotEquals(patternA, "^[a-zA-Z]??", "A pattern should not be equal to an object of a different class");
        }

        @Test
        @DisplayName("toString() should return the generated pattern string")
        void testPatternToString() {
            SiftPattern pattern = Sift.fromStart().exactly(3).digits();

            assertEquals("^[0-9]{3}", pattern.toString(),
                    "toString() should expose the raw regex pattern");
            assertEquals(pattern.shake(), pattern.toString(),
                    "toString() must rely on the cached shake() output");
        }

        @Test
        @DisplayName("shake() should perform fail-fast validation and throw IllegalStateException on invalid regex")
        void testShakeFailFastValidation() {
            SiftPattern malformedPattern = () -> "(?unclosedGroup";

            SiftPattern pattern = Sift.fromStart().pattern(malformedPattern);

            IllegalStateException exception = assertThrows(IllegalStateException.class, pattern::shake,
                    "shake() should throw an IllegalStateException if the generated regex is physically invalid");

            assertTrue(exception.getMessage().contains("Sift generated an invalid regex pattern"),
                    "The exception message should clearly indicate a Sift compilation error");
            assertInstanceOf(java.util.regex.PatternSyntaxException.class, exception.getCause(),
                    "The root cause must be the original PatternSyntaxException from java.util.regex");
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
            NamedCapture orderIdGroup = capture("orderId",
                    fromAnywhere().oneOrMore().digits()
            );

            String regex = fromAnywhere()
                    .pattern(literal("Order: #"))
                    .then()
                    .namedCapture(orderIdGroup)
                    .shake();

            assertEquals("Order:\\ \\#(?<orderId>[0-9]+)", regex);

            Matcher m = Pattern.compile(regex).matcher("Status for Order: #9988");
            assertTrue(m.find());
            assertEquals("9988", m.group("orderId"));
        }

        @Test
        @DisplayName("namedCapture should throw NullPointerException when group is null")
        void namedCaptureNullFailure() {
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> fromStart().namedCapture(null),
                    "Passing a null NamedCapture should trigger a NullPointerException"
            );

            assertEquals("NamedCapture cannot be null.", exception.getMessage());
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

        @Test
        @DisplayName("Should successfully match symmetric text using backreferences (XML-style)")
        void backreferenceSuccess() {

            ConnectorStep tagContent = fromAnywhere().oneOrMore().letters();
            NamedCapture tagGroup = capture("tag", tagContent);

            SiftPattern open = literal("<");
            SiftPattern slashOpen = literal("</");
            SiftPattern close = literal(">");

            String regex = fromStart()
                    .pattern(open).then().namedCapture(tagGroup).then().pattern(close)             // <tag>
                    .then().zeroOrMore().anyCharacter()                                                     // content
                    .then().pattern(slashOpen).then().backreference(tagGroup).then().pattern(close)// </tag>
                    .shake();

            assertEquals("^\\<(?<tag>[a-zA-Z]+)\\>.*\\</\\k<tag>\\>", regex);

            assertTrue(Pattern.compile(regex).matcher("<div>content</div>").find());
            assertTrue(Pattern.compile(regex).matcher("<div></div>").find());
            assertTrue(Pattern.compile(regex).matcher("<section>more content</section>").find());

            assertFalse(Pattern.compile(regex).matcher("<div>content</span>").find());
            assertFalse(Pattern.compile(regex).matcher("<div>content<div>").find());
        }

        @Test
        @DisplayName("Should match symmetric text with possessive quantifier for performance")
        void backreferencePossessiveSuccess() {
            // same test as above but accounting for possessive
            ConnectorStep tagContent = fromAnywhere().oneOrMore().letters();
            NamedCapture tagGroup = SiftPatterns.capture("tag", tagContent);

            SiftPattern open = literal("<");
            SiftPattern slashOpen = literal("</");
            SiftPattern close = literal(">");

            SiftPattern contentExceptTag = anythingBut("<");

            String regex = fromStart()
                    .pattern(open).then().namedCapture(tagGroup).then().pattern(close)
                    .then().zeroOrMore().pattern(contentExceptTag).withoutBacktracking() // Added possessive check here
                    .then().pattern(slashOpen).then().backreference(tagGroup).then().pattern(close)
                    .shake();

            String expected = "^\\<(?<tag>[a-zA-Z]+)\\>(?:[^<])*+\\</\\k<tag>\\>";
            assertEquals(expected, regex);
            // Validation
            assertTrue(Pattern.compile(regex).matcher("<div></div>").find());
            assertTrue(Pattern.compile(regex).matcher("<div>content</div>").find());
            assertFalse(Pattern.compile(regex).matcher("<div>content</span>").find());
            assertFalse(Pattern.compile(regex).matcher("<div>content<div>").find());

            String nestedInput = "<div>first</div><div>second</div>";
            assertTrue(Pattern.compile(regex).matcher(nestedInput).find());

            var matcher = Pattern.compile(regex).matcher(nestedInput);
            if (matcher.find()) {
                assertEquals("<div>first</div>", matcher.group(),
                        "Possessive + Negated Set should stop at the first closing tag");
            }
        }

        @Test
        @DisplayName("Should throw IllegalStateException when backreference is called before capture")
        void backreferenceOrderValidation() {
            NamedCapture userGroup = capture("user", literal("admin"));

            Exception exception = assertThrows(IllegalStateException.class, () ->
                    fromStart()
                            .backreference(userGroup) // throws IllegalStateException
                            .then()
                            .namedCapture(userGroup)
                            .shake());

            assertTrue(exception.getMessage().contains("must be captured"));
        }

        @Test
        @DisplayName("backreference should throw NullPointerException when group is null")
        void backreferenceNullFailure() {
            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                            fromStart().backreference(null),
                    "Passing a null NamedCapture to backreference should trigger a NullPointerException");

            assertEquals("Backreference group cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("backreference should throw exception when group is not registered yet")
        void backreferenceNotRegisteredFailure() {
            NamedCapture myGroup = capture("myTag", literal("test"));

            assertThrows(IllegalStateException.class, () ->
                            fromStart().backreference(myGroup),
                    "Should throw IllegalStateException if the group wasn't captured before being referenced");
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
        @DisplayName("Should apply possessive quantifier (+) via withoutBacktracking() on variable lengths")
        void applyPossessiveQuantifier() {
            String regex = Sift.fromAnywhere()
                    .oneOrMore().digits()
                    .withoutBacktracking()
                    .shake();

            assertEquals("[0-9]++", regex);
        }

        @Test
        @DisplayName("Should wrap pattern in atomic group (?>...) via preventBacktracking()")
        void applyAtomicGroup() {
            SiftPattern basePattern = Sift.fromAnywhere().exactly(3).digits();

            String regex = basePattern.preventBacktracking().shake();

            assertEquals("(?>[0-9]{3})", regex);
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

            String exactlyRegex = fromAnywhere().oneOrMore().digits().withoutBacktracking().shake();
            assertEquals("[0-9]++", exactlyRegex);
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
            SiftPattern animal = anyOf(cat, dog).preventBacktracking();

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
                    .pattern(aOrAb.preventBacktracking())
                    .followedBy('c')
                    .andNothingElse().shake();

            assertFalse("abc".matches(atomicRegex), "Atomic group should NOT backtrack to 'ab'");
        }

        @Test
        @DisplayName("asFewAsPossible() should append '?' to variable quantifiers making them lazy")
        void testLazyModifier() {
            // Simulates a classic match for multi-line comments: /* .*? */
            String regex = Sift.fromStart()
                    .exactly(1).pattern(SiftPatterns.literal("/*"))
                    .then()
                    .zeroOrMore().anyCharacter().asFewAsPossible()
                    .then()
                    .exactly(1).pattern(SiftPatterns.literal("*/"))
                    .shake();

            // Expects the .*? quantifier (zero or more, but as few as possible)
            assertEquals("^/\\*.*?\\*/", regex,
                    "The lazy modifier should append '?' to the zeroOrMore quantifier");
        }

        @Test
        @DisplayName("asFewAsPossible() should work correctly with range quantifiers")
        void testLazyModifierWithRange() {
            // Simulates a lazy range match
            String regex = Sift.fromStart()
                    .between(2, 5).digits().asFewAsPossible()
                    .shake();

            // Expects ^[0-9]{2,5}?
            assertEquals("^[0-9]{2,5}?", regex,
                    "The lazy modifier should append '?' to the {min,max} quantifier");
        }

        @Test
        @DisplayName("asFewAsPossible() exhaustive branch coverage for internal state protections")
        void testLazyModifierEdgeCasesAndProtections() {
            // We bypass the DSL and test the internal PatternAssembler directly
            // to achieve 100% coverage on its defensive programming branches,
            // since the new type-safe DSL physically prevents these invalid states.

            // BRANCH 1: Valid optional class
            PatternAssembler assembler1 = new PatternAssembler();
            assembler1.addClassRange("0-9");
            assembler1.setQuantifier("?");
            assembler1.applyLazyModifier();
            assertEquals("[0-9]??", assembler1.build(), "Should allow lazy modifier on optional character classes");

            // BRANCH 2: Ignore second lazy call on a class
            PatternAssembler assembler2 = new PatternAssembler();
            assembler2.addClassRange("0-9");
            assembler2.setQuantifier("+");
            assembler2.applyLazyModifier();
            assembler2.applyLazyModifier(); // Attempt double lazy
            assertEquals("[0-9]+?", assembler2.build(), "Should ignore a second lazy call on a character class");

            // BRANCH 3: Ignore lazy if the class quantifier is empty (Fixed length)
            PatternAssembler assembler3 = new PatternAssembler();
            assembler3.addClassRange("0-9");
            assembler3.setQuantifier(""); // Empty quantifier
            assembler3.applyLazyModifier(); // Invalid call
            assertEquals("[0-9]", assembler3.build(), "Should ignore lazy modifier if the class quantifier is empty");

            // BRANCH 4: Ignore second lazy call on the main pattern
            PatternAssembler assembler4 = new PatternAssembler();
            assembler4.setQuantifier("+");
            assembler4.addAnyChar();
            assembler4.applyLazyModifier(); // First valid call
            assembler4.applyLazyModifier(); // Second invalid attempt
            assertEquals(".+?", assembler4.build(), "Should ignore a second lazy call on the main pattern");
        }

        @Test
        @DisplayName("withoutBacktracking() exhaustive branch coverage for internal state protections")
        void testPossessiveModifierEdgeCasesAndProtections() {
            // BRANCH 1: Valid one-or-more class
            PatternAssembler assembler1 = new PatternAssembler();
            assembler1.addClassRange("0-9");
            assembler1.setQuantifier("+");
            assembler1.applyPossessiveModifier();
            assertEquals("[0-9]++", assembler1.build(), "Should allow possessive modifier on classes");

            // BRANCH 2: Ignore second possessive call on a class
            PatternAssembler assembler2 = new PatternAssembler();
            assembler2.addClassRange("0-9");
            assembler2.setQuantifier("+");
            assembler2.applyPossessiveModifier();
            assembler2.applyPossessiveModifier(); // Attempt double possessive
            assertEquals("[0-9]++", assembler2.build(), "Should ignore a second possessive call on a character class");

            // BRANCH 3: Ignore possessive if the class quantifier is empty (Fixed length)
            PatternAssembler assembler3 = new PatternAssembler();
            assembler3.addClassRange("0-9");
            assembler3.setQuantifier(""); // Empty quantifier
            assembler3.applyPossessiveModifier(); // Invalid call
            assertEquals("[0-9]", assembler3.build(), "Should ignore possessive modifier if the class quantifier is empty");

            // BRANCH 4: Ignore second possessive call on the main pattern
            PatternAssembler assembler4 = new PatternAssembler();
            assembler4.setQuantifier("*");
            assembler4.addAnyChar();
            assembler4.applyPossessiveModifier(); // First valid call
            assembler4.applyPossessiveModifier(); // Second invalid attempt
            assertEquals(".*+", assembler4.build(), "Should ignore a second possessive call on the main pattern");
        }

        @Test
        void shouldMemoizeShakeAndSieveForPreventBacktracking() {
            SiftPattern basePattern = SiftPatterns.literal("atomic");
            SiftPattern atomicPattern = basePattern.preventBacktracking();

            // 1. Verify shake() caching
            String firstShake = atomicPattern.shake();
            String secondShake = atomicPattern.shake();

            assertEquals("(?>atomic)", firstShake);
            assertSame(firstShake, secondShake, "shake() on preventBacktracking() should return the same String instance.");

            // 2. Verify sieve() caching
            Pattern firstSieve = atomicPattern.sieve();
            Pattern secondSieve = atomicPattern.sieve();

            assertEquals("(?>atomic)", firstSieve.pattern());
            assertSame(firstSieve, secondSieve, "sieve() on preventBacktracking() should return the same Pattern instance.");
        }
    }

    @Nested
    @DisplayName("8. Extended & Unicode Character Classes")
    class ExtendedAndUnicodeTypes {

        @Test
        @DisplayName("Should properly restrict and negate ASCII classes")
        void asciiNegations() {
            // nonLetters: [^a-zA-Z]
            String nonLettersRegex = fromStart().exactly(1).nonLetters().andNothingElse().shake();
            assertEquals("^[^a-zA-Z]$", nonLettersRegex);
            assertRegexMatches(nonLettersRegex, "1");
            assertRegexMatches(nonLettersRegex, "!");
            assertRegexMatches(nonLettersRegex, "è"); // Valid! 'è' is not an ASCII letter
            assertRegexDoesNotMatch(nonLettersRegex, "a");
            assertRegexDoesNotMatch(nonLettersRegex, "Z");

            // nonAlphanumeric: [^a-zA-Z0-9]
            String nonAlphaRegex = fromStart().exactly(1).nonAlphanumeric().andNothingElse().shake();
            assertEquals("^[^a-zA-Z0-9]$", nonAlphaRegex);
            assertRegexMatches(nonAlphaRegex, " ");
            assertRegexMatches(nonAlphaRegex, "!");
            assertRegexDoesNotMatch(nonAlphaRegex, "1");
            assertRegexDoesNotMatch(nonAlphaRegex, "A");

            // nonDigits: [\D]
            String nonDigitsRegex = fromStart().exactly(1).nonDigits().andNothingElse().shake();
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
            String uniLetters = fromStart().oneOrMore().lettersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{L}]+$", uniLetters);
            assertRegexMatches(uniLetters, "Aimé");
            assertRegexMatches(uniLetters, "Müller");
            assertRegexDoesNotMatch(uniLetters, "123");

            // nonUnicodeLetters: [\P{L}]
            String nonUniLetters = fromStart().oneOrMore().nonLettersUnicode().andNothingElse().shake();
            assertEquals("^[\\P{L}]+$", nonUniLetters);
            assertRegexMatches(nonUniLetters, "123 !!");
            assertRegexDoesNotMatch(nonUniLetters, "è");

            // unicodeLettersUppercaseOnly: [\p{Lu}]
            String uniUpper = fromStart().oneOrMore().uppercaseLettersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{Lu}]+$", uniUpper);
            assertRegexMatches(uniUpper, "È");
            assertRegexMatches(uniUpper, "Ñ");
            assertRegexDoesNotMatch(uniUpper, "è"); // Fails because lowercase
            assertRegexDoesNotMatch(uniUpper, "A1");

            // unicodeLettersLowercaseOnly: [\p{Ll}]
            String uniLower = fromStart().oneOrMore().lowercaseLettersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{Ll}]+$", uniLower);
            assertRegexMatches(uniLower, "è");
            assertRegexMatches(uniLower, "ñ");
            assertRegexDoesNotMatch(uniLower, "È");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Digits and Whitespaces")
        void unicodeDigitsAndWhitespace() {
            // unicodeDigits: [\p{Nd}]
            String uniDigits = fromStart().exactly(1).digitsUnicode().andNothingElse().shake();
            assertEquals("^[\\p{Nd}]$", uniDigits);
            assertRegexMatches(uniDigits, "5");
            assertRegexMatches(uniDigits, "٣"); // Arabic-Indic digit 3
            assertRegexDoesNotMatch(uniDigits, "a");

            // nonUnicodeDigits: [\P{Nd}]
            String nonUniDigits = fromStart().exactly(1).nonDigitsUnicode().andNothingElse().shake();
            assertEquals("^[\\P{Nd}]$", nonUniDigits);
            assertRegexMatches(nonUniDigits, "A");
            assertRegexDoesNotMatch(nonUniDigits, "٣");

            // unicodeWhitespace: [\p{IsWhite_Space}]
            String uniSpace = fromStart().exactly(1).whitespaceUnicode().andNothingElse().shake();
            assertEquals("^[\\p{IsWhite_Space}]$", uniSpace);
            assertRegexMatches(uniSpace, " ");
            assertRegexMatches(uniSpace, "\u00A0"); // Non-breaking space
            assertRegexDoesNotMatch(uniSpace, "a");

            // nonUnicodeWhitespace: [\P{IsWhite_Space}]
            String nonUniSpace = fromStart().exactly(1).nonWhitespaceUnicode().andNothingElse().shake();
            assertEquals("^[\\P{IsWhite_Space}]$", nonUniSpace);
            assertRegexMatches(nonUniSpace, "a");
            assertRegexDoesNotMatch(nonUniSpace, "\u00A0");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Word Characters and Alphanumeric")
        void unicodeAlphanumericAndWords() {
            // unicodeAlphanumeric: [\p{L}\p{Nd}]
            String uniAlpha = fromStart().exactly(1).alphanumericUnicode().andNothingElse().shake();
            assertEquals("^[\\p{L}\\p{Nd}]$", uniAlpha);
            assertRegexMatches(uniAlpha, "è");
            assertRegexMatches(uniAlpha, "٣");
            assertRegexDoesNotMatch(uniAlpha, "_"); // Does NOT include underscore

            // nonUnicodeAlphanumeric: [^\p{L}\p{Nd}]
            String nonUniAlpha = fromStart().exactly(1).nonAlphanumericUnicode().andNothingElse().shake();
            assertEquals("^[^\\p{L}\\p{Nd}]$", nonUniAlpha);
            assertRegexMatches(nonUniAlpha, "!");
            assertRegexMatches(nonUniAlpha, " ");
            assertRegexMatches(nonUniAlpha, "_"); // Underscore is NOT alphanumeric, so it matches the negation
            assertRegexDoesNotMatch(nonUniAlpha, "è");
            assertRegexDoesNotMatch(nonUniAlpha, "٣");

            // unicodeWordCharacters: [\p{L}\p{Nd}_]
            String uniWord = fromStart().exactly(1).wordCharactersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{L}\\p{Nd}_]$", uniWord);
            assertRegexMatches(uniWord, "è");
            assertRegexMatches(uniWord, "٣");
            assertRegexMatches(uniWord, "_"); // Includes underscore
            assertRegexDoesNotMatch(uniWord, " ");

            // nonUnicodeWordCharacters: [^\p{L}\p{Nd}_]
            String nonUniWord = fromStart().exactly(1).nonWordCharactersUnicode().andNothingElse().shake();
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
            String regex = filteringWith(CASE_INSENSITIVE)
                    .fromStart()
                    .exactly(5).uppercaseLetters()
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
            String regex = filteringWith(CASE_INSENSITIVE, SiftGlobalFlag.MULTILINE)
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
            String regex = filteringWith(SiftGlobalFlag.DOTALL)
                    .fromStart()
                    .exactly(3).anyCharacter()
                    .andNothingElse()
                    .shake();

            assertEquals("(?s)^.{3}$", regex);

            // Without (?s), .any() does NOT match \n. With (?s), it does!
            assertRegexMatches(regex, "A\nB");
        }

        @Test
        @DisplayName("Should apply flags when starting from anywhere")
        void flagsFromAnywhere() {
            String regex = filteringWith(CASE_INSENSITIVE)
                    .fromAnywhere()
                    .exactly(4).uppercaseLetters()
                    .shake();

            assertEquals("(?i)[A-Z]{4}", regex);

            assertRegexMatches(regex, "some TEST string");
            assertRegexMatches(regex, "some test string");
        }

        @Test
        @DisplayName("Should apply flags when starting from a word boundary")
        void flagsFromWordBoundary() {
            String regex = filteringWith(CASE_INSENSITIVE)
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
        Constructor<Sift> constructor = Sift.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()),
                "Sift constructor must be private");

        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Nested
    @DisplayName("10. Lookarounds (Lookahead & Lookbehind)")
    class Lookarounds {

        @Test
        @DisplayName("Positive Lookahead: Match 'q' only if followed by 'u'")
        void positiveLookaheadTest() {
            String regex = fromAnywhere()
                    .character('q')
                    .then()
                    .pattern(positiveLookahead(literal("u")))
                    .shake();

            assertEquals("q(?=u)", regex);
            assertRegexMatches(regex, "question"); // 'q' is followed by 'u'
            assertRegexDoesNotMatch(regex, "qatar"); // 'q' is followed by 'a'
        }

        @Test
        @DisplayName("Negative Lookahead: Match 'foo' only if NOT followed by 'bar'")
        void negativeLookaheadTest() {
            String regex = fromStart()
                    .pattern(literal("foo"))
                    .then()
                    .pattern(negativeLookahead(literal("bar")))
                    .shake();

            assertEquals("^foo(?!bar)", regex);
            assertRegexMatches(regex, "foobaz");
            assertRegexMatches(regex, "foo");
            assertRegexDoesNotMatch(regex, "foobar");
        }

        @Test
        @DisplayName("Positive Lookbehind: Match 'apple' only if preceded by 'green '")
        void positiveLookbehindTest() {
            String regex = fromAnywhere()
                    .pattern(positiveLookbehind(literal("green ")))
                    .then()
                    .pattern(literal("apple"))
                    .shake();

            assertEquals("(?<=green\\ )apple", regex);
            assertRegexMatches(regex, "I like my green apple.");
            assertRegexDoesNotMatch(regex, "I like my red apple.");
        }

        @Test
        @DisplayName("Negative Lookbehind: Match 'cat' only if NOT preceded by 'super'")
        void negativeLookbehindTest() {
            String regex = fromAnywhere()
                    .pattern(negativeLookbehind(literal("super")))
                    .then()
                    .pattern(literal("cat"))
                    .shake();

            assertEquals("(?<!super)cat", regex);
            assertRegexMatches(regex, "tomcat");
            assertRegexMatches(regex, "cat");
            assertRegexDoesNotMatch(regex, "supercat");
        }
    }

    @Test
    @DisplayName("Should detect group name collisions when injecting nested patterns")
    void nestedPatternGroupCollision() {
        SiftPattern nestedModule = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("id", literal("foo")))
                .andNothingElse();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                Sift.fromAnywhere()
                        .namedCapture(SiftPatterns.capture("id", literal("bar")))
                        .then()
                        .pattern(nestedModule) // FAIL!
                        .shake()
        );

        assertTrue(exception.getMessage().contains("Collision detected"));
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    @DisplayName("Should successfully merge nested patterns with non-colliding groups")
    void nestedPatternGroupMergeSuccess() {
        SiftPattern nestedModule = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("nestedId", fromAnywhere().oneOrMore().digits()))
                .andNothingElse();

        String regex = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("mainId", fromAnywhere().oneOrMore().letters()))
                .then()
                .pattern(nestedModule)
                .shake();

        assertEquals("(?<mainId>[a-zA-Z]+)(?<nestedId>[0-9]+)$", regex);
    }

    @Test
    @DisplayName("Should detect collisions when a NamedCapture contains nested groups")
    void namedCaptureNestedGroupCollision() {
        SiftPattern innerPattern = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("sharedId", Sift.fromAnywhere().oneOrMore().digits()))
                .andNothingElse();

        NamedCapture wrapperGroup = SiftPatterns.capture("wrapper", innerPattern);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                Sift.fromAnywhere()
                        .namedCapture(SiftPatterns.capture("sharedId", Sift.fromAnywhere().oneOrMore().letters()))
                        .then()
                        .namedCapture(wrapperGroup) // FAIL!
                        .shake()
        );

        assertTrue(exception.getMessage().contains("Collision detected"));
        assertTrue(exception.getMessage().contains("wrapper"));
        assertTrue(exception.getMessage().contains("sharedId"));
    }

    @Test
    @DisplayName("Should successfully merge NamedCapture containing non-colliding nested groups")
    void namedCaptureNestedGroupMergeSuccess() {
        SiftPattern innerPattern = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("innerId", Sift.fromAnywhere().oneOrMore().digits()))
                .andNothingElse();

        NamedCapture wrapperGroup = SiftPatterns.capture("wrapper", innerPattern);

        String regex = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("mainId", Sift.fromAnywhere().oneOrMore().letters()))
                .then()
                .namedCapture(wrapperGroup)
                .shake();

        assertEquals("(?<mainId>[a-zA-Z]+)(?<wrapper>(?<innerId>[0-9]+)$)", regex);
    }

    @Nested
    @DisplayName("Custom Range Modifier Tests")
    class CustomRangeTests {

        @Test
        @DisplayName("range() should generate a valid custom character class")
        void testCustomRange() {
            // Tests an alphabetic range with a variable quantifier
            String regexHexLetters = Sift.fromStart()
                    .oneOrMore().range('a', 'f')
                    .shake();
            assertEquals("^[a-f]+", regexHexLetters, "Should generate a custom range [a-f]+");

            // Tests a numeric range with a fixed quantifier
            String regexOctal = Sift.fromStart()
                    .exactly(3).range('0', '7')
                    .shake();
            assertEquals("^[0-7]{3}", regexOctal, "Should generate a custom range [0-7]{3}");

            // Tests special characters to ensure the internal escaper is triggered correctly
            String regexSymbols = Sift.fromStart()
                    .optional().range('!', '/')
                    .shake();
            assertEquals("^[!-/]?", regexSymbols, "Should correctly escape special characters inside the range");
            // Tests special characters to ensure the internal escaper is triggered correctly
            String regexBrackets = Sift.fromStart()
                    .optional().range('[', ']')
                    .shake();
            // '[' and ']' are special inside classes and must be escaped by RegexEscaper
            assertEquals("^[\\[-\\]]?", regexBrackets, "Should correctly escape brackets inside the range");
        }

        @Test
        @DisplayName("range(char, char) should correctly delegate and append the range")
        void testCharRangeDelegation() {
            String regex = Sift.fromStart().range('A', 'Z').shake();

            assertNotNull(regex);
            assertTrue(regex.contains("A-Z"), "The regex should contain the A-Z range");
        }

        @Test
        @DisplayName("range() should throw an exception if the boundaries are inverted")
        void testInvalidCustomRange() {
            // Verifies that defensive programming blocks invalid logical ranges
            assertThrows(IllegalArgumentException.class, () -> {
                Sift.fromStart().range('z', 'a');
            }, "An inverted range (z to a) should throw an IllegalArgumentException");
        }
    }
}