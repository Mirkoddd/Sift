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
package com.mirkoddd.sift.engine.graalvm;

import com.mirkoddd.sift.core.Delimiter;
import com.mirkoddd.sift.core.NamedCapture;
import com.mirkoddd.sift.core.Sift;
import com.mirkoddd.sift.core.SiftGlobalFlag;
import com.mirkoddd.sift.core.SiftPatterns;
import com.mirkoddd.sift.core.dsl.Connector;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.mirkoddd.sift.core.Sift.fromAnywhere;
import static com.mirkoddd.sift.core.Sift.fromPreviousMatchEnd;
import static com.mirkoddd.sift.core.Sift.fromWordBoundary;
import static com.mirkoddd.sift.core.Sift.oneOrMore;
import static com.mirkoddd.sift.core.Sift.zeroOrMore;
import static com.mirkoddd.sift.core.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GraalVM TRegex Engine Integration Tests")
class GraalVmEngineTest {

    private final GraalVmEngine engine = GraalVmEngine.INSTANCE;


    @Test
    @DisplayName("Should safely close pattern and thread scope without memory leaks")
    void testAutoCloseableScopeAndPattern() {
        assertDoesNotThrow(() -> {
            try (
                    AutoCloseable scope = engine.openThreadScope();
                    SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)
            ) {
                assertNotNull(scope, "Thread scope should be successfully created");
                assertTrue(pattern.matchesEntire("12345"));
                assertFalse(pattern.matchesEntire("abc"));
            }
        });
    }

    @Test
    @DisplayName("Coverage: test all branches of openThreadScope")
    void testOpenThreadScopeCoverage() {
        GraalVmEngine.THREAD_CONTEXT.remove();
        assertDoesNotThrow(() -> {
            try (AutoCloseable scope = engine.openThreadScope()) {
                assertNotNull(scope, "Thread scope should be successfully created");
            }
        });

        assertDoesNotThrow(() -> {
            try (AutoCloseable scope = engine.openThreadScope()) {
                assertNotNull(scope, "Thread scope should be successfully created");
                try (SiftCompiledPattern p = oneOrMore().digits().sieveWith(engine)) {
                    p.matchesEntire("123");
                }
            }
        });
    }

    @Test
    @DisplayName("Coverage: test catch block in openThreadScope")
    void testOpenThreadScopeCatchBlock() {
        assertDoesNotThrow(() -> {
            AutoCloseable scope = engine.openThreadScope();

            try (SiftCompiledPattern p = oneOrMore().digits().sieveWith(engine)) {
                p.matchesEntire("1");
            }

            org.graalvm.polyglot.Context ctx = GraalVmEngine.THREAD_CONTEXT.get();
            if (ctx != null) {
                ctx.close();
            }

            scope.close();
        });
    }

    @Test
    @DisplayName("Coverage: force catch block in openThreadScope")
    void testOpenThreadScopeCatchCoverage() {
        AutoCloseable scope = engine.openThreadScope();

        try (SiftCompiledPattern p = oneOrMore().digits().sieveWith(engine)) {
            p.matchesEntire("1");
        }

        org.graalvm.polyglot.Context ctx = GraalVmEngine.THREAD_CONTEXT.get();

        ctx.close();

        assertDoesNotThrow(scope::close);
    }

    @Test
    @DisplayName("Should silently ignore exceptions thrown while closing the context")
    void testOpenThreadScopeIgnoresCloseExceptions() {
        GraalVmEngine testEngine = new GraalVmEngine(ctx -> {
            throw new IllegalStateException("Simulated closure failure");
        });

        GraalVmEngine.THREAD_CONTEXT.set(Context.newBuilder(GraalVmDictionary.JS_LANGUAGE_ID).build());

        assertDoesNotThrow(() -> {
            try (AutoCloseable scope = testEngine.openThreadScope()) {
                assertNotNull(scope);
            }
        });

        assertNull(GraalVmEngine.THREAD_CONTEXT.get(), "ThreadLocal should be cleaned up even if close() throws");
    }

    @Test
    @DisplayName("Should match patterns built with Sift DSL")
    void testDslMatching() {
        try (SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)) {
            // --- containsMatchIn tests ---
            assertTrue(pattern.containsMatchIn("Order #12345"));
            assertFalse(pattern.containsMatchIn("No numbers here"));

            // --- matchesEntire tests ---
            assertTrue(pattern.matchesEntire("12345"));
            assertFalse(pattern.matchesEntire("Order #12345"));
            assertFalse(pattern.matchesEntire("12345 Order"));
            assertFalse(pattern.matchesEntire("No numbers here"));
        }
    }

    @Test
    @DisplayName("Should safely return absent values when testing strings without matches")
    void testNoMatchFallbacks() {
        try (SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)) {
            String input = "No numbers here";

            assertTrue(pattern.extractFirst(input).isEmpty());
            assertTrue(pattern.extractAll(input).isEmpty());
            assertTrue(pattern.extractGroups(input).isEmpty());
            assertTrue(pattern.extractAllGroups(input).isEmpty());
            assertEquals(0, pattern.streamMatches(input).count());

            assertEquals(input, pattern.replaceFirst(input, "[REDACTED]"));
            assertEquals(input, pattern.replaceAll(input, "[REDACTED]"));

            List<String> splitResult = pattern.splitBy(input);
            assertEquals(1, splitResult.size());
            assertEquals(input, splitResult.get(0));
        }
    }

    @Test
    @DisplayName("Should extract only the first match")
    void testExtractFirst() {
        try (SiftCompiledPattern pattern = fromWordBoundary()
                .then().exactly(4).letters()
                .followedBy(fromWordBoundary())
                .sieveWith(engine)) {

            Optional<String> firstMatch = pattern.extractFirst("One fine morning, four dogs ran.");

            assertTrue(firstMatch.isPresent());
            assertEquals("fine", firstMatch.get());
        }
    }

    @Test
    @DisplayName("Should extract all matches using word boundaries")
    void testExtractAll() {
        try (SiftCompiledPattern pattern = fromWordBoundary()
                .then().exactly(4).wordCharacters()
                .followedBy(fromWordBoundary())
                .sieveWith(engine)) {

            List<String> matches = pattern.extractAll("This is a test with some four word text");

            assertEquals(List.of("This", "test", "with", "some", "four", "word", "text"), matches);
        }
    }

    @Test
    @DisplayName("Should provide a stream of matches")
    void testStreamMatches() {
        try (SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)) {
            Stream<String> stream = pattern.streamMatches("ID10, ID20, ID30");
            List<String> collected = stream.toList();

            assertEquals(List.of("10", "20", "30"), collected);
        }
    }

    @Test
    @DisplayName("Should extract named groups using SiftPatterns (Hydroxide Test)")
    void testNamedGroupExtraction() {
        NamedCapture metalGroup = capture("metal",
                fromAnywhere().range('A', 'Z').followedBy(zeroOrMore().range('a', 'z')));

        SiftPattern<Fragment> hydroxide = literal("(OH)");

        NamedCapture valencyGroup = capture("valency",
                zeroOrMore().digits());

        Connector<Fragment> metal = fromAnywhere().namedCapture(metalGroup);
        Connector<Fragment> valency = fromAnywhere().namedCapture(valencyGroup);

        try (SiftCompiledPattern pattern = fromAnywhere()
                .of(metal)
                .followedBy(hydroxide)
                .followedBy(valency)
                .sieveWith(engine)) {

            Map<String, String> groups = pattern.extractGroups("Reaction yields Ca(OH)2");

            assertEquals("Ca", groups.get("metal"));
            assertEquals("2", groups.get("valency"));
        }
    }

    @Test
    @DisplayName("Should handle optional groups via DSL")
    void testOptionalGroups() {
        try (AutoCloseable scope = engine.openThreadScope()) {
            assertNotNull(scope, "Thread scope should be successfully created");

            NamedCapture metalGroup = capture("metal",
                    fromAnywhere().range('A', 'Z').followedBy(zeroOrMore().range('a', 'z')));

            NamedCapture valencyGroup = capture("valency",
                    oneOrMore().digits());

            Connector<Fragment> metal = fromAnywhere().namedCapture(metalGroup);
            Connector<Fragment> valency = fromAnywhere().namedCapture(valencyGroup);

            try (SiftCompiledPattern pattern = fromAnywhere()
                    .of(metal)
                    .followedBy(literal("OH"))
                    .then().optional().of(valency)
                    .sieveWith(engine)) {

                Map<String, String> groups = pattern.extractGroups("Reaction yields NaOH");

                assertEquals("Na", groups.get("metal"));
                assertNull(groups.get("valency"));
            }
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should extract multiple named groups from multiple occurrences")
    void testExtractAllGroups() {
        NamedCapture nameGroup = capture("name", oneOrMore().letters());
        NamedCapture ageGroup = capture("age", oneOrMore().digits());

        try (SiftCompiledPattern pattern = fromAnywhere()
                .of(literal("User: "))
                .then().namedCapture(nameGroup)
                .followedBy(literal(", Age: "))
                .then().namedCapture(ageGroup)
                .sieveWith(engine)) {

            List<Map<String, String>> results = pattern.extractAllGroups(
                    "User: Alice, Age: 30 | User: Bob, Age: 25"
            );

            assertEquals(2, results.size());

            assertEquals("Alice", results.get(0).get("name"));
            assertEquals("30", results.get(0).get("age"));

            assertEquals("Bob", results.get(1).get("name"));
            assertEquals("25", results.get(1).get("age"));
        }
    }

    @Test
    @DisplayName("Should perform replacements on DSL-generated patterns")
    void testReplacements() {
        try (SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)) {
            assertEquals("ID: [HIDDEN]", pattern.replaceFirst("ID: 12345", "[HIDDEN]"));
            assertEquals("X: ?, Y: ?", pattern.replaceAll("X: 10, Y: 20", "?"));
        }
    }

    @Test
    @DisplayName("Should handle zero-width matches safely")
    void testZeroWidthMatch() {
        try (SiftCompiledPattern pattern = fromWordBoundary().sieveWith(engine)) {
            String result = pattern.replaceAll("cat", "|");
            assertEquals("|cat|", result);
        }
    }

    @Test
    @DisplayName("Should split text using DSL patterns")
    void testSplitBy() {
        try (SiftCompiledPattern pattern = zeroOrMore().whitespace()
                .followedBy(literal(","))
                .followedBy(zeroOrMore().whitespace())
                .sieveWith(engine)) {

            List<String> chunks = pattern.splitBy("A , B, C  , D");
            assertEquals(List.of("A", "B", "C", "D"), chunks);
        }
    }

    @Test
    @DisplayName("Should expose the raw regex string for debugging")
    void testRawRegexExposure() {
        try (SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)) {
            assertNotNull(pattern.getRawRegex());
            assertFalse(pattern.getRawRegex().isEmpty());
            assertEquals(pattern.getRawRegex(), pattern.toString());
        }
    }

    @Test
    @DisplayName("Should correctly handle and extract Unicode characters and Emojis")
    void testUnicodeSupport() {
        NamedCapture greetingCapture = capture("greeting", literal("こんにちは"));
        SiftPattern<Fragment> options = anyOf(
                literal("Mirkö"),
                literal("🚀")
        );

        NamedCapture nameCapture = capture("name", options);

        try (SiftCompiledPattern pattern = fromAnywhere()
                .namedCapture(greetingCapture)
                .followedBy(oneOrMore().whitespace())
                .then().namedCapture(nameCapture)
                .sieveWith(engine)) {

            Map<String, String> groups1 = pattern.extractGroups("Message: こんにちは Mirkö!");
            assertEquals(2, groups1.size());
            assertEquals("こんにちは", groups1.get("greeting"));
            assertEquals("Mirkö", groups1.get("name"));

            Map<String, String> groups2 = pattern.extractGroups("Message: こんにちは 🚀!");
            assertEquals(2, groups2.size());
            assertEquals("こんにちは", groups2.get("greeting"));
            assertEquals("🚀", groups2.get("name"));

            String replaced = pattern.replaceFirst("Say こんにちは 🚀 to the world", "[CENSORED]");
            assertEquals("Say [CENSORED] to the world", replaced);
        }
    }

    @Test
    @DisplayName("Should wrap PolyglotException into IllegalArgumentException on invalid syntax")
    void testPolyglotExceptionHandling() {
        String invalidRegex = "[unterminated-class";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    try (SiftCompiledPattern ignored = engine.compile(invalidRegex, Collections.emptySet())) {
                        System.out.println("unreachable");
                    }
                }
        );

        assertTrue(exception.getMessage().contains("GraalVM TRegex engine rejected the generated syntax"));

        assertNotNull(exception.getCause());
        assertEquals("org.graalvm.polyglot.PolyglotException", exception.getCause().getClass().getName());
    }

    @Test
    @DisplayName("Should return empty maps when matching patterns without named captures")
    void testMatchWithoutNamedCaptures() {
        try (SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)) {
            Map<String, String> groups = pattern.extractGroups("Order 12345");
            assertTrue(groups.isEmpty());

            List<Map<String, String>> allGroups = pattern.extractAllGroups("ID 10, ID 20");
            assertEquals(2, allGroups.size());
            assertTrue(allGroups.get(0).isEmpty());
            assertTrue(allGroups.get(1).isEmpty());
        }
    }

    @Test
    @DisplayName("Should successfully compile and execute advanced regex features supported by GraalJS")
    void testAdvancedRegexFeatures() {
        // Positive Lookahead: Matches 'q' only if followed by 'u'
        try (SiftCompiledPattern lookahead = engine.compile("q(?=u)", Collections.emptySet())) {
            assertTrue(lookahead.containsMatchIn("queen"));
            assertFalse(lookahead.containsMatchIn("qatar"));
        }

        // Positive Lookbehind: Matches 'b' only if preceded by 'a'
        try (SiftCompiledPattern lookbehind = engine.compile("(?<=a)b", Collections.emptySet())) {
            assertTrue(lookbehind.containsMatchIn("cab"));
            assertFalse(lookbehind.containsMatchIn("bed"));
        }

        // Negative Lookahead: Matches 'q' only if NOT followed by 'u'
        try (SiftCompiledPattern negLookahead = engine.compile("q(?!u)", Collections.emptySet())) {
            assertTrue(negLookahead.containsMatchIn("faq"));
            assertFalse(negLookahead.containsMatchIn("queen"));
        }

        // Negative Lookbehind: Matches 'b' only if NOT preceded by 'a'
        try (SiftCompiledPattern negLookbehind = engine.compile("(?<!a)b", Collections.emptySet())) {
            assertTrue(negLookbehind.containsMatchIn("bed"));
            assertFalse(negLookbehind.containsMatchIn("cab"));
        }

        // Backreferences: Matches duplicated words
        try (SiftCompiledPattern backref = engine.compile("\\b(\\w+)\\s+\\1\\b", Collections.emptySet())) {
            assertTrue(backref.containsMatchIn("hello hello"));
            assertFalse(backref.containsMatchIn("hello world"));
        }
    }

    @Test
    @DisplayName("Should successfully compile and execute advanced regex features built with Sift DSL")
    void testAdvancedRegexFeaturesWithDsl() {

        // 1. Positive Lookahead: Matches 'q' only if followed by 'u'
        try (SiftCompiledPattern lookahead = fromAnywhere()
                .of(literal("q"))
                .mustBeFollowedBy(literal("u"))
                .sieveWith(engine)) {
            assertTrue(lookahead.containsMatchIn("queen"));
            assertFalse(lookahead.containsMatchIn("qatar"));
        }

        // 2. Positive Lookbehind: Matches 'b' only if preceded by 'a'
        try (SiftCompiledPattern lookbehind = fromAnywhere()
                .of((literal("b")))
                .mustBePrecededBy(literal("a"))
                .sieveWith(engine)) {
            assertTrue(lookbehind.containsMatchIn("cab"));
            assertFalse(lookbehind.containsMatchIn("bed"));
        }

        // 3. Negative Lookahead: Matches 'q' only if NOT followed by 'u'
        try (SiftCompiledPattern negLookahead = fromAnywhere()
                .of(literal("q"))
                .notFollowedBy(literal("u"))
                .sieveWith(engine)) {
            assertTrue(negLookahead.containsMatchIn("faq"));
            assertFalse(negLookahead.containsMatchIn("queen"));
        }

        // 4. Negative Lookbehind: Matches 'b' only if NOT preceded by 'a'
        try (SiftCompiledPattern negLookbehind = fromAnywhere()
                .of(literal("b"))
                .notPrecededBy(literal("a"))
                .sieveWith(engine)) {
            assertTrue(negLookbehind.containsMatchIn("bed"));
            assertFalse(negLookbehind.containsMatchIn("cab"));
        }

        // 5. Backreferences: Matches duplicated words (e.g., "hello hello")
        NamedCapture wordCapture = capture("word", oneOrMore().wordCharacters());

        try (SiftCompiledPattern backref = fromWordBoundary()
                .then().namedCapture(wordCapture)
                .followedBy(oneOrMore().whitespace())
                .then().backreference(wordCapture)
                .followedBy(fromWordBoundary())
                .sieveWith(engine)) {
            assertTrue(backref.containsMatchIn("hello hello"));
            assertFalse(backref.containsMatchIn("hello world"));
        }
    }

    @Test
    @DisplayName("Should correctly execute conditional logic using lookaheads in GraalVM")
    void testGraalVmConditionalsExecution() {
        // Rule: If followed by 'a', match "apple", otherwise match "banana"
        // Translated to: (?:(?=a)apple|(?!a)banana)
        SiftPattern<Fragment> conditionalRule = ifFollowedBy(literal("a"))
                .thenUse(literal("apple"))
                .otherwiseUse(literal("banana"));

        try (
                AutoCloseable scope = engine.openThreadScope();
                SiftCompiledPattern pattern = conditionalRule.sieveWith(engine)
        ) {
            assertNotNull(scope, "Thread scope should be successfully created");
            // Test true branch (lookahead matches)
            assertTrue(pattern.containsMatchIn("I want an apple"), "Should evaluate true branch and match 'apple'");
            assertEquals("apple", pattern.extractFirst("Here is an apple").orElse(""));

            // Test false branch (negative lookahead matches)
            assertTrue(pattern.containsMatchIn("I want a banana"), "Should evaluate false branch and match 'banana'");
            assertEquals("banana", pattern.extractFirst("Here is a banana").orElse(""));

            // Test no match
            assertFalse(pattern.containsMatchIn("I want a cherry"), "Should not match unrelated strings");

            // Test multiple extractions covering both branches
            List<String> expectedMatches = Arrays.asList("apple", "banana", "apple");
            List<String> actualMatches = pattern.extractAll("apple, orange, banana, kiwi, apple");
            assertEquals(expectedMatches, actualMatches, "Should extract all conditional matches correctly");

        } catch (Exception e) {
            fail("GraalVM failed to execute conditional logic: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should be thread-safe when used concurrently by multiple threads")
    void testThreadSafety() throws InterruptedException {
        try (SiftCompiledPattern pattern = Sift.oneOrMore().digits().sieveWith(engine)) {
            int threadCount = 100;

            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        String input = "Data A: " + threadId + " | Data B: " + (threadId + 500);
                        List<String> matches = pattern.extractAll(input);

                        if (matches.size() == 2
                                && matches.get(0).equals(String.valueOf(threadId))
                                && matches.get(1).equals(String.valueOf(threadId + 500))) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception ignore) {
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean isZeroReached = latch.await(5, TimeUnit.SECONDS);
            assertTrue(isZeroReached, "Concurrency test timed out before all threads could finish");
            executor.shutdown();

            assertEquals(threadCount, successCount.get(), "Concurrency issue detected: Not all threads extracted the correct values");
        }
    }

    @Test
    @DisplayName("Should correctly identify and reject unsupported features via Sift DSL")
    void testFeatureGuard() {

        // 1. RECURSION
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = SiftPatterns.nesting(3)
                    .using(Delimiter.PARENTHESES)
                    .containing(Sift.fromAnywhere().letters())
                    .sieveWith(engine)) {
                System.out.println("unreachable");
            }
        }, "Expected engine to reject recursive patterns");

        // 2. PREVIOUS_MATCH_ANCHOR
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = fromPreviousMatchEnd()
                    .alphanumeric()
                    .sieveWith(engine)) {
                System.out.println("unreachable");
            }
        }, "Expected engine to reject the \\G anchor");

        // 3. INLINE_FLAGS
        assertThrows(UnsupportedOperationException.class, () -> {
            try (SiftCompiledPattern ignored = Sift.filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
                    .fromAnywhere()
                    .of(literal("abc"))
                    .sieveWith(engine)) {
                System.out.println("unreachable");
            }
        }, "Expected engine to reject inline/local flags like (?i)");

        // 4. INLINE_FLAGS (via withFlags)
        assertThrows(UnsupportedOperationException.class, () -> {
            SiftPattern<Fragment> caseInsensitiveSift = SiftPatterns.withFlags(
                    SiftPatterns.literal("sift"),
                    SiftGlobalFlag.CASE_INSENSITIVE
            );

            try (SiftCompiledPattern ignored = fromAnywhere()
                    .of(caseInsensitiveSift)
                    .sieveWith(engine)) {
                System.out.println("unreachable");
            }
        }, "Expected engine to reject inline/local flags like (?i)");
    }

    @Test
    @DisplayName("Should gracefully handle null inputs across all extraction and matching methods")
    void testNullGuards() {
        try (SiftCompiledPattern pattern = oneOrMore().digits().sieveWith(engine)) {

            // --- Matching ---
            assertFalse(pattern.containsMatchIn(null), "containsMatchIn should return false for null");
            assertFalse(pattern.matchesEntire(null), "matchesEntire should return false for null");

            // --- Extraction ---
            assertFalse(pattern.extractFirst(null).isPresent(), "extractFirst should be empty for null");
            assertTrue(pattern.extractAll(null).isEmpty(), "extractAll should be empty for null");

            // --- Replacement ---
            assertNull(pattern.replaceFirst(null, "replacement"), "replaceFirst should return empty string for null");
            assertNull(pattern.replaceAll(null, "replacement"), "replaceAll should return empty string for null");

            // --- Groups ---
            assertTrue(pattern.extractGroups(null).isEmpty(), "extractGroups should be empty for null");
            assertTrue(pattern.extractAllGroups(null).isEmpty(), "extractAllGroups should be empty for null");

            // --- Splitting & Streaming ---
            assertTrue(pattern.splitBy(null).isEmpty(), "splitBy should be empty for null");
            assertEquals(0, pattern.streamMatches(null).count(), "streamMatches should be empty for null");
        }
    }
}