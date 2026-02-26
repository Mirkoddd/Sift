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
}