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
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mirkoddd.sift.core.engine.AbstractSiftEngine;
import com.mirkoddd.sift.core.engine.RegexFeature;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

@DisplayName("AbstractSiftEngine Core Logic Tests")
class AbstractSiftEngineTest {

    /**
     * A dummy engine used strictly to test the Template Method's validation logic
     * inside the sift-core module, independent of external implementations like RE2J.
     */
    private static final class StubEngine extends AbstractSiftEngine {

        @Override
        protected SiftCompiledPattern doCompile(String rawRegex, Set<RegexFeature> usedFeatures) {
            return null; // Compilation logic is not the focus of this test
        }

        @Override
        protected Map<RegexFeature, String> getUnsupportedFeatures() {
            Map<RegexFeature, String> map = new EnumMap<>(RegexFeature.class);
            map.put(RegexFeature.RECURSION, "StubEngine explicitly rejects recursion.");
            return map;
        }
    }

    @Test
    @DisplayName("Should successfully pass validation if all features are supported")
    void testValidationPassesForSupportedFeatures() {
        StubEngine engine = new StubEngine();

        // Let's pass a feature that is NOT in the unsupported map (e.g., NAMED_CAPTURE)
        Set<RegexFeature> requestedFeatures = Collections.singleton(RegexFeature.NAMED_CAPTURE);

        assertDoesNotThrow(() -> engine.checkSupport(requestedFeatures),
                "Engine should not throw if the requested feature is fully supported");
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException and hit the exception branch if a feature is explicitly unsupported")
    void testValidationFailsForUnsupportedFeatures() {
        StubEngine engine = new StubEngine();

        // Let's pass the feature that the StubEngine explicitly rejects
        Set<RegexFeature> requestedFeatures = Collections.singleton(RegexFeature.RECURSION);

        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> engine.checkSupport(requestedFeatures),
                "Engine must throw when encountering an unsupported feature"
        );

        assertEquals("StubEngine explicitly rejects recursion.", ex.getMessage(),
                "The exception message must exactly match the mapped error message");
    }
}