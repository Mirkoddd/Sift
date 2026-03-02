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
package com.mirkoddd.sift.core.dsl;

import java.util.regex.Pattern;

/**
 * Represents a component that can be converted into a valid Regex string.
 * <p>
 * This is a <b>Functional Interface</b> serving as the building block for the entire library.
 * It allows any part of the chain (Connectors, custom patterns, or lambda expressions)
 * to be treated uniformly as a regex source.
 * <p>
 * <b>Example of custom implementation:</b>
 * <pre>
 * SiftPattern myPattern = () -> "[a-z]{3}";
 * </pre>
 */
@FunctionalInterface
public interface SiftPattern {

    /**
     * Finalizes the construction and returns the raw Regular Expression string.
     * <p>
     * This method "shakes" the builder components together to produce the final output.
     * It is typically the last method called in the chain.
     *
     * @return The compiled regex string (e.g., {@code "^[0-9]+$"}).
     */
    String shake();

    /**
     * Wraps this pattern in an Atomic Group to prevent Catastrophic Backtracking (ReDoS).
     * <p>
     * Once the regex engine finds a match inside an atomic group, it will lock it
     * and never backtrack into it to try different alternatives, significantly
     * boosting performance on complex sub-patterns.
     *
     * @return A new SiftPattern wrapped in an atomic group {@code (?>...)}.
     */
    default SiftPattern preventBacktracking() {
        final String atomicOpen = "(?>";
        final String atomicClose = ")";
        return new SiftPattern() {
            private volatile String cachedRegex = null;
            private volatile Pattern cachedPattern = null;

            @Override
            public String shake() {
                if (cachedRegex == null) {
                    synchronized (this) {
                        if (cachedRegex == null) {
                            cachedRegex = atomicOpen + SiftPattern.this.shake() + atomicClose;
                        }
                    }
                }
                return cachedRegex;
            }

            @Override
            public Pattern sieve() {
                if (cachedPattern == null) {
                    synchronized (this) {
                        if (cachedPattern == null) {
                            cachedPattern = Pattern.compile(shake());
                        }
                    }
                }
                return cachedPattern;
            }
        };
    }

    /**
     * Compiles this pattern into a {@link java.util.regex.Pattern}.
     * <p>
     * This is the natural completion of the builder chain: <b>Sift</b> → <b>shake</b> → <b>sieve</b>.
     * Use this when you need a compiled {@link java.util.regex.Pattern} directly,
     * rather than the raw regex string.
     * <p>
     * The default implementation compiles the result of {@link #shake()} on every call.
     * {@code SiftBuilder} overrides this method to reuse the {@link java.util.regex.Pattern}
     * already compiled internally by {@link #shake()}, avoiding any redundant compilation.
     *
     * @return A compiled {@link java.util.regex.Pattern} ready for matching.
     */
    default Pattern sieve() {
        return Pattern.compile(this.shake());
    }

    /**
     * Convenience method to quickly test if a given input matches this pattern completely.
     * Under the hood, this compiles the regex (or uses the cached one) and evaluates it.
     *
     * @param input The text to evaluate.
     * @return true if the entire input matches the pattern, false otherwise or if input is null.
     */
    default boolean matches(CharSequence input) {
        if (input == null) {
            return false;
        }
        return sieve().matcher(input).matches();
    }
}