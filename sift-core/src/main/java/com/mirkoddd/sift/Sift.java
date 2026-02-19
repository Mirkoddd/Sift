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
package com.mirkoddd.sift;

import com.mirkoddd.sift.dsl.ConnectorStep;
import com.mirkoddd.sift.dsl.QuantifierStep;

/**
 * <h2>Sift - Fluent Regex Builder</h2>
 * The main entry point for programmatically building Type-Safe Regular Expressions.
 * <p>
 * Sift enforces a state machine flow (Start -> Quantifier -> Type -> Connector) to prevent
 * syntax errors at compile-time.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * String regex = Sift.fromStart()
 * .exactly(3).digits()
 * .followedBy()
 * .oneOrMore().letters()
 * .shake();
 * }
 * </pre>
 *
 * @author Mirko Dimartino
 * @version 1.0
 */
public class Sift {

    /**
     * Starts building a Regex anchored at the beginning of the string using {@code ^}.
     * <p>
     * Use this for <b>strict validation</b> (e.g., passwords, emails, codes) where the entire string must match
     * the pattern from the very first character.
     *
     * @return A builder configured in the initial state, anchored to the start.
     */
    public static QuantifierStep fromStart() {
        return new SiftBuilder().anchorStart();
    }

    /**
     * Starts building a Regex that can match anywhere within a text.
     * <p>
     * Use this for <b>search operations</b> ({@code Matcher.find()}) or data extraction where the pattern
     * does not need to start at the beginning of the string.
     *
     * @return A builder configured for free-floating search (no anchors).
     */
    public static QuantifierStep anywhere() {
        return new SiftBuilder();
    }

    /**
     * Starts building a Regex beginning with a <b>Word Boundary</b> {@code \b}.
     * <p>
     * A word boundary asserts that the position is between a word character ({@code \w})
     * and a non-word character ({@code \W}), or the start/end of the string.
     * Use this to find whole words (e.g., matching "cat" but not "catalog").
     *
     * @return A connector step ready to append the word to match.
     */
    public static ConnectorStep wordBoundary() {
        return new SiftBuilder().wordBoundary();
    }
}