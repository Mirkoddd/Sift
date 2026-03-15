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

import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.RegexFeature;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import com.mirkoddd.sift.core.engine.SiftEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AtomicPattern Core Tests")
class AtomicPatternTest {

    @Test
    @DisplayName("Should correctly wrap regex and propagate all metadata/features from inner pattern")
    void testAtomicPatternMetadataPropagation() {
        NamedCapture myGroup = SiftPatterns.capture("atomicGroup", SiftPatterns.literal("val"));

        SiftPattern<Fragment> innerPattern = Sift.fromAnywhere()
                .namedCapture(myGroup)
                .then()
                .backreference(myGroup);

        SiftPattern<Fragment> atomic = innerPattern.preventBacktracking();

        assertEquals("(?>(?<atomicGroup>val)\\k<atomicGroup>)", atomic.shake());

        PatternMetadata metadata = (PatternMetadata) atomic;

        assertTrue(metadata.getInternalRegisteredGroups().contains("atomicGroup"),
                "Should propagate inner registered groups");
        assertTrue(metadata.getInternalRequiredBackreferences().contains("atomicGroup"),
                "Should propagate inner backreferences");

        Set<RegexFeature> features = metadata.getInternalUsedFeatures();
        assertTrue(features.contains(RegexFeature.ATOMIC_GROUP),
                "Should self-declare ATOMIC_GROUP feature");
        assertTrue(features.contains(RegexFeature.NAMED_CAPTURE),
                "Should propagate NAMED_CAPTURE feature from inner pattern");
        assertTrue(features.contains(RegexFeature.BACKREFERENCE),
                "Should propagate BACKREFERENCE feature from inner pattern");
    }

    @Test
    @DisplayName("Should safely handle inner patterns that do not implement PatternMetadata")
    void testAtomicPatternWithNonMetadataInner() {
        SiftPattern<Fragment> mockInner = new SiftPattern<Fragment>() {
            @Override public String shake() { return "mock"; }
            @Override public SiftCompiledPattern sieveWith(SiftEngine engine) { return null; }
            @Override public SiftPattern<Fragment> preventBacktracking() { return this; }
            @Override public Object ___internal_lock___() { return this; }
        };

        AtomicPattern<Fragment> atomic = new AtomicPattern<>(mockInner);

        assertEquals("(?>mock)", atomic.shake());

        assertTrue(atomic.getInternalRegisteredGroups().isEmpty(),
                "Should default to empty set safely");
        assertTrue(atomic.getInternalRequiredBackreferences().isEmpty(),
                "Should default to empty set safely");

        assertEquals(1, atomic.getInternalUsedFeatures().size());
        assertTrue(atomic.getInternalUsedFeatures().contains(RegexFeature.ATOMIC_GROUP));
    }
}