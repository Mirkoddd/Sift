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
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static com.mirkoddd.sift.core.Sift.filteringWith;
import static com.mirkoddd.sift.core.Sift.fromAnywhere;
import static com.mirkoddd.sift.core.Sift.fromStart;
import static com.mirkoddd.sift.core.SiftGlobalFlag.CASE_INSENSITIVE;
import static com.mirkoddd.sift.core.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mirkoddd.sift.core.dsl.Assertion;
import com.mirkoddd.sift.core.dsl.CharacterConnector;
import com.mirkoddd.sift.core.dsl.Connector;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.Root;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.dsl.VariableConnector;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import com.mirkoddd.sift.core.engine.SiftEngine;

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
                    .oneOrMore().lowerCaseLetters()
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
            String regex = fromAnywhere().oneOrMore().upperCaseLetters().andNothingElse().shake();

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
                    .of(anyOf(
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
                    .of(capture(
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
            String regex = fromAnywhere().of(literal("1.50$")).shake();

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
                    .of(literal("abc"))
                    .shake();

            assertEquals("(?:abc)+", regex);
            assertRegexMatches(regex, "abcabc");
        }

        @Test
        @DisplayName("Should throw exceptions for invalid bounds in new quantifiers")
        void testQuantifierExceptions() {
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().atMost(-1));
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().between(-1, 5));
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().between(2, -1));
            assertThrows(IllegalArgumentException.class, () -> fromAnywhere().between(5, 2)); // min > max
        }

        @Test
        @DisplayName("atMost() and between() should exhaustively validate boundaries and throw IllegalArgumentException")
        void testQuantifierBoundaryGuards() {
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().atMost(0));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().atMost(-1));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(0, 0));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(-1, 5));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(2, 0));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(2, -1));
            assertThrows(IllegalArgumentException.class, () -> Sift.fromStart().between(5, 2));
            assertDoesNotThrow(() -> Sift.fromStart().between(0, 5));
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

            assertEquals("^[\\w]{0,3}[\\s]{1,2}[\\W]?[\\S]$", regex);

            assertRegexMatches(regex, "abc \t@X");
            assertRegexMatches(regex, "  X");
            assertRegexMatches(regex, "a @X");

            assertRegexDoesNotMatch(regex, "abcd  X");
            assertRegexDoesNotMatch(regex, "aX");
            assertRegexDoesNotMatch(regex, "a    X");
            assertRegexDoesNotMatch(regex, "a  @@X");
            assertRegexDoesNotMatch(regex, "a  ");
        }

        @Test
        @DisplayName("Should allow modifiers on shorthand character classes")
        void testModifiersOnShorthands() {
            String regex = fromAnywhere()
                    .wordCharacters().excluding('_')
                    .shake();

            assertEquals("[\\w&&[^_]]", regex);
            assertRegexMatches(regex, "a");
            assertRegexMatches(regex, "Z");
            assertRegexMatches(regex, "9");
            assertRegexDoesNotMatch(regex, "_");
            assertRegexDoesNotMatch(regex, "@");
        }

        @Test
        @DisplayName("Should skip possessive modifier if quantifier already ends with + (Coverage Booster)")
        void coverageDuplicatePossessiveCondition() {
            VariableConnector<Fragment> step1 = Sift.fromAnywhere().zeroOrMore().digits();
            Connector<Fragment> step2 = step1.withoutBacktracking();
            ((VariableConnector<Fragment>) step2).withoutBacktracking();

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

            SiftPattern<Fragment> digits = fromAnywhere().digits();
            SiftPattern<Fragment> letters = fromAnywhere().letters();

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
            SiftPattern<Fragment> validPattern = SiftPatterns.literal("valid");

            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                    Sift.fromAnywhere().letters().followedBy(Arrays.asList(validPattern, validPattern, null, validPattern))
            );

            assertTrue(exception.getMessage().contains("cannot be null"));
        }

        @Test
        @DisplayName("shake() should be idempotent and cache the generated regex")
        void testShakeIdempotency() {
            SiftPattern<Root> pattern = Sift.fromStart().oneOrMore().digits();

            String firstShake = pattern.shake();
            String secondShake = pattern.shake();

            assertEquals(firstShake, secondShake);
            assertEquals("^[0-9]+", firstShake);
        }

        @Test
        @DisplayName("sieve() should return a CompiledPattern that correctly matches the input")
        void testSieveReturnsCompiledPattern() {
            SiftPattern<Root> pattern = Sift.fromStart().exactly(3).digits();

            SiftCompiledPattern compiledPattern = pattern.sieve();

            assertNotNull(compiledPattern);
            assertEquals("^[0-9]{3}", compiledPattern.getRawRegex());

            assertTrue(compiledPattern.matchesEntire("123"));
            assertFalse(compiledPattern.matchesEntire("12"));
            assertFalse(compiledPattern.matchesEntire("abc"));
        }

        @Test
        @DisplayName("equals() and hashCode() should evaluate structural identity based on the regex")
        @SuppressWarnings("EqualsWithItself, ConstantConditions")
        void testPatternIdentity() {
            SiftPattern<Root> patternA = Sift.fromStart().optional().letters();
            SiftPattern<Root> patternB = Sift.fromStart().optional().letters();
            SiftPattern<Root> patternDifferent = Sift.fromStart().optional().digits();

            assertEquals(patternA, patternB);
            assertEquals(patternA.hashCode(), patternB.hashCode());
            assertNotEquals(patternA, patternDifferent);
            assertEquals(patternA, patternA);

            boolean isPAEqualsNull = patternA.equals(null);
            assertFalse(isPAEqualsNull);
        }

        @Test
        @DisplayName("equals() should return false when one pattern has an unresolved backreference")
        void testEqualsWithInvalidPattern() {
            NamedCapture group = SiftPatterns.capture("x", Sift.fromAnywhere().digits());

            // Pattern with unresolved backreference — shake() will throw
            SiftPattern<?> invalid = Sift.fromAnywhere()
                    .backreference(group); // namedCapture never called

            SiftPattern<?> valid = Sift.fromAnywhere().digits();

            // Must not throw — must return false defensively
            assertFalse(invalid.equals(valid));
            assertFalse(valid.equals(invalid));
        }

        @Test
        @DisplayName("hashCode() should not throw when pattern has an unresolved backreference")
        void testHashCodeWithInvalidPattern() {
            NamedCapture group = SiftPatterns.capture("x", Sift.fromAnywhere().digits());
            SiftPattern<?> invalid = Sift.fromAnywhere().backreference(group);

            // Must not throw
            assertDoesNotThrow(invalid::hashCode);
        }

        @Test
        @DisplayName("toString() should return a readable error string instead of throwing for invalid patterns")
        void testToStringWithInvalidPattern() {
            NamedCapture group = SiftPatterns.capture("x", Sift.fromAnywhere().digits());
            SiftPattern<?> invalid = Sift.fromAnywhere().backreference(group);

            String result = invalid.toString();
            assertTrue(result.startsWith("[Invalid SiftPattern:"),
                    "Expected error prefix but got: " + result);
            assertTrue(result.contains("x"), "Error message should reference the unresolved group name");
        }

        @Test
        @DisplayName("equals() should differentiate patterns with same regex but different feature sets")
        void testEqualsWithSameRegexDifferentFeatures() {
            // Both compile to (?=\d+) conceptually, but one tracks LOOKAHEAD, one doesn't
            SiftPattern<Assertion> withLookahead = SiftPatterns.positiveLookahead(
                    Sift.fromAnywhere().oneOrMore().digits()
            );

            // A raw pattern injected with the same regex string but no feature tracking
            SiftPattern<Fragment> rawSameRegex = new BaseSiftPattern<Fragment>(null) {
                @Override
                public void accept(PatternVisitor visitor) {
                    visitor.visitAnchor("(?=[0-9]+)");
                }
            };

            // Same regex string, different features — should NOT be equal
            assertEquals(withLookahead.shake(), rawSameRegex.shake()); // confirm same string
            assertNotEquals(withLookahead, rawSameRegex);
        }

        @Test
        @DisplayName("toString() should return the generated pattern string")
        void testPatternToString() {
            SiftPattern<Root> pattern = Sift.fromStart().exactly(3).digits();

            assertEquals("^[0-9]{3}", pattern.toString());
            assertEquals(pattern.shake(), pattern.toString());
        }

        @Test
        @DisplayName("Engine compilation should perform fail-fast validation when a malformed pattern is compiled")
        void testEngineFailFastValidation() {
            // Simulate a malformed pattern by injecting raw invalid syntax directly into the AST
            SiftPattern<Fragment> malformedPattern = new SiftConnector<>(null, visitor -> visitor.visitAnchor("(?unclosedGroup"));

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                        try (SiftCompiledPattern ignore = Sift.fromStart().of(malformedPattern).sieve()) {
                            fail("Should not reach this point");
                        }
                    }
            );

            assertTrue(exception.getMessage().contains("Sift generated an invalid regex syntax"));
            assertInstanceOf(java.util.regex.PatternSyntaxException.class, exception.getCause());
        }

        @Test
        @DisplayName("PatternAssembler should safely merge custom external patterns that lack PatternMetadata")
        void testExternalPatternWithoutMetadataBranchCoverage() {
            // Simulating a third-party plugin: a custom class that only implements
            // the public SiftPattern interface, completely ignoring internal metadata.
            SiftPattern<Fragment> alienPattern = new SiftPattern<Fragment>() {
                @Override
                public String shake() {
                    return "alien-regex";
                }

                @Override
                public SiftCompiledPattern sieveWith(SiftEngine engine) {
                    return engine.compile(shake(), java.util.Collections.emptySet());
                }

                @Override
                public SiftPattern<Fragment> preventBacktracking() {
                    return this; // Simplified for the scope of this test
                }

                @Override
                public Object ___internal_lock___() {
                    return new Object();
                }
            };

            // By injecting 'alienPattern' via .of(), PatternAssembler will trigger
            // getIncomingGroups, getIncomingBackreferences, and getIncomingFeatures.
            // The 'instanceof PatternMetadata' check will evaluate to FALSE in all three methods!
            String regex = Sift.fromStart()
                    .of(alienPattern)
                    .andNothingElse()
                    .shake();

            // We verify that the assembly succeeds and returns the expected raw string,
            // proving that the Collections.emptySet() fallback branches work flawlessly.
            assertEquals("^alien-regex$", regex);
        }

        @Test
        @DisplayName("equals() should return true for two patterns with same regex when one lacks PatternMetadata")
        @SuppressWarnings("EqualsWithItself")
        void testEqualsWithNonMetadataPattern() {
            SiftPattern<Fragment> normalPattern = Sift.fromAnywhere().oneOrMore().digits();

            // An alien pattern implementing only the public SiftPattern interface,
            // with no PatternMetadata — hits the final `return true` branch in equals()
            SiftPattern<Fragment> alienPattern = new SiftPattern<Fragment>() {
                @Override public Object ___internal_lock___() { return InternalToken.INSTANCE; }
                @Override public String shake() { return normalPattern.shake(); }
                @Override public SiftCompiledPattern sieveWith(SiftEngine engine) { return null; }
                @Override public SiftPattern<Fragment> preventBacktracking() { return this; }
            };

            // Same regex, no PatternMetadata on alien → hits return true
            assertTrue(normalPattern.equals(alienPattern));
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
            NamedCapture orderIdGroup = capture("orderId",
                    fromAnywhere().oneOrMore().digits()
            );

            String regex = fromAnywhere()
                    .of(literal("Order: #"))
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
                    () -> fromStart().namedCapture(null)
            );

            assertEquals("NamedCapture cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("Scenario: Extracting Prices")
        void priceExtraction() {
            SiftPattern<Fragment> decimalPart = fromAnywhere()
                    .character('.')
                    .then()
                    .exactly(2).digits();

            String regex = fromStart()
                    .character('$')
                    .then()
                    .oneOrMore().digits()
                    .then()
                    .optional()
                    .of(decimalPart)
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

            Connector<Fragment> tagContent = fromAnywhere().oneOrMore().letters();
            NamedCapture tagGroup = capture("tag", tagContent);

            SiftPattern<Fragment> open = literal("<");
            SiftPattern<Fragment> slashOpen = literal("</");
            SiftPattern<Fragment> close = literal(">");

            String regex = fromStart()
                    .of(open).then().namedCapture(tagGroup).then().of(close)
                    .then().zeroOrMore().anyCharacter()
                    .then().exactly(1).of(slashOpen)
                    .then().backreference(tagGroup)
                    .then().exactly(1).of(close)
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
            Connector<Fragment> tagContent = fromAnywhere().oneOrMore().letters();
            NamedCapture tagGroup = SiftPatterns.capture("tag", tagContent);

            SiftPattern<Fragment> open = literal("<");
            SiftPattern<Fragment> slashOpen = literal("</");
            SiftPattern<Fragment> close = literal(">");
            SiftPattern<Fragment> contentExceptTag = anythingBut("<");

            String regex = fromStart()
                    .of(open).then().namedCapture(tagGroup).then().of(close)
                    .then().zeroOrMore().of(contentExceptTag).withoutBacktracking()
                    .then().exactly(1).of(slashOpen)
                    .then().backreference(tagGroup)
                    .then().exactly(1).of(close)
                    .shake();

            String expected = "^\\<(?<tag>[a-zA-Z]+)\\>(?:[^<])*+\\</\\k<tag>\\>";
            assertEquals(expected, regex);

            assertTrue(Pattern.compile(regex).matcher("<div></div>").find());
            assertTrue(Pattern.compile(regex).matcher("<div>content</div>").find());
            assertFalse(Pattern.compile(regex).matcher("<div>content</span>").find());
            assertFalse(Pattern.compile(regex).matcher("<div>content<div>").find());

            String nestedInput = "<div>first</div><div>second</div>";
            assertTrue(Pattern.compile(regex).matcher(nestedInput).find());

            Matcher matcher = Pattern.compile(regex).matcher(nestedInput);
            if (matcher.find()) {
                assertEquals("<div>first</div>", matcher.group(),
                        "Possessive + Negated Set should stop at the first closing tag");
            }
        }

        @Test
        @DisplayName("backreference should throw NullPointerException when group is null")
        void backreferenceNullFailure() {
            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                    fromStart().backreference(null)
            );

            assertEquals("Backreference group cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("backreference should throw exception when group is not registered yet")
        void backreferenceNotRegisteredFailure() {
            NamedCapture myGroup = capture("myTag", literal("test"));

            assertThrows(IllegalStateException.class, () ->
                    fromStart().backreference(myGroup).shake()
            );
        }
    }

    @Nested
    @DisplayName("6. Anchors & Boundaries")
    class AnchorsAndBoundaries {

        @Test
        @DisplayName("Should respect Word Boundaries (Static & Instance)")
        void wordBoundaries() {
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
            SiftPattern<Fragment> basePattern = Sift.fromAnywhere().exactly(3).digits();

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
            String regex = fromAnywhere().optional().of(literal("abc")).withoutBacktracking().shake();
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
            String greedyRegex = fromStart()
                    .oneOrMore().digits()
                    .then().exactly(1).digits()
                    .andNothingElse().shake();

            assertEquals("^[0-9]+[0-9]$", greedyRegex);
            assertTrue("123".matches(greedyRegex));

            String possessiveRegex = fromStart()
                    .oneOrMore().digits().withoutBacktracking()
                    .then().exactly(1).digits()
                    .andNothingElse().shake();

            assertEquals("^[0-9]++[0-9]$", possessiveRegex);
            assertFalse("123".matches(possessiveRegex));
        }

        @Test
        @DisplayName("Should generate atomic group for standalone SiftPatterns")
        void atomicGroupSyntax() {
            SiftPattern<Fragment> cat = literal("cat");
            SiftPattern<Fragment> dog = literal("dog");
            SiftPattern<Fragment> animal = anyOf(cat, dog).preventBacktracking();

            String regex = fromAnywhere().of(animal).shake();

            assertEquals("(?>(?:cat|dog))", regex);
        }

        @Test
        @DisplayName("Should actually prevent backtracking using Atomic Groups")
        void atomicGroupBehaviorCheck() {
            SiftPattern<Fragment> a = literal("a");
            SiftPattern<Fragment> ab = literal("ab");
            SiftPattern<Fragment> aOrAb = anyOf(a, ab);

            String normalRegex = fromStart()
                    .of(aOrAb)
                    .followedBy('c')
                    .andNothingElse().shake();

            assertTrue("abc".matches(normalRegex));

            String atomicRegex = fromStart()
                    .of(aOrAb.preventBacktracking())
                    .followedBy('c')
                    .andNothingElse().shake();

            assertFalse("abc".matches(atomicRegex));
        }

        @Test
        @DisplayName("asFewAsPossible() should append '?' to variable quantifiers making them lazy")
        void testLazyModifier() {
            String regex = Sift.fromStart()
                    .exactly(1).of(SiftPatterns.literal("/*"))
                    .then()
                    .zeroOrMore().anyCharacter().asFewAsPossible()
                    .then()
                    .exactly(1).of(SiftPatterns.literal("*/"))
                    .shake();

            assertEquals("^/\\*.*?\\*/", regex);
        }

        @Test
        @DisplayName("asFewAsPossible() should work correctly with range quantifiers")
        void testLazyModifierWithRange() {
            String regex = Sift.fromStart()
                    .between(2, 5).digits().asFewAsPossible()
                    .shake();

            assertEquals("^[0-9]{2,5}?", regex);
        }

        @Test
        @DisplayName("asFewAsPossible() exhaustive branch coverage for internal state protections")
        void testLazyModifierEdgeCasesAndProtections() {
            PatternAssembler assembler = new PatternAssembler();
            assembler.visitQuantifier("?");
            assembler.visitClassRange("0-9");
            assembler.visitLazyModifier();
            assertEquals("[0-9]??", assembler.build());

            PatternAssembler assembler2 = new PatternAssembler();
            assembler2.visitQuantifier("+");
            assembler2.visitClassRange("0-9");
            assembler2.visitLazyModifier();
            assembler2.visitLazyModifier();
            assertEquals("[0-9]+?", assembler2.build());

            PatternAssembler assembler3 = new PatternAssembler();
            assembler3.visitQuantifier("");
            assembler3.visitClassRange("0-9");
            assembler3.visitLazyModifier();
            assertEquals("[0-9]", assembler3.build());

            PatternAssembler assembler4 = new PatternAssembler();
            assembler4.visitQuantifier("+");
            assembler4.visitAnyChar();
            assembler4.visitLazyModifier();
            assembler4.visitLazyModifier();
            assertEquals(".+?", assembler4.build());
        }

        @Test
        @DisplayName("withoutBacktracking() exhaustive branch coverage for internal state protections")
        void testPossessiveModifierEdgeCasesAndProtections() {
            PatternAssembler assembler1 = new PatternAssembler();
            assembler1.visitQuantifier("+");
            assembler1.visitClassRange("0-9");
            assembler1.visitPossessiveModifier();
            assertEquals("[0-9]++", assembler1.build());

            PatternAssembler assembler2 = new PatternAssembler();
            assembler2.visitQuantifier("+");
            assembler2.visitClassRange("0-9");
            assembler2.visitPossessiveModifier();
            assembler2.visitPossessiveModifier();
            assertEquals("[0-9]++", assembler2.build());

            PatternAssembler assembler3 = new PatternAssembler();
            assembler3.visitQuantifier("");
            assembler3.visitClassRange("0-9");
            assembler3.visitPossessiveModifier();
            assertEquals("[0-9]", assembler3.build());

            PatternAssembler assembler4 = new PatternAssembler();
            assembler4.visitQuantifier("*");
            assembler4.visitAnyChar();
            assembler4.visitPossessiveModifier();
            assembler4.visitPossessiveModifier();
            assertEquals(".*+", assembler4.build());
        }

        @Test
        void shouldMemoizeShakeForPreventBacktracking() {
            SiftPattern<Fragment> basePattern = SiftPatterns.literal("atomic");
            SiftPattern<Fragment> atomicPattern = basePattern.preventBacktracking();

            String firstShake = atomicPattern.shake();
            String secondShake = atomicPattern.shake();

            assertEquals("(?>atomic)", firstShake);
            assertSame(firstShake, secondShake);
        }
    }

    @Nested
    @DisplayName("8. Extended & Unicode Character Classes")
    class ExtendedAndUnicodeTypes {

        @Test
        @DisplayName("Should properly restrict and negate ASCII classes")
        void asciiNegations() {
            String nonLettersRegex = fromStart().exactly(1).nonLetters().andNothingElse().shake();
            assertEquals("^[^a-zA-Z]$", nonLettersRegex);
            assertRegexMatches(nonLettersRegex, "1");
            assertRegexMatches(nonLettersRegex, "!");
            assertRegexMatches(nonLettersRegex, "è");
            assertRegexDoesNotMatch(nonLettersRegex, "a");
            assertRegexDoesNotMatch(nonLettersRegex, "Z");

            String nonAlphaRegex = fromStart().exactly(1).nonAlphanumeric().andNothingElse().shake();
            assertEquals("^[^a-zA-Z0-9]$", nonAlphaRegex);
            assertRegexMatches(nonAlphaRegex, " ");
            assertRegexMatches(nonAlphaRegex, "!");
            assertRegexDoesNotMatch(nonAlphaRegex, "1");
            assertRegexDoesNotMatch(nonAlphaRegex, "A");

            String nonDigitsRegex = fromStart().exactly(1).nonDigits().andNothingElse().shake();
            assertEquals("^[\\D]$", nonDigitsRegex);
            assertRegexMatches(nonDigitsRegex, "A");
            assertRegexMatches(nonDigitsRegex, "!");
            assertRegexMatches(nonDigitsRegex, "è");
            assertRegexDoesNotMatch(nonDigitsRegex, "5");
            assertRegexDoesNotMatch(nonDigitsRegex, "0");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Letters and Case Specifics")
        void unicodeLettersAndCases() {
            String uniLetters = fromStart().oneOrMore().lettersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{L}]+$", uniLetters);
            assertRegexMatches(uniLetters, "Aimé");
            assertRegexMatches(uniLetters, "Müller");
            assertRegexDoesNotMatch(uniLetters, "123");

            String nonUniLetters = fromStart().oneOrMore().nonLettersUnicode().andNothingElse().shake();
            assertEquals("^[\\P{L}]+$", nonUniLetters);
            assertRegexMatches(nonUniLetters, "123 !!");
            assertRegexDoesNotMatch(nonUniLetters, "è");

            String uniUpper = fromStart().oneOrMore().upperCaseLettersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{Lu}]+$", uniUpper);
            assertRegexMatches(uniUpper, "È");
            assertRegexMatches(uniUpper, "Ñ");
            assertRegexDoesNotMatch(uniUpper, "è");
            assertRegexDoesNotMatch(uniUpper, "A1");

            String uniLower = fromStart().oneOrMore().lowerCaseLettersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{Ll}]+$", uniLower);
            assertRegexMatches(uniLower, "è");
            assertRegexMatches(uniLower, "ñ");
            assertRegexDoesNotMatch(uniLower, "È");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Digits and Whitespaces")
        void unicodeDigitsAndWhitespace() {
            String uniDigits = fromStart().exactly(1).digitsUnicode().andNothingElse().shake();
            assertEquals("^[\\p{Nd}]$", uniDigits);
            assertRegexMatches(uniDigits, "5");
            assertRegexMatches(uniDigits, "٣");
            assertRegexDoesNotMatch(uniDigits, "a");

            String nonUniDigits = fromStart().exactly(1).nonDigitsUnicode().andNothingElse().shake();
            assertEquals("^[\\P{Nd}]$", nonUniDigits);
            assertRegexMatches(nonUniDigits, "A");
            assertRegexDoesNotMatch(nonUniDigits, "٣");

            String uniSpace = fromStart().exactly(1).whitespaceUnicode().andNothingElse().shake();
            assertEquals("^[\\p{IsWhite_Space}]$", uniSpace);
            assertRegexMatches(uniSpace, " ");
            assertRegexMatches(uniSpace, "\u00A0");
            assertRegexDoesNotMatch(uniSpace, "a");

            String nonUniSpace = fromStart().exactly(1).nonWhitespaceUnicode().andNothingElse().shake();
            assertEquals("^[\\P{IsWhite_Space}]$", nonUniSpace);
            assertRegexMatches(nonUniSpace, "a");
            assertRegexDoesNotMatch(nonUniSpace, "\u00A0");
        }

        @Test
        @DisplayName("Should correctly handle Unicode Word Characters and Alphanumeric")
        void unicodeAlphanumericAndWords() {
            String uniAlpha = fromStart().exactly(1).alphanumericUnicode().andNothingElse().shake();
            assertEquals("^[\\p{L}\\p{Nd}]$", uniAlpha);
            assertRegexMatches(uniAlpha, "è");
            assertRegexMatches(uniAlpha, "٣");
            assertRegexDoesNotMatch(uniAlpha, "_");

            String nonUniAlpha = fromStart().exactly(1).nonAlphanumericUnicode().andNothingElse().shake();
            assertEquals("^[^\\p{L}\\p{Nd}]$", nonUniAlpha);
            assertRegexMatches(nonUniAlpha, "!");
            assertRegexMatches(nonUniAlpha, " ");
            assertRegexMatches(nonUniAlpha, "_");
            assertRegexDoesNotMatch(nonUniAlpha, "è");
            assertRegexDoesNotMatch(nonUniAlpha, "٣");

            String uniWord = fromStart().exactly(1).wordCharactersUnicode().andNothingElse().shake();
            assertEquals("^[\\p{L}\\p{Nd}_]$", uniWord);
            assertRegexMatches(uniWord, "è");
            assertRegexMatches(uniWord, "٣");
            assertRegexMatches(uniWord, "_");
            assertRegexDoesNotMatch(uniWord, " ");

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
                    .exactly(5).upperCaseLetters()
                    .andNothingElse()
                    .shake();

            assertEquals("(?i)^[A-Z]{5}$", regex);

            assertRegexMatches(regex, "PROMO");
            assertRegexMatches(regex, "promo");
            assertRegexMatches(regex, "PrOmO");

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

            assertEquals("(?im)^[a-zA-Z]+$", regex);

            String multilineInput = "HELLO\nworld\nTEST";

            Matcher matcher = Pattern.compile(regex).matcher(multilineInput);
            int matchCount = 0;
            while (matcher.find()) {
                matchCount++;
            }
            assertEquals(3, matchCount);
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
            assertRegexMatches(regex, "A\nB");
        }

        @Test
        @DisplayName("Should apply flags when starting from anywhere")
        void flagsFromAnywhere() {
            String regex = filteringWith(CASE_INSENSITIVE)
                    .fromAnywhere()
                    .exactly(4).upperCaseLetters()
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

        @Test
        @DisplayName("Should handle COMMENTS (verbose) mode correctly")
        void commentsFlag() {
            String regex = filteringWith(SiftGlobalFlag.COMMENTS)
                    .fromStart()
                    .exactly(3).letters()
                    .andNothingElse()
                    .shake();

            assertEquals("(?x)^[a-zA-Z]{3}$", regex);
            assertRegexMatches(regex, "abc");
            assertRegexDoesNotMatch(regex, "a c");
        }

        @Test
        @DisplayName("Should handle UNICODE_CASE mode correctly alongside CASE_INSENSITIVE")
        void unicodeCaseFlag() {
            String regex = filteringWith(CASE_INSENSITIVE, SiftGlobalFlag.UNICODE_CASE)
                    .fromStart()
                    .oneOrMore().lettersUnicode()
                    .andNothingElse()
                    .shake();

            assertEquals("(?iu)^[\\p{L}]+$", regex);

            assertRegexMatches(regex, "è");
            assertRegexMatches(regex, "È");
            assertRegexMatches(regex, "ω");
            assertRegexMatches(regex, "Ω");
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
                    .mustBeFollowedBy(literal("u"))
                    .shake();

            assertEquals("q(?=u)", regex);
            assertRegexMatches(regex, "question");
            assertRegexDoesNotMatch(regex, "qatar");
        }

        @Test
        @DisplayName("Negative Lookahead: Match 'foo' only if NOT followed by 'bar'")
        void negativeLookaheadTest() {
            String regex = fromStart()
                    .of(literal("foo"))
                    .notFollowedBy(literal("bar"))
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
                    .of(literal("apple"))
                    .mustBePrecededBy(literal("green "))
                    .shake();

            assertEquals("(?<=green\\ )apple", regex);
            assertRegexMatches(regex, "I like my green apple.");
            assertRegexDoesNotMatch(regex, "I like my red apple.");
        }

        @Test
        @DisplayName("Negative Lookbehind: Match 'cat' only if NOT preceded by 'super'")
        void negativeLookbehindTest() {
            String regex = fromAnywhere()
                    .of(literal("cat"))
                    .notPrecededBy(literal("super"))
                    .shake();

            assertEquals("(?<!super)cat", regex);
            assertRegexMatches(regex, "tomcat");
            assertRegexMatches(regex, "cat");
            assertRegexDoesNotMatch(regex, "supercat");
        }

        @Test
        @DisplayName("Mixed Prepending & Lookarounds: The Ultimate Boss Fight")
        void mixedLookaroundsTest() {
            // We want to match the exact string "TEST_PASS" with very strict rules:
            // 1. Must be structurally built by prepending "TEST_" to "PASS" (Tests precededBy Fragment)
            // 2. Must NOT be preceded by "SKIP_" (Tests precededBy Assertion)
            // 3. Must be followed by "_OK" (Tests followedBy Assertion)
            // 4. Must NOT be followed by "_OK_BUT_SLOW" (Tests followedBy Assertion)

            String regexSugar = fromAnywhere()
                    .of(literal("PASS"))
                    .precededBy(literal("TEST_"))             // 1. Structural prepend -> "TEST_PASS"
                    .notPrecededBy(literal("SKIP_"))          // 2. Negative Lookbehind
                    .mustBeFollowedBy(literal("_OK"))         // 3. Positive Lookahead
                    .notFollowedBy(literal("_OK_BUT_SLOW"))   // 4. Negative Lookahead
                    .shake();

            // The engine correctly layers the prepends: the lookbehind goes BEFORE the "TEST_" prefix!
            String expectedRegex = "(?<!SKIP_)TEST_PASS(?=_OK)(?!_OK_BUT_SLOW)";

            assertEquals(expectedRegex, regexSugar, "Mixed prepending and lookarounds failed to assemble correctly");

            // VALID MATCHES:
            assertRegexMatches(regexSugar, "TEST_PASS_OK");
            assertRegexMatches(regexSugar, "System_TEST_PASS_OK_fast");

            // INVALID MATCHES (Structural & Lookbehind failures):
            assertRegexDoesNotMatch(regexSugar, "UNIT_PASS_OK");      // Missing the structurally prepended "TEST_"
            assertRegexDoesNotMatch(regexSugar, "SKIP_TEST_PASS_OK"); // Fails negative lookbehind

            // INVALID MATCHES (Lookahead failures):
            assertRegexDoesNotMatch(regexSugar, "TEST_PASS_FAIL");         // Fails positive lookahead
            assertRegexDoesNotMatch(regexSugar, "TEST_PASS_OK_BUT_SLOW");  // Fails negative lookahead
        }

        @Test
        @DisplayName("Quantifier notFollowedBy: Assertion immediately after start anchor")
        void quantifierNotFollowedByTest() {
            // Goal: Match any full line of text, as long as it does NOT start with "ERROR"

            String regexSugar = fromStart()
                    .notFollowedBy(literal("ERROR")) // Appended directly to the Start anchor!
                    .then().zeroOrMore().anyCharacter()
                    .andNothingElse()
                    .shake();

            // The negative lookahead must be attached immediately after the '^'
            assertEquals("^(?!ERROR).*$", regexSugar);

            // VALID MATCHES:
            assertRegexMatches(regexSugar, "WARNING: Disk space low");
            assertRegexMatches(regexSugar, "INFO: System booted successfully");
            assertRegexMatches(regexSugar, "ERR: Just a typo, which is allowed");

            // INVALID MATCHES (because they start with ERROR):
            assertRegexDoesNotMatch(regexSugar, "ERROR: Out of memory");
            assertRegexDoesNotMatch(regexSugar, "ERROR");
        }

        @Test
        @DisplayName("visitConditional should correctly handle non-empty quantifiers via .of()")
        void conditionalQuantifierCoverage() {
            SiftPattern<Fragment> conditionalBlock = SiftPatterns.ifFollowedBy(literal("A"))
                    .thenUse(literal("B"))
                    .otherwiseNothing();

            String unquantified = Sift.fromStart()
                    .of(conditionalBlock)
                    .shake();

            assertEquals("^(?:(?=A)B|(?!A))", unquantified);

            String quantified = Sift.fromStart()
                    .oneOrMore().of(conditionalBlock).withoutBacktracking()
                    .shake();

            assertEquals("^(?:(?:(?=A)B|(?!A)))++", quantified);
        }
    }

    @Test
    @DisplayName("Should detect group name collisions when injecting nested patterns")
    void nestedPatternGroupCollision() {
        SiftPattern<Fragment> nestedModule = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("id", literal("foo")));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                Sift.fromAnywhere()
                        .namedCapture(SiftPatterns.capture("id", literal("bar")))
                        .then().exactly(1).of(nestedModule)
                        .andNothingElse()
                        .shake()
        );

        assertTrue(exception.getMessage().contains("Collision detected"));
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    @DisplayName("Should successfully merge nested patterns with non-colliding groups")
    void nestedPatternGroupMergeSuccess() {
        SiftPattern<Fragment> nestedModule = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("nestedId", fromAnywhere().oneOrMore().digits()));

        String regex = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("mainId", fromAnywhere().oneOrMore().letters()))
                .then().exactly(1).of(nestedModule)
                .andNothingElse()
                .shake();

        assertEquals("(?<mainId>[a-zA-Z]+)(?<nestedId>[0-9]+)$", regex);
    }

    @Test
    @DisplayName("Should detect collisions when a NamedCapture contains nested groups")
    void namedCaptureNestedGroupCollision() {
        SiftPattern<Fragment> innerPattern = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("sharedId", Sift.fromAnywhere().oneOrMore().digits()));

        NamedCapture wrapperGroup = SiftPatterns.capture("wrapper", innerPattern);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                Sift.fromAnywhere()
                        .namedCapture(SiftPatterns.capture("sharedId", Sift.fromAnywhere().oneOrMore().letters()))
                        .then()
                        .namedCapture(wrapperGroup)
                        .andNothingElse()
                        .shake()
        );

        assertTrue(exception.getMessage().contains("Collision detected"));
        assertTrue(exception.getMessage().contains("wrapper"));
        assertTrue(exception.getMessage().contains("sharedId"));
    }

    @Test
    @DisplayName("Should successfully merge NamedCapture containing non-colliding nested groups")
    void namedCaptureNestedGroupMergeSuccess() {
        SiftPattern<Fragment> innerPattern = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("innerId", Sift.fromAnywhere().oneOrMore().digits()));

        NamedCapture wrapperGroup = SiftPatterns.capture("wrapper", innerPattern);

        String regex = Sift.fromAnywhere()
                .namedCapture(SiftPatterns.capture("mainId", Sift.fromAnywhere().oneOrMore().letters()))
                .then()
                .namedCapture(wrapperGroup)
                .andNothingElse()
                .shake();

        assertEquals("(?<mainId>[a-zA-Z]+)(?<wrapper>(?<innerId>[0-9]+))$", regex);
    }

    @Nested
    @DisplayName("Custom Range Modifier Tests")
    class CustomRangeTests {

        @Test
        @DisplayName("range() should generate a valid custom character class")
        void testCustomRange() {
            String regexHexLetters = Sift.fromStart()
                    .oneOrMore().range('a', 'f')
                    .shake();
            assertEquals("^[a-f]+", regexHexLetters);

            String regexOctal = Sift.fromStart()
                    .exactly(3).range('0', '7')
                    .shake();
            assertEquals("^[0-7]{3}", regexOctal);

            String regexSymbols = Sift.fromStart()
                    .optional().range('!', '/')
                    .shake();
            assertEquals("^[!-/]?", regexSymbols);

            String regexBrackets = Sift.fromStart()
                    .optional().range('[', ']')
                    .shake();
            assertEquals("^[\\[-\\]]?", regexBrackets);
        }

        @Test
        @DisplayName("range(char, char) should correctly delegate and append the range")
        void testCharRangeDelegation() {
            String regex = Sift.fromStart().range('A', 'Z').shake();

            assertNotNull(regex);
            assertTrue(regex.contains("A-Z"));
        }

        @Test
        @DisplayName("range() should throw an exception if the boundaries are inverted")
        void testInvalidCustomRange() {
            assertThrows(IllegalArgumentException.class, () ->
                    Sift.fromStart().range('z', 'a'));
        }
    }

    @Nested
    @DisplayName("Chain Nested Patterns Test")
    class ChainNestedPatternsTest {

        @Test
        @DisplayName("Should allow nesting pure unanchored patterns seamlessly")
        void shouldAllowNestingUnanchoredPatterns() {
            CharacterConnector<Fragment> safeBlock = fromAnywhere().exactly(3).digits();

            String regex = Sift.fromStart()
                    .of(safeBlock)
                    .andNothingElse()
                    .shake();

            assertEquals("^[0-9]{3}$", regex);
        }

        @Test
        @DisplayName("Should evaluate false branch of absolute anchor check")
        void shouldCoverFalseBranchInAddAnchor() {
            PatternAssembler assembler = new PatternAssembler();

            assembler.visitAnchor("\\b");

            assertFalse(assembler.isContainsAbsoluteAnchor());
        }
    }

    @Nested
    @DisplayName("Advanced Features (Intersection, Line breaks, Anchors)")
    class AdvancedFeaturesTest {

        @Test
        @DisplayName("Should validate character class intersection (&&) and local inline flags")
        void testIntersectionAndLocalFlags() {
            // Target: Match 1 Greek letter followed by the word "sift" (case-insensitive)

            // 1. Intersection: Must be a Word Character (\w) AND a Greek letter
            CharacterConnector<Fragment> greekLetter = fromAnywhere().exactly(1).wordCharactersUnicode()
                    .intersecting(CharacterSubset.GREEK);

            // 2. Local Flag: The string "sift" is not sensitive to uppercase/lowercase
            SiftPattern<Fragment> caseInsensitiveSift = SiftPatterns.withFlags(
                    SiftPatterns.literal("sift"),
                    SiftGlobalFlag.CASE_INSENSITIVE
            );

            String regex = fromStart()
                    .of(greekLetter)
                    .followedBy(caseInsensitiveSift)
                    .andNothingElse()
                    .shake();

            assertEquals("^[\\p{L}\\p{Nd}_&&[\\p{IsGreek}]](?i:sift)$", regex);

            assertRegexMatches(regex, "αSIFT");   // Greek letter + SIFT uppercase
            assertRegexMatches(regex, "βsift");   // Greek letter + sift lowercase
            assertRegexMatches(regex, "γSiFt");   // Greek letter + mixed case

            assertRegexDoesNotMatch(regex, "aSIFT");  // 'a' is Latin, not Greek (Intersection fails)
            assertRegexDoesNotMatch(regex, "αSIFTX"); // Too long, andNothingElse() blocks it
        }

        @Test
        @DisplayName("visitLocalFlags should safely delegate quantification to the parent node")
        void localFlagsQuantifierCoverage() {
            SiftPattern<Fragment> flaggedBlock = SiftPatterns.withFlags(literal("A"), SiftGlobalFlag.CASE_INSENSITIVE);

            // Branch 1: Unquantified
            String unquantified = Sift.fromStart().of(flaggedBlock).shake();
            assertEquals("^(?i:A)", unquantified);

            // Branch 2: Quantified + Possessive
            String quantified = Sift.fromStart()
                    .oneOrMore().of(flaggedBlock).withoutBacktracking()
                    .shake();

            assertEquals("^(?:(?i:A))++", quantified);
        }

        @Test
        @DisplayName("Should filter word characters to strictly allow only ASCII letters")
        void testRealWorldAsciiIntersection() {
            // Real-world scenario: The \w token (word characters) natively includes digits (0-9)
            // and underscores (_). We want to match pure alphabetical names only.
            // By intersecting \w with ASCII_LETTERS, we use the intersection as a "sieve"
            // to filter out the unwanted digits and underscores.
            String regex = Sift.fromStart()
                    .oneOrMore().wordCharacters()
                    .intersecting(CharacterSubset.ASCII_LETTERS)
                    .andNothingElse()
                    .shake();

            // Verify that the engine correctly structures the intersection syntax
            // between a shorthand character class and an ASCII range
            assertEquals("^[\\w&&[a-zA-Z]]+$", regex);

            // VALID MATCHES: Pure alphabetical strings pass the intersection filter
            assertRegexMatches(regex, "Sift");
            assertRegexMatches(regex, "Mirko");

            // INVALID MATCHES: Valid for \w, but strictly rejected by the ASCII_LETTERS intersection
            assertRegexDoesNotMatch(regex, "User123"); // Fails because digits are filtered out
            assertRegexDoesNotMatch(regex, "my_var");  // Fails because the underscore is filtered out

            // INVALID MATCHES: Standard rejection (punctuation is neither \w nor a letter)
            assertRegexDoesNotMatch(regex, "Mirko!");
        }

        @Test
        @DisplayName("Should parse formats using horizontal whitespace and universal linebreaks")
        void testAdvancedWhitespace() {
            // Target: Match "Word [Space/Tab] Word [Universal Linebreak]"

            SiftPattern<Fragment> word = fromAnywhere().oneOrMore().wordCharacters();
            SiftPattern<Fragment> horizontalSpace = fromAnywhere().oneOrMore().whitespaceHorizontal();
            SiftPattern<Fragment> lineBreak = fromAnywhere().exactly(1).linebreakUnicode();

            String regex = fromStart()
                    .of(word)
                    .followedBy(horizontalSpace)
                    .followedBy(word)
                    .followedBy(lineBreak)
                    .andNothingElse()
                    .shake();

            assertEquals("^[\\w]+[\\h]+[\\w]+\\R$", regex);

            assertRegexMatches(regex, "Hello\tWorld\r\n"); // Horizontal Tab + CRLF (Windows)
            assertRegexMatches(regex, "Sift Space\n");     // Horizontal Space + LF (Linux/Mac)

            assertRegexDoesNotMatch(regex, "Hello\nWorld\n"); // \n in the middle is NOT horizontal space
            assertRegexDoesNotMatch(regex, "HelloWorld\n");   // Missing horizontal space entirely
        }

        @Test
        @DisplayName("addLinebreak() exhaustive branch coverage for internal state protections")
        void testLinebreakModifierCoverage() {
            String singleLinebreak = Sift.fromAnywhere()
                    .exactly(1).linebreakUnicode()
                    .shake();

            assertEquals("\\R", singleLinebreak);

            String possessiveLineBreaks = Sift.fromAnywhere()
                    .oneOrMore().linebreakUnicode().withoutBacktracking()
                    .shake();

            assertEquals("\\R++", possessiveLineBreaks);
        }

        @Test
        @DisplayName("Should strictly match consecutive tokens using the \\G anchor (Previous Match End)")
        void testPreviousMatchEndAnchor() {
            // Target: Extract sequential words WITHOUT skipping invalid characters in between.

            String regex = Sift.fromPreviousMatchEnd()
                    .oneOrMore().wordCharacters()
                    .shake();

            assertEquals("\\G[\\w]+", regex);

            Pattern pattern = Pattern.compile(regex);

            // The string contains a non-word character (the comma) in the middle
            Matcher matcher = pattern.matcher("OneTwo,Three");

            // First match: starts from the beginning and finds "OneTwo"
            assertTrue(matcher.find());
            assertEquals("OneTwo", matcher.group());

            // Second match: should start EXACTLY where the first one ended (index 6).
            // There is a comma there. Since we look for word characters, the match fails instantly.
            // If we hadn't used \G, the engine would have skipped the comma and matched "Three".
            assertFalse(matcher.find(), "The matcher should have stopped at the comma due to the \\G anchor");
        }

        @Test
        @DisplayName("Should apply flags when starting from previous match end (\\G)")
        void flagsFromPreviousMatchEnd() {
            String regex = filteringWith(CASE_INSENSITIVE)
                    .fromPreviousMatchEnd()
                    .oneOrMore().letters()
                    .shake();

            assertEquals("(?i)\\G[a-zA-Z]+", regex);

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);

            java.util.regex.Matcher matcher = pattern.matcher("AbCd,EfGh");

            assertTrue(matcher.find());
            assertEquals("AbCd", matcher.group());

            assertFalse(matcher.find(), "The matcher should fail at the comma due to the \\G anchor");
        }
    }
}