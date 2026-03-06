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

import static com.mirkoddd.sift.core.SiftGlobalFlag.CASE_INSENSITIVE;
import static com.mirkoddd.sift.core.SiftGlobalFlag.MULTILINE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates the syntax sugar and shortcuts introduced in Sift 2.0.0.
 * Ensures that calling static and instance shortcuts generates the exact same
 * patterns as the traditional verbose builder chains.
 */
@DisplayName("Sift API Shortcuts Tests")
class SiftShortcutsTest {

    @Nested
    @DisplayName("Static Shortcuts (Unanchored Fragments)")
    class StaticShortcuts {

        @Test
        @DisplayName("exactly() shortcut should generate correct fragment")
        void testExactlyShortcut() {
            String regex = Sift.exactly(3).digits().shake();
            assertEquals("[0-9]{3}", regex);
        }

        @Test
        @DisplayName("atLeast() shortcut should generate correct fragment")
        void testAtLeastShortcut() {
            String regex = Sift.atLeast(2).letters().shake();
            assertEquals("[a-zA-Z]{2,}", regex);
        }

        @Test
        @DisplayName("between() shortcut should generate correct fragment")
        void testBetweenShortcut() {
            String regex = Sift.between(2, 5).alphanumeric().shake();
            assertEquals("[a-zA-Z0-9]{2,5}", regex);
        }

        @Test
        @DisplayName("oneOrMore() shortcut should generate correct fragment")
        void testOneOrMoreShortcut() {
            String regex = Sift.oneOrMore().whitespace().shake();
            assertEquals("[\\s]+", regex);
        }

        @Test
        @DisplayName("zeroOrMore() shortcut should generate correct fragment")
        void testZeroOrMoreShortcut() {
            String regex = Sift.zeroOrMore().nonDigits().shake();
            assertEquals("[\\D]*", regex);
        }

        @Test
        @DisplayName("optional() shortcut should generate correct fragment")
        void testOptionalShortcut() {
            String regex = Sift.optional().character('A').shake();
            assertEquals("A?", regex);
        }
    }

    @Nested
    @DisplayName("SiftStarter Shortcuts (Anchored Roots with Flags)")
    class StarterShortcuts {

        @Test
        @DisplayName("exactly() on Starter should generate flag-prepended root")
        void testStarterExactlyShortcut() {
            String regex = Sift.filteringWith(CASE_INSENSITIVE).exactly(3).letters().shake();
            assertEquals("(?i)[a-zA-Z]{3}", regex);
        }

        @Test
        @DisplayName("atLeast() on Starter should generate flag-prepended root")
        void testStarterAtLeastShortcut() {
            String regex = Sift.filteringWith(MULTILINE).atLeast(1).digits().shake();
            assertEquals("(?m)[0-9]{1,}", regex);
        }

        @Test
        @DisplayName("between() on Starter should handle multiple flags")
        void testStarterBetweenShortcut() {
            String regex = Sift.filteringWith(CASE_INSENSITIVE, MULTILINE).between(1, 3).anyCharacter().shake();
            assertEquals("(?im).{1,3}", regex);
        }

        @Test
        @DisplayName("oneOrMore() on Starter should generate flag-prepended root")
        void testStarterOneOrMoreShortcut() {
            String regex = Sift.filteringWith(CASE_INSENSITIVE).oneOrMore().wordCharacters().shake();
            assertEquals("(?i)[\\w]+", regex);
        }

        @Test
        @DisplayName("zeroOrMore() on Starter should generate flag-prepended root")
        void testStarterZeroOrMoreShortcut() {
            String regex = Sift.filteringWith(CASE_INSENSITIVE).zeroOrMore().character('-').shake();
            assertEquals("(?i)-*", regex);
        }

        @Test
        @DisplayName("optional() on Starter should generate flag-prepended root")
        void testStarterOptionalShortcut() {
            String regex = Sift.filteringWith(CASE_INSENSITIVE).optional().whitespace().shake();
            assertEquals("(?i)[\\s]?", regex);
        }
    }
}