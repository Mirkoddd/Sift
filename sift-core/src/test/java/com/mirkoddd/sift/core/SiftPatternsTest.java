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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mirkoddd.sift.core.dsl.Assertion;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.Root;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

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
        Constructor<SiftPatterns> constructor = SiftPatterns.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");

        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    @DisplayName("Should throw exception when group name is null")
    void groupNameNullFailure() {
        assertThrows(IllegalArgumentException.class, () ->
                SiftPatterns.capture(null, literal("abc")), "Should not allow null group names");
    }

    @Test
    @DisplayName("Should throw exception when group name is invalid (starts with digit or has symbols)")
    void groupNameInvalidFailure() {
        assertThrows(IllegalArgumentException.class, () ->
                SiftPatterns.capture("123group", literal("abc")));

        assertThrows(IllegalArgumentException.class, () ->
                SiftPatterns.capture("group-name", literal("abc")));

        assertThrows(IllegalArgumentException.class, () ->
                SiftPatterns.capture("", literal("abc")));
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
    @SuppressWarnings("EqualsWithItself, ConstantConditions")
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

        boolean isGroupEqualsNull = group1.equals(null);
        assertFalse(isGroupEqualsNull, "equals(null) must explicitly return false");

        boolean isGroupEqualsToObject = group1.equals(new Object());
        assertFalse(isGroupEqualsToObject, "equals(differentClass) must explicitly return false");
    }

    @Test
    @DisplayName("group() should combine multiple patterns into a non-capturing block")
    void groupTest() {
        SiftPattern<Fragment> grouped = group(
                literal("Mr."),
                Sift.fromAnywhere().whitespace()
        );

        assertEquals("(?:Mr\\.[\\s])", grouped.shake());

        String regex = Sift.fromStart()
                .optional().of(grouped)
                .then().oneOrMore().letters()
                .andNothingElse()
                .shake();

        assertRegexMatches(regex, "Mr. John");
        assertRegexMatches(regex, "John");
        assertRegexDoesNotMatch(regex, "Mr.123");
        assertRegexDoesNotMatch(regex, "Mr.1");
    }

    @Test
    @DisplayName("Anti-Pattern: Manual concatenation with null creates a silent logic bug")
    void manualConcatenation_withNull_createsSilentBug() {
        String regex = literal("user_" + null).shake();

        assertEquals("user_null", regex, "It should silently generate a search for 'user_null'");
    }

    @Test
    @DisplayName("Best Practice: Using the DSL protects against null logic bugs (Fail-Fast)")
    void usingDsl_withNull_failsFast() {
        assertThrows(NullPointerException.class, () ->
                group(literal("user_"), literal(null))
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
        SiftPattern<Fragment> validPattern = literal("test");
        assertThrows(NullPointerException.class, () ->
                anyOf(validPattern, validPattern, (SiftPattern<Fragment>) null)
        );
    }

    @Test
    void group_withNullElementInVarargs_throwsNullPointerException() {
        SiftPattern<Fragment> validPattern = literal("test");
        assertThrows(NullPointerException.class, () ->
                group(validPattern, (SiftPattern<Fragment>) null)
        );
    }

    @Test
    @DisplayName("Should fully cover the possessive assignment branches in PatternAssembler")
    void fullPossessiveBranchCoverage() {
        Sift.fromAnywhere().anyCharacter().shake();
        Sift.fromAnywhere().oneOrMore().anyCharacter().withoutBacktracking().shake();

        Sift.fromAnywhere().character('a').shake();
        Sift.fromAnywhere().zeroOrMore().character('a').withoutBacktracking().shake();

        Sift.fromAnywhere().digits().shake();
        Sift.fromAnywhere().optional().digits().withoutBacktracking().shake();
    }

    @Test
    @DisplayName("SiftPattern interface should correctly expose and execute sieve()")
    void testSiftPatternInterfaceSieveCoverage() {
        SiftPattern<Root> patternInterface =
                Sift.fromStart().exactly(3).digits();

        SiftCompiledPattern compiled = patternInterface.sieve();

        assertNotNull(compiled, "The interface method should return a valid Compiled Pattern");
        assertEquals("^[0-9]{3}", compiled.getRawRegex());
    }

    @Test
    @DisplayName("BaseSiftPattern execution methods should correctly delegate to the default engine")
    void testDefaultSieveImplementation() {
        // We use a real AST node created by the factory instead of a fake anonymous class
        SiftPattern<Fragment> pattern = SiftPatterns.anyOf(literal("abc"), literal("def"));

        SiftCompiledPattern compiled = pattern.sieve();

        assertNotNull(compiled, "sieve() should return a valid SiftCompiledPattern");
        assertEquals("(?:abc|def)", compiled.getRawRegex(), "The compiled pattern should match the AST evaluation");

        assertTrue(pattern.matchesEntire("abc"), "matchesEntire() should correctly match valid strings");
        assertTrue(pattern.containsMatchIn("123abc456"), "containsMatchIn() should correctly find valid substrings");
        assertFalse(pattern.matchesEntire("123"), "matchesEntire() should correctly reject invalid strings");

        // Use a cast to check the internal lock since it's package-private
        assertEquals(InternalToken.INSTANCE, (pattern).___internal_lock___(),
                "Internal lock must return the Singleton Enum instance");
    }

    @Test
    @DisplayName("shake() result should be aggressively memoized and thread-safe")
    void shouldMemoizeShakeResult() {
        SiftPattern<Fragment> memoizedPattern = SiftPatterns.literal("cache-test");

        String firstShake = memoizedPattern.shake();
        String secondShake = memoizedPattern.shake();

        assertEquals("cache-test", firstShake);
        assertSame(firstShake, secondShake, "shake() should return the exact same String instance from cache.");
    }

    @Test
    @DisplayName("anyOf(List) should throw IllegalArgumentException for null or empty lists")
    void testAnyOfListNullOrEmpty() {
        IllegalArgumentException nullEx = assertThrows(IllegalArgumentException.class,
                () -> SiftPatterns.anyOf(null));
        assertTrue(nullEx.getMessage().contains("requires at least one pattern"));

        IllegalArgumentException emptyEx = assertThrows(IllegalArgumentException.class,
                () -> SiftPatterns.anyOf(java.util.Collections.emptyList()));
        assertTrue(emptyEx.getMessage().contains("requires at least one pattern"));
    }

    @Test
    @DisplayName("anyOf(List) should optimize single-element lists by avoiding unnecessary grouping")
    void testAnyOfListSingleElementOptimization() {
        SiftPattern<Fragment> singlePattern = Sift.fromAnywhere().digits();

        SiftPattern<Fragment> result = SiftPatterns.anyOf(java.util.Collections.singletonList(singlePattern));

        assertEquals("[0-9]", result.shake(),
                "Should return the exact pattern without the (?:...) wrapper overhead");
    }

    @Test
    @DisplayName("anyOf(List) should wrap multiple elements in a non-capturing OR group")
    void testAnyOfListMultipleElements() {
        SiftPattern<Fragment> p1 = Sift.fromAnywhere().letters();
        SiftPattern<Fragment> p2 = Sift.fromAnywhere().digits();
        SiftPattern<Fragment> p3 = Sift.fromAnywhere().character('-');

        SiftPattern<Fragment> result = SiftPatterns.anyOf(java.util.Arrays.asList(p1, p2, p3));

        assertEquals("(?:[a-zA-Z]|[0-9]|-)", result.shake(),
                "Should accurately wrap multiple patterns separated by the OR operator");
    }

    @Test
    @DisplayName("Semantic matching methods should correctly evaluate inputs, handle nulls, and support StringBuilders")
    void testPatternMatchingConvenienceMethods() {
        SiftPattern<Root> pattern = Sift.fromStart().exactly(3).digits().andNothingElse();

        // matchesEntire tests
        assertFalse(pattern.matchesEntire(null),
                "Should gracefully return false when the input is null, avoiding NullPointerException");
        assertTrue(pattern.matchesEntire("123"),
                "Should return true for a string that perfectly matches the pattern");
        assertFalse(pattern.matchesEntire("12a"),
                "Should return false for a string with invalid characters");
        assertFalse(pattern.matchesEntire("1234"),
                "Should return false for a string that exceeds the exact length bounds");

        StringBuilder sbInput = new StringBuilder("987");
        assertTrue(pattern.matchesEntire(sbInput),
                "Should natively support other CharSequence implementations like StringBuilder without allocations");

        // containsMatchIn tests
        SiftPattern<Fragment> partialPattern = Sift.fromAnywhere().exactly(3).digits();
        assertTrue(partialPattern.containsMatchIn("abc123def"),
                "Should return true when the exact sequence is contained within a larger string");
        assertFalse(partialPattern.containsMatchIn("ab12cd"),
                "Should return false when the sequence is broken");
        assertFalse(partialPattern.containsMatchIn(null),
                "Should gracefully return false when the input is null, avoiding NullPointerException");
    }

    @Test
    @DisplayName("AST Structural nodes should safely delegate quantification to the parent node")
    void structuralNodesQuantifierCoverage() {
        // 1. anyOf
        SiftPattern<Fragment> anyOfNode = SiftPatterns.anyOf(literal("A"), literal("B"));
        assertEquals("^(?:(?:A|B))++", Sift.fromStart().oneOrMore().of(anyOfNode).withoutBacktracking().shake());

        // 2. captureGroup
        SiftPattern<Fragment> captureNode = SiftPatterns.capture(literal("A"));
        assertEquals("^(?:(A))++", Sift.fromStart().oneOrMore().of(captureNode).withoutBacktracking().shake());

        // 3. nonCapturingGroup
        SiftPattern<Fragment> nonCapNode = SiftPatterns.group(literal("A"), literal("B"));
        assertEquals("^(?:(?:AB))++", Sift.fromStart().oneOrMore().of(nonCapNode).withoutBacktracking().shake());

        // 4. lookaround (Positive Lookahead)
        SiftPattern<Assertion> lookaroundNode = SiftPatterns.positiveLookahead(literal("A"));
        assertEquals("^(?=A)", Sift.fromStart().followedByAssertion(lookaroundNode).shake());
    }
}