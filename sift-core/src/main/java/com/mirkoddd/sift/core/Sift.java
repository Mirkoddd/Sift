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

import com.mirkoddd.sift.core.dsl.*;
import com.mirkoddd.sift.core.engine.RegexFeature;

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
 */
public final class Sift {

    private Sift() {
        // Prevents instantiation
    }

    /**
     * Initializes the Sift builder with global inline flags (e.g., CASE_INSENSITIVE).
     * <p>
     * <b>Note:</b> Applying global flags permanently alters the engine's behavior. Therefore,
     * patterns generated after calling this method are strictly forced to be {@code Root}
     * contexts and cannot be embedded into other fragments to prevent side-effects.
     *
     * @param flag  The primary global flag to apply.
     * @param flags Optional additional global flags.
     * @return A starter step ensuring the context remains securely anchored as a Root.
     * @throws NullPointerException if the primary flag or any element in the flags array is null.
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
     * Returns a <b>Root</b> context, meaning it cannot be embedded inside other patterns.
     *
     * @return The initial quantifier step to start the definition.
     */
    public static Quantifier<Root> fromStart() {
        PatternAssembler assembler = new PatternAssembler();
        assembler.addAnchor(RegexSyntax.START_OF_LINE);
        return new SiftQuantifier<>(assembler);
    }

    /**
     * Starts building a Regex that can match anywhere within a text.
     * Returns a <b>Fragment</b> context, making it safe to embed into other patterns.
     *
     * @return The initial quantifier step to start the definition.
     */
    public static Quantifier<Fragment> fromAnywhere() {
        PatternAssembler assembler = new PatternAssembler();
        return new SiftQuantifier<>(assembler);
    }

    /**
     * Starts building a Regex beginning with a <b>Word Boundary</b> {@code \b}.
     * Returns a <b>Fragment</b> context.
     *
     * @return The standard connector step, bypassing the initial quantifier.
     */
    public static Connector<Fragment> fromWordBoundary() {
        PatternAssembler assembler = new PatternAssembler();
        assembler.addWordBoundary();
        return new SiftConnector<>(assembler);
    }

    /**
     * Starts building a Regex anchored at the end of the previous match using {@code \G}.
     * <p>
     * This is particularly useful for iterative parsing with {@code Matcher.find()}, ensuring
     * that the next match begins exactly where the last one ended, without skipping characters.
     * Returns a <b>Root</b> context, meaning it cannot be embedded inside other patterns.
     *
     * @return The initial quantifier step to start the definition.
     */
    public static Quantifier<Root> fromPreviousMatchEnd() {
        PatternAssembler assembler = new PatternAssembler();
        assembler.registerFeature(RegexFeature.PREVIOUS_MATCH_ANCHOR);
        assembler.addAnchor(RegexSyntax.PREVIOUS_MATCH_END);
        return new SiftQuantifier<>(assembler);
    }

    /**
     * Convenience shortcut to start an unanchored Fragment matching exactly the specified number of times.
     *
     * @param count The exact number of repetitions.
     * @return The type step to define what to repeat.
     */
    public static Type<Fragment, Connector<Fragment>, CharacterConnector<Fragment>> exactly(int count) {
        return fromAnywhere().exactly(count);
    }

    /**
     * Convenience shortcut to start an unanchored Fragment matching at least the specified number of times.
     *
     * @param count The minimum number of repetitions.
     * @return The type step to define what to repeat.
     */
    public static Type<Fragment, VariableConnector<Fragment>, VariableCharacterConnector<Fragment>> atLeast(int count) {
        return fromAnywhere().atLeast(count);
    }

    /**
     * Convenience shortcut to start an unanchored Fragment matching between a minimum and maximum number of times.
     *
     * @param min The minimum number of repetitions.
     * @param max The maximum number of repetitions.
     * @return The type step to define what to repeat.
     */
    public static Type<Fragment, VariableConnector<Fragment>, VariableCharacterConnector<Fragment>> between(int min, int max) {
        return fromAnywhere().between(min, max);
    }

    /**
     * Convenience shortcut to start an unanchored Fragment matching one or more times.
     *
     * @return The type step to define what to repeat.
     */
    public static Type<Fragment, VariableConnector<Fragment>, VariableCharacterConnector<Fragment>> oneOrMore() {
        return fromAnywhere().oneOrMore();
    }

    /**
     * Convenience shortcut to start an unanchored Fragment matching zero or more times.
     *
     * @return The type step to define what to repeat.
     */
    public static Type<Fragment, VariableConnector<Fragment>, VariableCharacterConnector<Fragment>> zeroOrMore() {
        return fromAnywhere().zeroOrMore();
    }

    /**
     * Convenience shortcut to start an unanchored Fragment marking the next element as optional.
     *
     * @return The type step to define what is optional.
     */
    public static Type<Fragment, VariableConnector<Fragment>, VariableCharacterConnector<Fragment>> optional() {
        return fromAnywhere().optional();
    }

    /**
     * Intermediate configuration class to maintain the fluent API flow after setting flags.
     * <p>
     * <b>Context Safety:</b> All methods in this starter strictly return a {@code Root} context.
     * Global flags (like {@code (?i)}) apply to the entire regex engine. Preventing these
     * patterns from being embedded as fragments avoids cross-contamination of flags
     * across composed patterns.
     */
    public static final class SiftStarter {
        private final SiftGlobalFlag[] flags;

        private SiftStarter(SiftGlobalFlag... flags) {
            this.flags = flags;
        }

        /**
         * Starts building a flagged Regex anchored at the beginning of the string using {@code ^}.
         *
         * @return The initial quantifier step.
         */
        public Quantifier<Root> fromStart() {
            PatternAssembler assembler = new PatternAssembler(flags);
            assembler.addAnchor(RegexSyntax.START_OF_LINE);
            return new SiftQuantifier<>(assembler);
        }

        /**
         * Starts building a flagged Regex that can match anywhere within a text.
         *
         * @return The initial quantifier step.
         */
        public Quantifier<Root> fromAnywhere() {
            PatternAssembler assembler = new PatternAssembler(flags);
            return new SiftQuantifier<>(assembler);
        }

        /**
         * Starts building a flagged Regex beginning with a Word Boundary {@code \b}.
         *
         * @return The standard connector step.
         */
        public Connector<Root> fromWordBoundary() {
            PatternAssembler assembler = new PatternAssembler(flags);
            assembler.addWordBoundary();
            return new SiftConnector<>(assembler);
        }

        /**
         * Starts building a flagged Regex anchored at the end of the previous match using {@code \G}.
         *
         * @return The initial quantifier step.
         */
        public Quantifier<Root> fromPreviousMatchEnd() {
            PatternAssembler assembler = new PatternAssembler(flags);
            assembler.registerFeature(RegexFeature.PREVIOUS_MATCH_ANCHOR);
            assembler.addAnchor(RegexSyntax.PREVIOUS_MATCH_END);
            return new SiftQuantifier<>(assembler);
        }

        /**
         * Shortcut to start a flagged Root pattern matching exactly the specified number of times.
         *
         * @param count The exact number of repetitions.
         * @return The type step to define what to repeat.
         */
        public Type<Root, Connector<Root>, CharacterConnector<Root>> exactly(int count) {
            return fromAnywhere().exactly(count);
        }

        /**
         * Shortcut to start a flagged Root pattern matching at least the specified number of times.
         *
         * @param count The minimum number of repetitions.
         * @return The type step to define what to repeat.
         */
        public Type<Root, VariableConnector<Root>, VariableCharacterConnector<Root>> atLeast(int count) {
            return fromAnywhere().atLeast(count);
        }

        /**
         * Shortcut to start a flagged Root pattern matching between a minimum and maximum number of times.
         *
         * @param min The minimum number of repetitions.
         * @param max The maximum number of repetitions.
         * @return The type step to define what to repeat.
         */
        public Type<Root, VariableConnector<Root>, VariableCharacterConnector<Root>> between(int min, int max) {
            return fromAnywhere().between(min, max);
        }

        /**
         * Shortcut to start a flagged Root pattern matching one or more times.
         *
         * @return The type step to define what to repeat.
         */
        public Type<Root, VariableConnector<Root>, VariableCharacterConnector<Root>> oneOrMore() {
            return fromAnywhere().oneOrMore();
        }

        /**
         * Shortcut to start a flagged Root pattern matching zero or more times.
         *
         * @return The type step to define what to repeat.
         */
        public Type<Root, VariableConnector<Root>, VariableCharacterConnector<Root>> zeroOrMore() {
            return fromAnywhere().zeroOrMore();
        }

        /**
         * Shortcut to start a flagged Root pattern marking the next element as optional.
         *
         * @return The type step to define what is optional.
         */
        public Type<Root, VariableConnector<Root>, VariableCharacterConnector<Root>> optional() {
            return fromAnywhere().optional();
        }
    }
}