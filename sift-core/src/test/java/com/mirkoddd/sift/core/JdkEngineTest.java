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
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import com.mirkoddd.sift.core.engine.JdkEngine;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

@DisplayName("JdkEngine Execution & Safety Tests")
class JdkEngineTest {

    @Test
    @DisplayName("All compiled pattern methods should gracefully handle null inputs directly at the engine level")
    void testNullInputsOnCompiledPattern() {
        // We instantiate the compiled pattern directly from the engine,
        // bypassing the DSL interfaces to test the engine's internal null safety.
        try (SiftCompiledPattern compiledPattern = JdkEngine.INSTANCE.compile("dummy-regex", Collections.emptySet())) {

            // Assert all methods handle null without throwing NullPointerException
            assertFalse(compiledPattern.containsMatchIn(null), "containsMatchIn should return false for null");
            assertFalse(compiledPattern.matchesEntire(null), "matchesEntire should return false for null");
            assertEquals(Optional.empty(), compiledPattern.extractFirst(null), "extractFirst should return empty for null");
            assertTrue(compiledPattern.extractAll(null).isEmpty(), "extractAll should return empty list for null");

            assertNull(compiledPattern.replaceFirst(null, "replacement"), "replaceFirst should return empty string");
            assertNull(compiledPattern.replaceAll(null, "replacement"), "replaceAll should return empty string");

            assertTrue(compiledPattern.extractGroups(null).isEmpty(), "extractGroups should return empty map for null");
            assertTrue(compiledPattern.extractAllGroups(null).isEmpty(), "extractAllGroups should return empty list for null");

            assertTrue(compiledPattern.splitBy(null).isEmpty(), "splitBy should return empty list for null");
            assertEquals(0L, compiledPattern.streamMatches(null).count(), "streamMatches should return empty stream for null");
        }
    }
}