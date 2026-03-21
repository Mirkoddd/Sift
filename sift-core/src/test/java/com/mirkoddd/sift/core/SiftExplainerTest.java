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

import com.mirkoddd.sift.core.dsl.Composable;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import com.mirkoddd.sift.core.engine.SiftEngine;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static com.mirkoddd.sift.core.SiftPatterns.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SiftExplainerTest {
    private final SiftPattern<?> dummyPattern = literal("X");

    @Test
    void testExplainWithDefaultNameAndLocale() {
        String result = SiftExplainer.explain(dummyPattern);

        assertTrue(result.contains("└─ Matches the literal character 'X'"));
    }

    @Test
    void testExplainWithCustomName() {
        String result = SiftExplainer.explain(dummyPattern);

        assertTrue(result.contains("└─ Matches the literal character 'X'"));
    }

    @Test
    void testExplainWithCustomNameAndLocale() {
        String result = SiftExplainer.explain(dummyPattern, Locale.ENGLISH);

        assertTrue(result.contains("└─ Matches the literal character 'X'"));
    }

    @Test
    void testExplainThrowsExceptionOnInvalidPattern() {
        SiftPattern<Composable> invalidPattern = new SiftPattern<Composable>() {
            @Override
            public Object ___internal_lock___() {
                return null;
            }

            @Override
            public String shake() {
                return "";
            }

            @Override
            public SiftCompiledPattern sieveWith(SiftEngine engine) {
                return null;
            }

            @Override
            public SiftPattern<Composable> preventBacktracking() {
                return null;
            }
        };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SiftExplainer.explain(invalidPattern)
        );

        assertEquals("Pattern must be an instance of BaseSiftPattern.", exception.getMessage());
    }

    @Test
    void testPrivateConstructorForCoverage() throws Exception {
        Constructor<SiftExplainer> constructor = SiftExplainer.class.getDeclaredConstructor();

        // Verify it is indeed private as a bonus check
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        // Force access and instantiation to satisfy the coverage tool
        constructor.setAccessible(true);
        SiftExplainer instance = constructor.newInstance();

        assertNotNull(instance);
    }

    @Test
    void testExplainWithNullPatternThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SiftExplainer.explain(null, Locale.ENGLISH)
        );

        assertEquals("SiftPattern cannot be null.", exception.getMessage());
    }

    @Test
    void testExplainMissingResourceBundleThrowsException() {
        SiftPattern<?> dummyPattern = literal("hello");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> SiftExplainer.explainInternal(dummyPattern, Locale.ENGLISH, "not_existing_bundle")
        );

        assertTrue(
                exception.getMessage().contains("Failed to load Sift translation bundle")
        );
    }

    @Test
    void testTranslation(){
        SiftPattern<?> testPattern = SiftCatalog.macAddress();

        String english = SiftExplainer.explain(testPattern, Locale.ENGLISH);
        System.out.println("ENGLISH");
        System.out.println(english);
        System.out.println("----------");

        String italian = SiftExplainer.explain(testPattern, Locale.ITALIAN);
        System.out.println("ITALIAN");
        System.out.println(italian);
        System.out.println("----------");


        assertNotEquals(english, italian);
        assertEquals(testPattern.explain(), english);
        assertEquals(testPattern.explain(null), english);
        assertEquals(testPattern.explain(Locale.ITALIAN), italian);
    }

    @Test
    void verifyBundleKeysAlignment() {
        ResourceBundle english = ResourceBundle.getBundle("sift_messages", Locale.ENGLISH);
        ResourceBundle italian = ResourceBundle.getBundle("sift_messages", Locale.ITALIAN);

        Set<String> englishKeys = english.keySet();
        Set<String> italianKeys = italian.keySet();

        for (String key : englishKeys) {
            assertTrue(italianKeys.contains(key),
                    "Missing translation in Italian bundle for key: " + key);
        }

        for (String key : italianKeys) {
            assertTrue(englishKeys.contains(key),
                    "Orphaned key in Italian bundle not present in English: " + key);
        }
    }
}