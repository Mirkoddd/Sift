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

import com.mirkoddd.sift.core.dsl.SiftPattern;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class responsible for translating a compiled {@link SiftPattern} into a human-readable,
 * ASCII-formatted tree explanation.
 * <p>
 * This class acts as the main entry point for the explaining feature of the Sift library.
 * It supports internationalization (i18n) by resolving localized strings via {@link ResourceBundle}.
 * </p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * SiftPattern<?> pattern = SiftPatterns.literal("hello");
 * String explanation = SiftExplainer.explain(pattern, Locale.ITALIAN);
 * System.out.println(explanation);
 * }
 * </pre>
 */
public final class SiftExplainer {

    private static final String BUNDLE_BASE_NAME = "sift_messages";

    private SiftExplainer() {
        // Prevent instantiation of utility class
    }

    /**
     * Translates the given {@link SiftPattern} into a natural English description.
     * <p>
     * This is a convenience method that defaults the output language to {@link Locale#ENGLISH}.
     * </p>
     *
     * @param pattern the Sift pattern to explain (must not be null)
     * @return a formatted ASCII tree string explaining the structure and logic of the pattern
     * @throws IllegalArgumentException if the provided pattern is null or not a recognized base pattern
     */
    public static String explain(SiftPattern<?> pattern) {
        return explain(pattern, null);
    }

    /**
     * Translates the given {@link SiftPattern} into a description based on the provided {@link Locale}.
     * <p>
     * To ensure consistent behavior across different environments and prevent test pollution,
     * this method explicitly disables the standard Java OS locale fallback. If the specific
     * translation for the requested locale is missing, it will fall back directly to the
     * root translation bundle (English).
     * If the provided {@code locale} is null, it gracefully defaults to {@link Locale#ENGLISH}.
     * </p>
     *
     * @param pattern the Sift pattern to explain (must not be null)
     * @param locale  the target language for the explanation (if null, defaults to English)
     * @return a formatted ASCII tree string explaining the structure and logic of the pattern
     * @throws IllegalArgumentException if the provided pattern is null or not a recognized base pattern
     * @throws IllegalStateException    if the base properties file cannot be found in the classpath
     */
    public static String explain(SiftPattern<?> pattern, Locale locale) {
        return explainInternal(pattern, locale, BUNDLE_BASE_NAME);
    }

    static String explainInternal(SiftPattern<?> pattern, Locale locale, String bundleName) {
        if (pattern == null) {
            throw new IllegalArgumentException("SiftPattern cannot be null.");
        }

        if (!(pattern instanceof BaseSiftPattern)) {
            throw new IllegalArgumentException("Pattern must be an instance of BaseSiftPattern.");
        }

        Locale targetLocale = (locale != null) ? locale : Locale.ENGLISH;

        ResourceBundle bundle;
        try {
            ResourceBundle.Control control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
            bundle = ResourceBundle.getBundle(bundleName, targetLocale, control);
        } catch (MissingResourceException e) {
            throw new IllegalStateException(
                    "Failed to load Sift translation bundle: '" + bundleName + "'. " +
                            "Ensure the properties files are correctly placed in your resources directory.", e
            );
        }

        ExplainerVisitor visitor = new ExplainerVisitor(bundle);
        ((BaseSiftPattern<?>) pattern).traverse(visitor);

        return visitor.getExplanation();
    }
}