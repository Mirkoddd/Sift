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

import static com.mirkoddd.sift.core.Sift.exactly;
import static com.mirkoddd.sift.core.Sift.oneOrMore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.JdkEngine;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

@DisplayName("SiftPattern Execution & Extraction Tests")
class SiftPatternExecutionTest {

    // Helper method: matches only letters
    private SiftPattern<?> createWordPattern() {
        return Sift.fromAnywhere().oneOrMore().lettersUnicode();
    }

    // Helper method: matches digits (useful for testing splits)
    private SiftPattern<?> createDigitPattern() {
        return Sift.fromAnywhere().oneOrMore().digits();
    }

    @Nested
    @DisplayName("Extraction Methods")
    class ExtractionTests {

        @Test
        @DisplayName("extractFirst() should return the first match or empty")
        void testExtractFirst() {
            SiftPattern<?> pattern = createWordPattern();

            assertEquals(Optional.of("Hello"), pattern.extractFirst("Hello world 123"));
            assertEquals(Optional.empty(), pattern.extractFirst("123 456")); // No words in the input
        }

        @Test
        @DisplayName("extractAll() should return all matches as a list")
        void testExtractAll() {
            SiftPattern<?> pattern = createWordPattern();
            List<String> results = pattern.extractAll("Java 8, Java 11, and Java 21");

            assertEquals(4, results.size());
            assertEquals("Java", results.get(0));
            assertEquals("Java", results.get(1));
            assertEquals("and", results.get(2));
            assertEquals("Java", results.get(3));
        }

        @Test
        @DisplayName("streamMatches() should return a lazy stream of matches")
        void testStreamMatches() {
            SiftPattern<?> pattern = createWordPattern();

            // Collect the stream into a list to verify its contents
            List<String> results = pattern.streamMatches("Java 8, Java 11, and Java 21")
                    .collect(Collectors.toList());

            assertEquals(4, results.size());
            assertEquals("Java", results.get(0));
            assertEquals("Java", results.get(1));
            assertEquals("and", results.get(2));
            assertEquals("Java", results.get(3));
        }

        @Test
        @DisplayName("streamMatches() should return an empty stream if no match is found")
        void testStreamMatchesNoMatch() {
            SiftPattern<?> pattern = createWordPattern();
            long count = pattern.streamMatches("123 456 789").count();

            assertEquals(0, count, "The stream should be empty when there are no matches");
        }
    }

    @Nested
    @DisplayName("Split Methods")
    class SplitTests {

        @Test
        @DisplayName("splitBy() should split the input sequence around matches")
        void testSplitBy() {
            // We use digits as the delimiter to split the string
            SiftPattern<?> digitSplitter = createDigitPattern();
            List<String> result = digitSplitter.splitBy("apple123banana456cherry");

            assertEquals(3, result.size());
            assertEquals("apple", result.get(0));
            assertEquals("banana", result.get(1));
            assertEquals("cherry", result.get(2));
        }

        @Test
        @DisplayName("splitBy() should return a single-element list with the original string if no match is found")
        void testSplitByNoMatch() {
            SiftPattern<?> pattern = createWordPattern();
            // Since there are no letters to act as delimiters, it should not split
            List<String> result = pattern.splitBy("123456");

            assertEquals(1, result.size());
            assertEquals("123456", result.get(0));
        }
    }

    @Nested
    @DisplayName("Replacement Methods")
    class ReplacementTests {

        @Test
        @DisplayName("replaceFirst() should replace only the first occurrence")
        void testReplaceFirst() {
            SiftPattern<?> pattern = createWordPattern();
            String result = pattern.replaceFirst("apple banana apple", "orange");

            assertEquals("orange banana apple", result);
        }

        @Test
        @DisplayName("replaceAll() should replace all occurrences")
        void testReplaceAll() {
            SiftPattern<?> pattern = createWordPattern();
            String result = pattern.replaceAll("apple banana apple", "orange");

            assertEquals("orange orange orange", result);
        }
    }

    @Nested
    @DisplayName("Named Groups Extraction")
    class NamedGroupsTests {

        @Test
        @DisplayName("extractGroups() should map captured group names to their values")
        void testExtractGroups() {

            NamedCapture yearGroup = SiftPatterns.capture("year", exactly(4).digits());
            NamedCapture monthGroup = SiftPatterns.capture("month", exactly(2).digits());
            NamedCapture dateGroup = SiftPatterns.capture("day", exactly(2).digits());

            SiftPattern<?> datePattern = Sift.fromStart()
                    .namedCapture(yearGroup)
                    .followedBy('-')
                    .then().namedCapture(monthGroup)
                    .followedBy('-')
                    .then().namedCapture(dateGroup)
                    .andNothingElse();

            Map<String, String> groups = datePattern.extractGroups("2026-03-13");

            assertEquals(3, groups.size());
            assertEquals("2026", groups.get("year"));
            assertEquals("03", groups.get("month"));
            assertEquals("13", groups.get("day"));
        }

        @Test
        @DisplayName("extractGroups() should return an empty map if no match is found")
        void testExtractGroupsNoMatch() {
            NamedCapture idGroup = SiftPatterns.capture("id", oneOrMore().digits());
            SiftPattern<?> pattern = Sift.fromStart()
                    .namedCapture(idGroup);

            Map<String, String> groups = pattern.extractGroups("not-an-id");

            assertTrue(groups.isEmpty());
        }

        @Test
        @DisplayName("extractAllGroups() should return a list of maps for all matches")
        void testExtractAllGroups() {
            NamedCapture idGroup = SiftPatterns.capture("id", exactly(3).digits());
            NamedCapture codeGroup = SiftPatterns.capture("code", exactly(2).lettersUnicode());

            SiftPattern<?> pattern = Sift.fromAnywhere()
                    .namedCapture(idGroup)
                    .followedBy('-')
                    .then().namedCapture(codeGroup);

            List<Map<String, String>> results = pattern.extractAllGroups("ID: 123-AB and ID: 456-XY");

            assertEquals(2, results.size());

            assertEquals("123", results.get(0).get("id"));
            assertEquals("AB", results.get(0).get("code"));

            assertEquals("456", results.get(1).get("id"));
            assertEquals("XY", results.get(1).get("code"));
        }

        @Test
        @DisplayName("extractAllGroups() should return an empty list if no match is found")
        void testExtractAllGroupsNoMatch() {
            NamedCapture idGroup = SiftPatterns.capture("id", oneOrMore().digits());
            SiftPattern<?> pattern = Sift.fromStart()
                    .namedCapture(idGroup);

            List<Map<String, String>> results = pattern.extractAllGroups("no-digits-here");

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Null Safety & Edge Cases")
    class NullSafetyTests {

        @Test
        @DisplayName("All methods should gracefully handle null inputs without throwing NullPointerException")
        void testNullInputs() {
            SiftPattern<?> pattern = createWordPattern();

            assertEquals(Optional.empty(), pattern.extractFirst(null));
            assertTrue(pattern.extractAll(null).isEmpty());
            assertNull(pattern.replaceFirst(null, "replacement"));
            assertNull(pattern.replaceAll(null, "replacement"));
            assertTrue(pattern.extractGroups(null).isEmpty());
            assertTrue(pattern.extractAllGroups(null).isEmpty());
            assertTrue(pattern.splitBy(null).isEmpty());
            assertEquals(0L, pattern.streamMatches(null).count());
        }

        @Test
        @DisplayName("extractGroups() should gracefully handle false-positive group names (IllegalArgumentException)")
        void testExtractGroupsIllegalArgumentExceptionCoverage() {
            // Test the engine directly with an edge-case raw regex string
            try (SiftCompiledPattern edgeCasePattern = JdkEngine.INSTANCE.compile("\\Q(?<fake>)\\E", Collections.emptySet())) {

                // The catch block must intercept the exception, ignore the false-positive group
                // and return an empty map, without crashing the application.
                Map<String, String> groups = edgeCasePattern.extractGroups("prefix (?<fake>) suffix");

                assertTrue(groups.isEmpty(), "The map should be empty, ignoring the false-positive group");
            }
        }

        @Test
        @DisplayName("extractGroups() should ignore groups that are optional and not present in the match (matchValue == null)")
        void testExtractGroupsWithUnmatchedOptionalGroup() {
            // Test the engine directly with an optional capture group
            try (SiftCompiledPattern pattern = JdkEngine.INSTANCE.compile("^(?<req>[a-z]+)(?<opt>[0-9]+)?$", Collections.emptySet())) {

                // Pass a string that matches the "req" group but NOT the optional "opt" group
                Map<String, String> groups = pattern.extractGroups("abc");

                // The "opt" group returned null, so the key was not inserted into the map.
                assertEquals(1, groups.size());
                assertEquals("abc", groups.get("req"));
                assertFalse(groups.containsKey("opt"), "The optional group should not be in the map");
            }
        }

        @Test
        @DisplayName("extractAllGroups() should gracefully handle false-positive group names (IllegalArgumentException)")
        void testExtractAllGroupsIllegalArgumentExceptionCoverage() {
            try (SiftCompiledPattern edgeCasePattern = JdkEngine.INSTANCE.compile("\\Q(?<fake>)\\E", Collections.emptySet())) {

                List<Map<String, String>> results = edgeCasePattern.extractAllGroups("prefix (?<fake>) suffix");

                assertEquals(1, results.size(), "Should find one match based on the literal string");
                assertTrue(results.get(0).isEmpty(), "The map for the match should be empty, ignoring the false-positive group");
            }
        }

        @Test
        @DisplayName("extractAllGroups() should ignore groups that are optional and not present in the match (matchValue == null)")
        void testExtractAllGroupsWithUnmatchedOptionalGroup() {
            try (SiftCompiledPattern pattern = JdkEngine.INSTANCE.compile("(?<req>[a-z]+)(?<opt>[0-9]+)?", Collections.emptySet())) {

                // Matches "abc" (no digits) and "def12" (with digits)
                List<Map<String, String>> results = pattern.extractAllGroups("abc def12");

                assertEquals(2, results.size());

                // First match: only 'req' is present
                assertEquals("abc", results.get(0).get("req"));
                assertFalse(results.get(0).containsKey("opt"));

                // Second match: both 'req' and 'opt' are present
                assertEquals("def", results.get(1).get("req"));
                assertEquals("12", results.get(1).get("opt"));
            }
        }
    }
}