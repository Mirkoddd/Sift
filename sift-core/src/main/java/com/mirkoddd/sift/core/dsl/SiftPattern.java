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
 * Represents a compiled regex component within a specific structural context.
 * <p>
 * <b>Type Safety:</b> The {@code Ctx} parameter enforces Type-Driven Design, ensuring
 * that fragments cannot be used where absolute roots (anchored patterns) are required,
 * and vice-versa.
 * <p>
 * <b>Thread Safety:</b> All implementations are guaranteed to be immutable and thread-safe.
 *
 * @param <Ctx> The {@link SiftContext} (Fragment or Root) that defines where this
 * pattern can be legally composed.
 */
public interface SiftPattern<Ctx extends SiftContext> extends SiftInternalSealer {

    /**
     * Finalizes the builder chain and returns the raw Regular Expression string.
     * <p>
     * This is an idempotent operation. Subsequent calls return the cached result.
     *
     * @return The generated regex string (e.g., {@code "[0-9]{3}"}).
     * @throws IllegalStateException if the internal state produces an invalid regex syntax.
     */
    String shake();

    /**
     * Compiles this pattern into a standard {@link java.util.regex.Pattern}.
     * <p>
     * Use this for high-performance matching. Implementations use internal caching
     * to ensure the compilation happens only once.
     *
     * @return A compiled and ready-to-use Regex Pattern.
     */
    Pattern sieve();

    /**
     * Wraps this pattern in an <b>Atomic Group</b> {@code (?>...)}.
     * <p>
     * This is a critical tool for ReDoS (Regular Expression Denial of Service) mitigation.
     * It prevents the regex engine from backtracking into this sub-pattern once matched,
     * significantly improving performance on complex or ambiguous strings.
     *
     * @return A decorated pattern protected against catastrophic backtracking.
     */
    SiftPattern<Ctx> preventBacktracking();

    /**
     * Performs a substring search (grep-style) on the provided input.
     * <p>
     * <b>Note:</b> Under the hood, this uses {@code Matcher.find()} rather than
     * {@code Matcher.matches()}. It will return true if the pattern is found <i>anywhere</i>
     * within the input sequence, not necessarily spanning the entire string.
     *
     * @param input The character sequence to search within.
     * @return {@code true} if any part of the input matches this pattern.
     */
    default boolean matches(CharSequence input) {
        if (input == null) return false;
        return sieve().matcher(input).find();
    }
}