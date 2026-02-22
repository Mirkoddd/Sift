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
package com.mirkoddd.sift.dsl;

import com.mirkoddd.sift.internal.RegexSyntax;

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
    default SiftPattern withoutBacktracking() {
        return () -> RegexSyntax.ATOMIC_GROUP_OPEN + this.shake() + RegexSyntax.GROUP_CLOSE;
    }
}