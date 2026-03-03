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

import com.mirkoddd.sift.core.dsl.ConnectorStep;
import com.mirkoddd.sift.core.dsl.QuantifierStep;

import java.util.Objects;

/**
 * <h2>Sift - Fluent Regex Builder</h2>
 * The main entry point for programmatically building Type-Safe Regular Expressions.
 * <p>
 * Sift enforces a state machine flow (Start -> Quantifier -> Type -> Connector) to prevent
 * syntax errors at compile-time.
 * <p>
 * <b>Thread Safety &amp; Immutability:</b>
 * Sift nodes are <b>100% immutable and thread-safe</b>.
 * Every step in the fluent chain returns a new independent instance.
 * You can safely assign intermediate steps to variables, reuse them to branch off
 * into different regex patterns without poisoning the state, and share them safely
 * across multiple threads (e.g., in Spring Boot controllers or Kotlin Coroutines).
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * String regex = Sift.fromStart()
 *     .exactly(3).digits()
 *     .then()
 *     .oneOrMore().letters()
 *     .shake();
 * }
 * </pre>
 *
 * @author Mirko Dimartino
 * @version {@value BuildInfo#VERSION}
 */
public final class Sift {
    private Sift() {
    }

    /**
     * Configures the Sift builder with global inline flags (e.g., Case Insensitive, Multiline).
     *
     * @param flag  The primary, mandatory flag to apply to the entire regular expression.
     * @param flags Additional optional flags to apply.
     *
     * @return An intermediate step to define the starting position or search strategy for the pattern.
     */
    public static SiftStarter filteringWith(SiftGlobalFlag flag, SiftGlobalFlag... flags) {
        Objects.requireNonNull(flag, "Primary flag cannot be null");
        Objects.requireNonNull(flags, "Additional flags array cannot be null");

        for (SiftGlobalFlag f : flags) {
            Objects.requireNonNull(f, "Additional flag cannot be null");
        }

        SiftGlobalFlag[] allFlags = new SiftGlobalFlag[flags.length + 1];
        allFlags[0] = flag;
        System.arraycopy(flags, 0, allFlags, 1, flags.length);
        return new SiftStarter(allFlags);
    }

    /**
     * Starts building a Regex anchored at the beginning of the string using {@code ^}.
     * <p>
     * Use this for <b>strict validation</b> (e.g., passwords, emails, codes) where the entire string must match
     * the pattern from the very first character.
     *
     * @return A builder configured in the initial state, anchored to the start.
     */
    public static QuantifierStep fromStart() {
        PatternAssembler assembler = new PatternAssembler();
        assembler.addAnchor(RegexSyntax.START_OF_LINE);
        return new SiftQuantifier(assembler);
    }

    /**
     * Starts building a Regex that can match anywhere within a text.
     * <p>
     * Use this for <b>search operations</b> ({@code Matcher.find()}) or data extraction where the pattern
     * does not need to start at the beginning of the string.
     *
     * @return A builder configured for free-floating search (no anchors).
     */
    public static QuantifierStep fromAnywhere() {
        PatternAssembler assembler = new PatternAssembler();
        return new SiftQuantifier(assembler);
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
    public static ConnectorStep fromWordBoundary() {
        PatternAssembler assembler = new PatternAssembler();
        assembler.addWordBoundary();
        return new SiftConnector(assembler);
    }

    /**
     * Intermediate configuration class to maintain the fluent API flow after setting flags.
     */
    public static final class SiftStarter {
        private final SiftGlobalFlag[] flags;

        private SiftStarter(SiftGlobalFlag... flags) {
            this.flags = flags;
        }

        /**
         * Starts building a Regex anchored at the beginning of the string using {@code ^},
         * applying the previously configured global flags.
         * <p>
         * Use this for <b>strict validation</b> (e.g., passwords, emails, codes) where the entire string must match
         * the pattern from the very first character.
         *
         * @return A builder configured in the initial state, anchored to the start.
         */
        public QuantifierStep fromStart() {
            PatternAssembler assembler = new PatternAssembler(flags);
            assembler.addAnchor(RegexSyntax.START_OF_LINE);
            return new SiftQuantifier(assembler);
        }

        /**
         * Starts building a Regex that can match anywhere within a text,
         * applying the previously configured global flags.
         * <p>
         * Use this for <b>search operations</b> ({@code Matcher.find()}) or data extraction where the pattern
         * does not need to start at the beginning of the string.
         *
         * @return A builder configured for free-floating search (no anchors).
         */
        public QuantifierStep fromAnywhere() {
            PatternAssembler assembler = new PatternAssembler(flags);
            return new SiftQuantifier(assembler);
        }

        /**
         * Starts building a Regex beginning with a <b>Word Boundary</b> {@code \b},
         * applying the previously configured global flags.
         * <p>
         * A word boundary asserts that the position is between a word character ({@code \w})
         * and a non-word character ({@code \W}), or the start/end of the string.
         * Use this to find whole words (e.g., matching "cat" but not "catalog").
         *
         * @return A connector step ready to append the word to match.
         */
        public ConnectorStep fromWordBoundary() {
            PatternAssembler assembler = new PatternAssembler(flags);
            assembler.addWordBoundary();
            return new SiftConnector(assembler);
        }
    }
}