package com.mirkoddd.sift.core;

import static com.mirkoddd.sift.core.Sift.exactly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.RegexFeature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class FeatureLoggingAndPropagationTest {

    @Test
    @DisplayName("Should correctly track NAMED_CAPTURE, BACKREFERENCE and ATOMIC_GROUP")
    void testCoreFeaturesPropagation() {
        NamedCapture group = SiftPatterns.capture("id", SiftPatterns.literal("val"));

        SiftPattern<Fragment> basePattern = Sift.fromAnywhere()
                .namedCapture(group)
                .then()
                .backreference(group)
                .preventBacktracking();

        Set<RegexFeature> features = ((PatternMetadata) basePattern).getInternalUsedFeatures();

        assertNotNull(features);
        assertTrue(features.contains(RegexFeature.NAMED_CAPTURE), "Missing NAMED_CAPTURE");
        assertTrue(features.contains(RegexFeature.BACKREFERENCE), "Missing BACKREFERENCE");
        assertTrue(features.contains(RegexFeature.ATOMIC_GROUP), "Missing ATOMIC_GROUP");
    }

    @Test
    @DisplayName("Should correctly track LOOKAHEAD and LOOKBEHIND")
    void testLookaroundFeaturesPropagation() {
        SiftPattern<Fragment> lookaroundPattern = Sift.fromAnywhere()
                .of(SiftPatterns.literal("target"))
                .mustBeFollowedBy(SiftPatterns.literal("suffix"))
                .mustBePrecededBy(SiftPatterns.literal("prefix"));

        Set<RegexFeature> features = ((PatternMetadata) lookaroundPattern).getInternalUsedFeatures();

        assertNotNull(features);
        assertTrue(features.contains(RegexFeature.LOOKAHEAD), "Missing LOOKAHEAD");
        assertTrue(features.contains(RegexFeature.LOOKBEHIND), "Missing LOOKBEHIND");
    }

    @Test
    @DisplayName("Should correctly track RECURSION from NestingBuilder")
    void testRecursionFeaturePropagation() {
        SiftPattern<Fragment> nestingPattern = SiftPatterns.nesting(3)
                .using(Delimiter.PARENTHESES)
                .containing(SiftPatterns.literal("inner"));

        Set<RegexFeature> features = ((PatternMetadata) nestingPattern).getInternalUsedFeatures();

        assertNotNull(features);
        assertTrue(features.contains(RegexFeature.RECURSION), "Missing RECURSION");
    }

    @Test
    @DisplayName("BOSS FIGHT: Should aggregate ALL features in a highly complex nested structure")
    void testAllFeaturesCombinedPropagation() {
        // 1. Define the NamedCapture group independently
        NamedCapture bossGroup = SiftPatterns.capture("bossGroup", exactly(1).digits());

        // 2. Build the recursive block (Feature: RECURSION)
        SiftPattern<Fragment> recursiveBlock = SiftPatterns.nesting(3)
                .using(Delimiter.BRACES)
                .containing(Sift.fromAnywhere().letters());

        // 3. Build an atomic sub-pattern (Feature: ATOMIC_GROUP)
        // We apply it to a self-contained part to avoid dangling backreference errors
        SiftPattern<Fragment> atomicPart = Sift.fromAnywhere()
                .exactly(3).letters()
                .preventBacktracking();

        // 4. Combine everything into the root pattern
        // This aggregates: LOOKBEHIND, NAMED_CAPTURE, RECURSION, ATOMIC_GROUP, BACKREFERENCE, LOOKAHEAD
        SiftPattern<Fragment> ultimatePattern = Sift.fromAnywhere()
                .namedCapture(bossGroup)                               // 1. NAMED_CAPTURE
                .mustBePrecededBy(SiftPatterns.literal("start-")) // 2. LOOKBEHIND
                .then().of(recursiveBlock)                             // 3. RECURSION
                .then().of(atomicPart)                                 // 4. ATOMIC_GROUP
                .then().backreference(bossGroup)                       // 5. BACKREFERENCE
                .mustBeFollowedBy(SiftPatterns.literal("-end"));  // 6. LOOKAHEAD

        // 5. Extraction and Validation
        Set<RegexFeature> features = ((PatternMetadata) ultimatePattern).getInternalUsedFeatures();

        assertNotNull(features);
        assertEquals(6, features.size(), "The pattern should aggregate exactly all 6 advanced features");

        assertTrue(features.contains(RegexFeature.NAMED_CAPTURE));
        assertTrue(features.contains(RegexFeature.BACKREFERENCE));
        assertTrue(features.contains(RegexFeature.ATOMIC_GROUP));
        assertTrue(features.contains(RegexFeature.LOOKAHEAD));
        assertTrue(features.contains(RegexFeature.LOOKBEHIND));
        assertTrue(features.contains(RegexFeature.RECURSION));
    }

    @Test
    @DisplayName("MemoizedPattern should satisfy 100% branch coverage including feature-only edge cases")
    void testMemoizedPatternFullCoverage() {
        // Case 1: inner == null && features.isEmpty() -> TRUE && TRUE (Hit)
        SiftPattern<Fragment> literal = SiftPatterns.literal("abc");
        assertTrue(((PatternMetadata) literal).getInternalUsedFeatures().isEmpty());

        // Case 2: inner == null && features.isEmpty() -> TRUE && FALSE (Hit)
        // We force a MemoizedPattern with a feature but NO inner pattern.
        // This targets the specific branch you mentioned.
        SiftPattern<Fragment> featureOnly = SiftPatterns.memoize(
                () -> "some-regex",
                RegexFeature.ATOMIC_GROUP,
                null
        );
        Set<RegexFeature> features = ((PatternMetadata) featureOnly).getInternalUsedFeatures();
        assertEquals(1, features.size());
        assertTrue(features.contains(RegexFeature.ATOMIC_GROUP));

        // Case 3: inner instanceof PatternMetadata -> FALSE (Hit)
        // Testing with an "alien" pattern that doesn't implement PatternMetadata
        SiftPattern<Fragment> alien = new SiftPattern<Fragment>() {
            @Override public String shake() { return "alien"; }
            @Override public com.mirkoddd.sift.core.engine.SiftCompiledPattern sieveWith(com.mirkoddd.sift.core.engine.SiftEngine e) { return null; }
            @Override public SiftPattern<Fragment> preventBacktracking() { return this; }
            @Override public Object ___internal_lock___() { return this; }
        };

        SiftPattern<Fragment> wrapper = SiftPatterns.memoize(() -> "wrap", RegexFeature.LOOKAHEAD, alien);
        Set<RegexFeature> wrapperFeatures = ((PatternMetadata) wrapper).getInternalUsedFeatures();
        assertEquals(1, wrapperFeatures.size());
        assertTrue(wrapperFeatures.contains(RegexFeature.LOOKAHEAD));
    }

    @Test
    @DisplayName("Should correctly track INLINE_FLAGS from global context and local blocks")
    void testFlagsFeaturePropagation() {
        // Test 1: Global flags via Sift.filteringWith
        SiftPattern<com.mirkoddd.sift.core.dsl.Root> globalPattern = Sift.filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
                .fromStart()
                .exactly(1).letters()
                .andNothingElse();

        Set<RegexFeature> globalFeatures = ((PatternMetadata) globalPattern).getInternalUsedFeatures();
        assertNotNull(globalFeatures);
        assertTrue(globalFeatures.contains(RegexFeature.INLINE_FLAGS), "Missing INLINE_FLAGS from filteringWith()");

        // Test 2: Local inline flags via SiftPatterns.withFlags
        SiftPattern<Fragment> localPattern = Sift.fromAnywhere()
                .of(SiftPatterns.withFlags(SiftPatterns.literal("test"), SiftGlobalFlag.DOTALL));

        Set<RegexFeature> localFeatures = ((PatternMetadata) localPattern).getInternalUsedFeatures();
        assertNotNull(localFeatures);
        assertTrue(localFeatures.contains(RegexFeature.INLINE_FLAGS), "Missing INLINE_FLAGS from withFlags()");
    }

    @Test
    @DisplayName("Should correctly track PREVIOUS_MATCH_ANCHOR from \\G anchor")
    void testPreviousMatchAnchorPropagation() {
        // Test 1: Standard fromPreviousMatchEnd
        SiftPattern<com.mirkoddd.sift.core.dsl.Root> anchorPattern = Sift.fromPreviousMatchEnd()
                .oneOrMore().digits()
                .andNothingElse();

        Set<RegexFeature> features = ((PatternMetadata) anchorPattern).getInternalUsedFeatures();
        assertNotNull(features);
        assertTrue(features.contains(RegexFeature.PREVIOUS_MATCH_ANCHOR), "Missing PREVIOUS_MATCH_ANCHOR");

        // Test 2: fromPreviousMatchEnd used together with global flags (SiftStarter)
        SiftPattern<com.mirkoddd.sift.core.dsl.Root> flaggedAnchorPattern = Sift.filteringWith(SiftGlobalFlag.MULTILINE)
                .fromPreviousMatchEnd()
                .oneOrMore().letters()
                .andNothingElse();

        Set<RegexFeature> flaggedFeatures = ((PatternMetadata) flaggedAnchorPattern).getInternalUsedFeatures();
        assertNotNull(flaggedFeatures);
        assertTrue(flaggedFeatures.contains(RegexFeature.PREVIOUS_MATCH_ANCHOR), "Missing PREVIOUS_MATCH_ANCHOR in flagged starter");
        assertTrue(flaggedFeatures.contains(RegexFeature.INLINE_FLAGS), "Missing INLINE_FLAGS in flagged starter");
    }
}
