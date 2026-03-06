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

import static com.mirkoddd.sift.core.SiftPatterns.anyOf;
import static com.mirkoddd.sift.core.SiftPatterns.anythingBut;
import static com.mirkoddd.sift.core.SiftPatterns.group;
import static com.mirkoddd.sift.core.SiftPatterns.literal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mirkoddd.sift.core.dsl.SiftPattern;

class SiftPatternsTest {

    private void assertRegexMatches(String regex, String input) {
        assertTrue(Pattern.compile(regex).matcher(input).find(),
                "Expected regex [" + regex + "] to match input [" + input + "]");
    }

    private void assertRegexDoesNotMatch(String regex, String input) {
        assertFalse(Pattern.compile(regex).matcher(input).find(),
                "Expected regex [" + regex + "] NOT to match input [" + input + "]");
    }

    @Test
    void testPrivateConstructorIsPrivateAndInvokable() throws Exception {
        // Reflection to check if the constructor is private and invokable
        Constructor<SiftPatterns> constructor = SiftPatterns.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");

        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    @DisplayName("Should throw exception when group name is null")
    void groupNameNullFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            SiftPatterns.capture(null, literal("abc"));
        }, "Should not allow null group names");
    }

    @Test
    @DisplayName("Should throw exception when group name is invalid (starts with digit or has symbols)")
    void groupNameInvalidFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            SiftPatterns.capture("123group", literal("abc"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            SiftPatterns.capture("group-name", literal("abc"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            SiftPatterns.capture("", literal("abc"));
        });
    }

    @Test
    @DisplayName("ToString should return the raw group name value")
    void groupNameToString() {
        String name = "validGroup123";
        GroupName groupName = GroupName.of(name);

        assertEquals(name, groupName.toString(),
                "toString() must return the exact string value for regex construction");
    }

    @Test
    @DisplayName("Equals and HashCode should follow strict Value Object rules")
    @SuppressWarnings("EqualsWithItself")
    void groupNameEquality() {
        GroupName group1 = GroupName.of("myGroup");
        GroupName group2 = GroupName.of("myGroup");
        GroupName group3 = GroupName.of("otherGroup");

        assertEquals(group1, group1);

        assertEquals(group1, group2);
        assertEquals(group2, group1);

        assertEquals(group1.hashCode(), group2.hashCode());

        assertNotEquals(group1, group3);
        assertNotEquals(group1.hashCode(), group3.hashCode());

        assertNotNull(group1, "A valid GroupName should never be equal to null");

        assertFalse(group1.equals(null), "equals(null) must explicitly return false");

        assertFalse(group1.equals(new Object()), "equals(differentClass) must explicitly return false");
    }

    @Test
    @DisplayName("group() should combine multiple patterns into a non-capturing block")
    void groupTest() {
        SiftPattern grouped = group(
                literal("Mr."),
                Sift.fromAnywhere().whitespace()
        );

        assertEquals("(?:Mr\\.[\\s])", grouped.shake());

        String regex = Sift.fromStart()
                .optional().pattern(grouped)
                .then().oneOrMore().letters()
                .andNothingElse()
                .shake();

        assertRegexMatches(regex, "Mr. John");
        assertRegexMatches(regex, "John");
        assertRegexDoesNotMatch(regex, "Mr.123");
    }

    @Test
    @DisplayName("Anti-Pattern: Manual concatenation with null creates a silent logic bug")
    void manualConcatenation_withNull_createsSilentBug() {
        // Arrange
        String userInput = null;

        // Act: The developer manually concatenates (Code Smell / Anti-Pattern).
        // In Java, "user_" + null evaluates to the valid string "user_null".
        String regex = literal("user_" + userInput).shake();

        // Assert: The Sift engine accepts it without errors, creating an invisible bug.
        assertEquals("user_null", regex, "It should silently generate a search for 'user_null'");
    }

    @Test
    @DisplayName("Best Practice: Using the DSL protects against null logic bugs (Fail-Fast)")
    void usingDsl_withNull_failsFast() {
        // Arrange
        String userInput = null;

        // Act & Assert: The developer uses the native DSL features (e.g., group or followedBy).
        // The Fail-Fast architecture immediately catches the null parameter.
        assertThrows(NullPointerException.class, () ->
                group(literal("user_"), literal(userInput))
        );
    }

    @Test
    void anythingBut_withEmptyString_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> anythingBut("")
        );

        assertEquals("Excluded characters string cannot be empty", exception.getMessage());
    }

    @Test
    void literal_withEmptyString_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> literal("")
        );

        assertEquals("Literal text cannot be empty. Use zero-width assertions if intentional.", exception.getMessage());
    }

    @Test
    void anyOf_withNullElementInVarargs_throwsNullPointerException() {
        SiftPattern validPattern = literal("test");
        assertThrows(NullPointerException.class, () ->
                anyOf(validPattern, validPattern, (SiftPattern) null)
        );
    }

    @Test
    void group_withNullElementInVarargs_throwsNullPointerException() {
        SiftPattern validPattern = literal("test");
        assertThrows(NullPointerException.class, () ->
                group(validPattern, (SiftPattern) null)
        );
    }

    @Test
    @DisplayName("Should fully cover the possessive assignment branches in PatternAssembler")
    void fullPossessiveBranchCoverage() {
        // --- 1. Method addAnyChar() ---
        // FALSE branch (empty quantifier)
        Sift.fromAnywhere().anyCharacter().shake();
        // TRUE branch (quantifier present)
        Sift.fromAnywhere().oneOrMore().anyCharacter().withoutBacktracking().shake();

        // --- 2. Method addCharacter() ---
        // FALSE branch
        Sift.fromAnywhere().character('a').shake();
        // TRUE branch
        Sift.fromAnywhere().zeroOrMore().character('a').withoutBacktracking().shake();

        // --- 3. Method flush() (used for character classes and patterns) ---
        // FALSE branch
        Sift.fromAnywhere().digits().shake();
        // TRUE branch
        Sift.fromAnywhere().optional().digits().withoutBacktracking().shake();
    }

    @Test
    @DisplayName("SiftPattern interface should correctly expose and execute sieve()")
    void testSiftPatternInterfaceSieveCoverage() {
        com.mirkoddd.sift.core.dsl.SiftPattern patternInterface =
                Sift.fromStart().exactly(3).digits();

        java.util.regex.Pattern compiled = patternInterface.sieve();

        assertNotNull(compiled, "The interface method should return a valid Pattern");
        assertEquals("^[0-9]{3}", compiled.pattern());
    }

    @Test
    @DisplayName("SiftPattern default sieve() should compile the result of shake()")
    void testDefaultSieveImplementation() {
        // We can no longer use a lambda due to the security sealing of the interface.
        // We use an anonymous class that explicitly fulfills the internal contract.
        com.mirkoddd.sift.core.dsl.SiftPattern anonymousPattern = new com.mirkoddd.sift.core.dsl.SiftPattern() {
            @Override
            public String shake() {
                return "[a-z]+";
            }

            @Override
            public void preventExternalImplementation(com.mirkoddd.sift.core.InternalToken token) {
                // Intentionally left blank for testing purposes
            }
        };

        java.util.regex.Pattern compiled = anonymousPattern.sieve();

        assertNotNull(compiled, "The default sieve() should return a valid Pattern");
        assertEquals("[a-z]+", compiled.pattern(), "The compiled pattern should match the shake() output");
        assertTrue(anonymousPattern.matches("abc"), "The default matches() should correctly match valid strings");
    }

    @Test
    void shouldMemoizeShakeAndSieveResults() {
        // We use literal() as it's wrapped by the memoize() helper
        SiftPattern memoizedPattern = SiftPatterns.literal("cache-test");

        // 1. Verify shake() caching
        String firstShake = memoizedPattern.shake();
        String secondShake = memoizedPattern.shake();

        assertEquals("cache-test", firstShake);
        assertSame(firstShake, secondShake, "shake() should return the exact same String instance from cache.");

        // 2. Verify sieve() caching
        Pattern firstSieve = memoizedPattern.sieve();
        Pattern secondSieve = memoizedPattern.sieve();

        assertEquals("cache-test", firstSieve.pattern());
        assertSame(firstSieve, secondSieve, "sieve() should return the exact same Pattern instance from cache.");
    }

    @Test
    @DisplayName("preventExternalImplementation() should throw SecurityException when passed a null token")
    void testPreventExternalImplementationThrowsSecurityExceptionOnNull() {
        com.mirkoddd.sift.core.dsl.SiftPattern pattern = com.mirkoddd.sift.core.Sift.fromStart()
                .exactly(1).digits()
                .andNothingElse();

        SecurityException exception = assertThrows(SecurityException.class, () -> pattern.preventExternalImplementation(null),
                "Calling preventExternalImplementation() with a null token must throw a SecurityException");

        assertTrue(exception.getMessage().contains("External implementation of SiftPattern is not allowed."),
                "The exception message should clearly state the security restriction");
    }

    @Test
    @DisplayName("preventExternalImplementation() should execute silently when passed a valid token (Branch Coverage)")
    void testPreventExternalImplementationExecutesSilentlyOnValidToken() throws Exception {
        // Using Reflection to instantiate the package-private token and cover the 'if (token != null)' branch
        java.lang.reflect.Constructor<com.mirkoddd.sift.core.InternalToken> constructor =
                com.mirkoddd.sift.core.InternalToken.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        com.mirkoddd.sift.core.InternalToken validToken = constructor.newInstance();

        com.mirkoddd.sift.core.dsl.SiftPattern pattern = com.mirkoddd.sift.core.Sift.fromStart()
                .exactly(1).digits()
                .andNothingElse();

        assertDoesNotThrow(() -> pattern.preventExternalImplementation(validToken),
                "Calling preventExternalImplementation() with a valid token should bypass the if-statement and execute silently");
    }

    @Test
    @DisplayName("preventExternalImplementation() inside memoize() anonymous class should execute silently")
    void testMemoizePreventExternalImplementationImplementation() {
        com.mirkoddd.sift.core.dsl.SiftPattern memoizedPattern = com.mirkoddd.sift.core.SiftPatterns.literal("coverage-test");

        assertDoesNotThrow(() -> memoizedPattern.preventExternalImplementation(null),
                "Calling preventExternalImplementation() on a memoized pattern should not throw any exceptions");
    }

    @Test
    @DisplayName("preventExternalImplementation() inside preventBacktracking() anonymous class should execute silently")
    void testPreventBacktrackingPreventExternalImplementationImplementation() {
        com.mirkoddd.sift.core.dsl.SiftPattern basePattern = com.mirkoddd.sift.core.Sift.fromStart()
                .exactly(1).digits()
                .andNothingElse();

        com.mirkoddd.sift.core.dsl.SiftPattern atomicPattern = basePattern.preventBacktracking();

        assertDoesNotThrow(() -> atomicPattern.preventExternalImplementation(null),
                "Calling preventExternalImplementation() on an atomic group pattern should not throw any exceptions");
    }

    @Test
    @DisplayName("anyOf(List) should throw IllegalArgumentException for null or empty lists")
    void testAnyOfListNullOrEmpty() {
        IllegalArgumentException nullEx = assertThrows(IllegalArgumentException.class,
                () -> SiftPatterns.anyOf((java.util.List<SiftPattern>) null));
        assertTrue(nullEx.getMessage().contains("requires at least one pattern"));

        IllegalArgumentException emptyEx = assertThrows(IllegalArgumentException.class,
                () -> SiftPatterns.anyOf(java.util.Collections.emptyList()));
        assertTrue(emptyEx.getMessage().contains("requires at least one pattern"));
    }

    @Test
    @DisplayName("anyOf(List) should optimize single-element lists by avoiding unnecessary grouping")
    void testAnyOfListSingleElementOptimization() {
        SiftPattern singlePattern = Sift.fromStart().digits();

        SiftPattern result = SiftPatterns.anyOf(java.util.Collections.singletonList(singlePattern));

        assertEquals("^[0-9]", result.shake(),
                "Should return the exact pattern without the (?:...) wrapper overhead");
    }

    @Test
    @DisplayName("anyOf(List) should wrap multiple elements in a non-capturing OR group")
    void testAnyOfListMultipleElements() {
        SiftPattern p1 = Sift.fromAnywhere().letters();
        SiftPattern p2 = Sift.fromAnywhere().digits();
        SiftPattern p3 = Sift.fromAnywhere().character('-');

        SiftPattern result = SiftPatterns.anyOf(java.util.Arrays.asList(p1, p2, p3));

        assertEquals("(?:[a-zA-Z]|[0-9]|-)", result.shake(),
                "Should accurately wrap multiple patterns separated by the OR operator");
    }

    @Test
    @DisplayName("matches(CharSequence) should correctly evaluate inputs, handle nulls, and support StringBuilders")
    void testPatternMatchesConvenienceMethod() {
        SiftPattern pattern = Sift.fromStart().exactly(3).digits().andNothingElse();

        // Branch 1: Null safety
        assertFalse(pattern.matches(null),
                "Should gracefully return false when the input is null, avoiding NullPointerException");

        // Branch 2: Valid match (Standard String)
        assertTrue(pattern.matches("123"),
                "Should return true for a string that perfectly matches the pattern");

        // Branch 3: Invalid match
        assertFalse(pattern.matches("12a"),
                "Should return false for a string with invalid characters");
        assertFalse(pattern.matches("1234"),
                "Should return false for a string that exceeds the exact length bounds");

        // Branch 4: CharSequence polymorphism (Zero-cost abstraction)
        StringBuilder sbInput = new StringBuilder("987");
        assertTrue(pattern.matches(sbInput),
                "Should natively support other CharSequence implementations like StringBuilder without allocations");
    }

    @Test
    @DisplayName("Should throw exception when passing anchored pattern to SiftPatterns factories")
    void shouldThrowWhenPassingAnchoredPatternToFactories() {
        SiftPattern radioActivePattern = Sift.fromStart().exactly(3).digits();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                SiftPatterns.positiveLookahead(radioActivePattern));

        assertTrue(exception.getMessage().contains("absolute boundaries"),
                "Exception message should indicate the boundary violation.");

        assertThrows(IllegalStateException.class, () ->
                SiftPatterns.capture("invalidGroup", radioActivePattern));
    }
}