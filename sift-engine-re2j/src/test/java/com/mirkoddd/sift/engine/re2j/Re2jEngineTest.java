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
package com.mirkoddd.sift.engine.re2j;

import static com.mirkoddd.sift.core.SiftPatterns.ifFollowedBy;
import static com.mirkoddd.sift.core.SiftPatterns.ifPrecededBy;
import static com.mirkoddd.sift.core.SiftPatterns.literal;

import com.mirkoddd.sift.core.Delimiter;
import com.mirkoddd.sift.core.NamedCapture;
import com.mirkoddd.sift.core.Sift;
import com.mirkoddd.sift.core.SiftPatterns;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class Re2jEngineTest {

    @Test
    @DisplayName("Should correctly match and extract using RE2J")
    void testBasicMatchingAndExtraction() {
        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .between(1, 3).digits()
                .followedBy('-')
                .then().exactly(2).letters()
                .sieveWith(Re2jEngine.INSTANCE)) {

            assertTrue(pattern.containsMatchIn("Order 12-ab confirmed"));
            assertFalse(pattern.matchesEntire("12-ab extra"));
            assertTrue(pattern.matchesEntire("12-ab"));

            Optional<String> first = pattern.extractFirst("IDs: 1-xy, 999-zz, 42-kk");
            assertTrue(first.isPresent());
            assertEquals("1-xy", first.get());

            List<String> all = pattern.extractAll("IDs: 1-xy, 999-zz, 42-kk");
            assertEquals(3, all.size());
            assertEquals("999-zz", all.get(1));
        }
    }

    @Test
    @DisplayName("Should correctly extract named capture groups using RE2J fallback")
    void testNamedGroupExtraction() {
        NamedCapture yearGroup = SiftPatterns.capture("year", Sift.exactly(4).digits());
        NamedCapture monthGroup = SiftPatterns.capture("month", Sift.exactly(2).digits());

        try (SiftCompiledPattern datePattern = Sift.fromStart()
                .namedCapture(yearGroup)
                .followedBy('-')
                .then().namedCapture(monthGroup)
                .sieveWith(Re2jEngine.INSTANCE)) {

            Map<String, String> groups = datePattern.extractGroups("2026-03");
            assertEquals("2026", groups.get("year"));
            assertEquals("03", groups.get("month"));
        }
    }

    @Test
    @DisplayName("Null safety: should handle null inputs gracefully")
    void testNullSafety() {
        try (SiftCompiledPattern pattern = Sift.fromStart().digits().sieveWith(Re2jEngine.INSTANCE)) {
            assertFalse(pattern.containsMatchIn(null));
            assertFalse(pattern.matchesEntire(null));
            assertEquals(Optional.empty(), pattern.extractFirst(null));
            assertTrue(pattern.extractAll(null).isEmpty());
            assertNull(pattern.replaceFirst(null, "test"));
            assertNull(pattern.replaceAll(null, "test"));
            assertTrue(pattern.splitBy(null).isEmpty());
            assertEquals(0, pattern.streamMatches(null).count());
            assertTrue(pattern.extractGroups(null).isEmpty());
            assertTrue(pattern.extractAllGroups(null).isEmpty());
        }
    }

    @Test
    @DisplayName("Engine Guard: should reject Lookaheads")
    void testEngineGuardRejectsLookahead() {
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = Sift.fromAnywhere()
                    .exactly(3).digits()
                    .mustBeFollowedBy(SiftPatterns.literal("EUR")) // Lookahead
                    .sieveWith(Re2jEngine.INSTANCE)) {

                fail("The engine should have rejected the lookahead syntax before entering the block");
            }
        });
    }

    @Test
    @DisplayName("Engine Guard: should reject Lookbehinds")
    void testEngineGuardRejectsLookbehind() {
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = Sift.fromAnywhere()
                    .oneOrMore().digits()
                    .mustBePrecededBy(SiftPatterns.literal("$"))
                    .sieveWith(Re2jEngine.INSTANCE)) {

                fail("The engine should have rejected the lookbehind syntax before entering the block");
            }
        });
    }

    @Test
    @DisplayName("Engine Guard: should reject Backreferences")
    void testEngineGuardRejectsBackreference() {
        NamedCapture quote = SiftPatterns.capture("quote", Sift.fromAnywhere().character('"'));

        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = Sift.fromAnywhere()
                    .namedCapture(quote)
                    .then().oneOrMore().letters()
                    .then().backreference(quote) // Backreference
                    .sieveWith(Re2jEngine.INSTANCE)) {

                fail("The engine should have rejected the backreference syntax before entering the block");
            }
        });
    }

    @Test
    @DisplayName("Engine Guard: should reject \\G Anchor")
    void testEngineGuardRejectsPreviousMatchAnchor() {
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = Sift.fromPreviousMatchEnd() // \G anchor
                    .oneOrMore().digits()
                    .sieveWith(Re2jEngine.INSTANCE)) {

                fail("The engine should have rejected the \\G anchor syntax before entering the block");
            }
        });
    }

    @Test
    @DisplayName("Engine Guard: should reject Atomic Groups")
    void testEngineGuardRejectsAtomicGroup() {
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = Sift.fromAnywhere()
                    .oneOrMore().digits()
                    .preventBacktracking() // Atomic group
                    .sieveWith(Re2jEngine.INSTANCE)) {

                fail("The engine should have rejected the atomic group syntax before entering the block");
            }
        });
    }

    @Test
    @DisplayName("Engine Guard: should reject Recursion")
    void testEngineGuardRejectsRecursion() {
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = SiftPatterns.nesting(3) // Recursion
                    .using(Delimiter.PARENTHESES)
                    .containing(Sift.fromAnywhere().letters())
                    .sieveWith(Re2jEngine.INSTANCE)) {

                fail("The engine should have rejected the recursion syntax before entering the block");
            }
        });
    }

    @Test
    @DisplayName("Engine Guard: should reject Conditionals")
    void testEngineGuardRejectsConditionals() {
        assertThrows(UnsupportedOperationException.class, () -> {
            SiftPattern<Fragment> serialRule = ifFollowedBy(literal("a"))
                    .thenUse(literal("option a"))
                    .otherwiseNothing();

            try (SiftCompiledPattern ignored = serialRule.sieveWith(Re2jEngine.INSTANCE)) {
                fail("The engine should have rejected the conditional syntax before entering the block");
            }
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            SiftPattern<Fragment> serialRule = ifPrecededBy(literal("a"))
                    .thenUse(literal("option a"))
                    .otherwiseUse(literal("option b"));

            try (SiftCompiledPattern ignored = serialRule.sieveWith(Re2jEngine.INSTANCE)) {
                fail("The engine should have rejected the conditional syntax before entering the block");
            }
        });
    }

    @Test
    @DisplayName("Should correctly replace first match")
    void testReplaceFirst() {
        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .exactly(2).digits()
                .sieveWith(Re2jEngine.INSTANCE)) {

            String result = pattern.replaceFirst("ID-42 and ID-99", "XX");
            assertEquals("ID-XX and ID-99", result);
        }
    }

    @Test
    @DisplayName("Should correctly replace all matches")
    void testReplaceAll() {
        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .exactly(2).digits()
                .sieveWith(Re2jEngine.INSTANCE)) {

            String result = pattern.replaceAll("ID-42 and ID-99", "XX");
            assertEquals("ID-XX and ID-XX", result);
        }
    }

    @Test
    @DisplayName("Should correctly split sequence by pattern")
    void testSplitBy() {
        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .character(',')
                .then().optional().character(' ')
                .sieveWith(Re2jEngine.INSTANCE)) {

            List<String> result = pattern.splitBy("apple, banana,orange, kiwi");
            assertEquals(Arrays.asList("apple", "banana", "orange", "kiwi"), result);
        }
    }

    @Test
    @DisplayName("Should preserve trailing empty strings in splitBy, matching JDK String::split(-1) behavior")
    void testSplitByTrailingEmptyStrings() {
        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .character(',')
                .sieveWith(Re2jEngine.INSTANCE)) {

            List<String> result = pattern.splitBy("a,b,");

            assertEquals(3, result.size());
            assertEquals(Arrays.asList("a", "b", ""), result);
        }
    }

    @Test
    @DisplayName("Should correctly stream matches lazily")
    void testStreamMatches() {
        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .oneOrMore().letters()
                .sieveWith(Re2jEngine.INSTANCE)) {

            long longWordsCount = pattern.streamMatches("hello 123 world 45 test")
                    .filter(s -> s.length() > 4)
                    .count();

            assertEquals(2, longWordsCount); // "hello" and "world"
        }
    }

    @Test
    @DisplayName("Should correctly extract all named capture groups from multiple matches")
    void testExtractAllGroups() {
        NamedCapture key = SiftPatterns.capture("key", Sift.oneOrMore().letters());
        NamedCapture val = SiftPatterns.capture("val", Sift.oneOrMore().digits());

        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .namedCapture(key)
                .followedBy(':')
                .then().namedCapture(val)
                .sieveWith(Re2jEngine.INSTANCE)) {

            List<Map<String, String>> allGroups = pattern.extractAllGroups("age:30, height:180, weight:75");

            assertEquals(3, allGroups.size());
            assertEquals("age", allGroups.get(0).get("key"));
            assertEquals("30", allGroups.get(0).get("val"));

            assertEquals("height", allGroups.get(1).get("key"));
            assertEquals("180", allGroups.get(1).get("val"));
        }
    }

    @Test
    @DisplayName("Should correctly return the raw regex string")
    void testGetRawRegex() {
        try (SiftCompiledPattern pattern = Sift.fromStart()
                .oneOrMore().digits()
                .andNothingElse()
                .sieveWith(Re2jEngine.INSTANCE)) {

            assertEquals("^[0-9]+$", pattern.getRawRegex());
        }
    }

    @Test
    @DisplayName("Engine Guard: should wrap PatternSyntaxException into IllegalArgumentException")
    void testEngineWrapsSyntaxExceptions() {
        String malformedRegex = "(unclosed_group";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try (SiftCompiledPattern ignored = Re2jEngine.INSTANCE.compile(malformedRegex, Collections.emptySet())) {
                fail("The engine should have rejected the malformed syntax before entering the block");
            }
        });

        assertTrue(exception.getMessage().startsWith("RE2J engine rejected the generated syntax"));

        assertNotNull(exception.getCause());
        assertEquals(com.google.re2j.PatternSyntaxException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Should handle no matches gracefully (Negative branch)")
    void testNoMatches() {
        try (SiftCompiledPattern pattern = Sift.fromStart().exactly(2).digits().sieveWith(Re2jEngine.INSTANCE)) {
            assertFalse(pattern.extractFirst("abc").isPresent());
            assertTrue(pattern.extractAll("abc").isEmpty());
            assertTrue(pattern.extractGroups("abc").isEmpty());
            assertTrue(pattern.extractAllGroups("abc").isEmpty());
        }
    }

    @Test
    @DisplayName("Should handle unmatched optional named groups (Null value branch)")
    void testOptionalNamedGroup() {
        NamedCapture optGroup = SiftPatterns.capture("opt", Sift.exactly(2).digits());
        SiftPattern<Fragment> wrappedOptGroup = Sift.fromAnywhere().namedCapture(optGroup);

        try (SiftCompiledPattern pattern = Sift.fromAnywhere()
                .oneOrMore().letters()
                .then().optional().of(wrappedOptGroup)
                .sieveWith(Re2jEngine.INSTANCE)) {

            Map<String, String> groups = pattern.extractGroups("abc");
            assertTrue(groups.isEmpty());

            List<Map<String, String>> allGroups = pattern.extractAllGroups("abc");
            assertEquals(1, allGroups.size());
            assertTrue(allGroups.get(0).isEmpty());
        }
    }

    @Test
    @DisplayName("Should translate named captures safely without touching literal syntax overlaps")
    void testSafeNamedCaptureTranslationWithLiterals() {
        NamedCapture myGroup = SiftPatterns.capture("myGroup", Sift.exactly(2).digits());

        try (SiftCompiledPattern pattern = Sift.fromStart()
                .of(SiftPatterns.literal("(?<fake>"))
                .then().namedCapture(myGroup)
                .andNothingElse()
                .sieveWith(Re2jEngine.INSTANCE)) {

            String targetText = "(?<fake>42";

            assertTrue(pattern.matchesEntire(targetText));

            Map<String, String> groups = pattern.extractGroups(targetText);
            assertEquals("42", groups.get("myGroup"));

            assertNull(groups.get("fake"));
        }
    }

    @Test
    @DisplayName("Should be thread-safe when used concurrently by multiple threads")
    void testThreadSafety() throws InterruptedException {
        try (SiftCompiledPattern pattern = Sift.oneOrMore().digits().sieveWith(Re2jEngine.INSTANCE)) {
            int threadCount = 100;

            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
            java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        String input = "ThreadData " + threadId + " and " + (threadId + 1000);
                        List<String> matches = pattern.extractAll(input);

                        if (matches.size() == 2
                                && matches.get(0).equals(String.valueOf(threadId))
                                && matches.get(1).equals(String.valueOf(threadId + 1000))) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception ignored) {

                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean isZeroReached = latch.await(5, TimeUnit.SECONDS);
            assertTrue(isZeroReached, "Concurrency test timed out before all threads could finish");
            executor.shutdown();

            assertEquals(threadCount, successCount.get(), "Concurrency issue detected: RE2J Matcher state leaked between threads");
        }
    }
}