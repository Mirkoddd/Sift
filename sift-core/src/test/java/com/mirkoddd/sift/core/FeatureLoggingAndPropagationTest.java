package com.mirkoddd.sift.core;

import static com.mirkoddd.sift.core.Sift.exactly;
import static com.mirkoddd.sift.core.SiftPatterns.literal;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mirkoddd.sift.core.dsl.Assertion;
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
        NamedCapture group = SiftPatterns.capture("id", literal("val"));

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
                .of(literal("target"))
                .mustBeFollowedBy(literal("suffix"))
                .mustBePrecededBy(literal("prefix"));

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
                .containing(literal("inner"));

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
                .mustBePrecededBy(literal("start-")) // 2. LOOKBEHIND
                .then().of(recursiveBlock)                             // 3. RECURSION
                .then().of(atomicPart)                                 // 4. ATOMIC_GROUP
                .then().backreference(bossGroup)                       // 5. BACKREFERENCE
                .mustBeFollowedBy(literal("-end"));  // 6. LOOKAHEAD

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
    @DisplayName("AST nodes should correctly propagate features and handle alien patterns safely")
    void testAstNodeFeaturePropagation() {
        // Case 1: Simple node without features
        SiftPattern<Fragment> literal = literal("abc");
        assertTrue(((PatternMetadata) literal).getInternalUsedFeatures().isEmpty(),
                "Literal should not introduce any advanced features");

        // Case 2: Node that inherently adds a feature
        SiftPattern<Assertion> lookahead = SiftPatterns.positiveLookahead(literal);
        Set<RegexFeature> features = ((PatternMetadata) lookahead).getInternalUsedFeatures();
        assertEquals(1, features.size(), "Lookahead should introduce exactly 1 feature");
        assertTrue(features.contains(RegexFeature.LOOKAHEAD));

        // Case 3: Alien pattern integration (Branch coverage for 'instanceof PatternMetadata == false')
        // We simulate a third-party class that implements SiftPattern but NOT PatternMetadata
        SiftPattern<Fragment> alien = new SiftPattern<Fragment>() {
            @Override public String shake() { return "alien"; }
            @Override public com.mirkoddd.sift.core.engine.SiftCompiledPattern sieveWith(com.mirkoddd.sift.core.engine.SiftEngine e) { return null; }
            @Override public com.mirkoddd.sift.core.engine.SiftCompiledPattern sieve() { return null; }
            @Override public boolean matchesEntire(CharSequence i) { return false; }
            @Override public boolean containsMatchIn(CharSequence i) { return false; }
            @Override public SiftPattern<Fragment> preventBacktracking() { return this; }
            @Override public Object ___internal_lock___() { return this; }
        };

        // Wrap the alien pattern in a group.
        // The AST Compiler (PatternAssembler) will try to extract features/groups from it.
        SiftPattern<Fragment> wrapper = SiftPatterns.group(literal("test"), alien);

        // This triggers shake() -> traverse() -> PatternAssembler visiting the alien pattern.
        // The fallback logic for alien patterns (returning empty sets) should prevent any ClassCastException.
        assertDoesNotThrow(() -> {
            String regex = wrapper.shake();
            assertEquals("(?:testalien)", regex);
        }, "The AST Compiler must not crash when traversing an alien pattern");
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
                .of(SiftPatterns.withFlags(literal("test"), SiftGlobalFlag.DOTALL));

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
