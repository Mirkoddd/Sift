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

import com.mirkoddd.sift.core.SiftExplainer;
import com.mirkoddd.sift.core.engine.JdkEngine;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import com.mirkoddd.sift.core.engine.SiftEngine;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
     * Compiles the constructed pattern using a custom execution engine.
     * This allows swapping the default JDK regex engine with third-party
     * alternatives (like RE2J for ReDoS protection).
     *
     * @param engine The specific engine adapter to use.
     * @return The compiled, executable pattern.
     */
    SiftCompiledPattern sieveWith(SiftEngine engine);

    /**
     * Compiles the constructed pattern using the default JDK execution engine.
     *
     * @return The compiled, executable pattern.
     */
    default SiftCompiledPattern sieve() {
        return sieveWith(JdkEngine.INSTANCE);
    }

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
     * Checks if the pattern can be found anywhere within the input sequence.
     * <p>
     * <b>Semantics:</b> This strictly delegates to {@link java.util.regex.Matcher#find()}
     * via the default execution engine. It returns {@code true} if the input contains
     * at least one subsequence that matches the pattern.
     *
     * @param input The character sequence to search within.
     * @return {@code true} if any part of the input matches this pattern.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default boolean containsMatchIn(CharSequence input) {
        if (input == null) return false;
        return sieve().containsMatchIn(input);
    }

    /**
     * Checks if the entire input sequence matches the pattern exactly.
     * <p>
     * <b>Semantics:</b> This strictly delegates to {@link java.util.regex.Matcher#matches()}
     * via the default execution engine. It returns {@code true} only if the pattern
     * matches the sequence from start to finish.
     * <p>
     * <b>Performance Note:</b> This convenience method compiles the regex on the fly.
     * If you are validating inputs inside a tight loop, it is highly recommended to call
     * {@link #sieve()} once and reuse the resulting {@link SiftCompiledPattern} instead.
     *
     * @param input The character sequence to validate.
     * @return {@code true} if the entire sequence matches this pattern.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default boolean matchesEntire(CharSequence input) {
        if (input == null) return false;
        return sieve().matchesEntire(input);
    }

    /**
     * Extracts the first subsequence that matches the pattern.
     *
     * @param input The character sequence to search within.
     * @return An {@link Optional} containing the matched string, or empty if not found.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default Optional<String> extractFirst(CharSequence input) {
        if (input == null) return Optional.empty();
        return sieve().extractFirst(input);
    }

    /**
     * Extracts all subsequences that match the pattern.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable list of all matched strings. Returns an empty list if none are found.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default List<String> extractAll(CharSequence input) {
        if (input == null) return Collections.emptyList();
        return sieve().extractAll(input);
    }

    /**
     * Replaces the first subsequence of the input that matches the pattern with the given string.
     * <p>
     * <b>Null-Safety:</b> If the provided input is {@code null}, this method safely returns
     * {@code null} without throwing an exception, preserving the original absence of data.
     * <p>
     * <b>Performance Note:</b> This convenience method compiles the regex on the fly.
     * If you are processing inputs inside a tight loop, it is highly recommended to call
     * {@link #sieve()} once and reuse the resulting {@link SiftCompiledPattern} instead.
     *
     * @param input       The character sequence to search within, may be null.
     * @param replacement The replacement string.
     * @return The resulting string with the first matching replacement applied, or {@code null} if the input was null.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default String replaceFirst(CharSequence input, String replacement) {
        if (input == null) return null;
        return sieve().replaceFirst(input, replacement);
    }

    /**
     * Replaces every subsequence of the input that matches the pattern with the given string.
     * <p>
     * <b>Null-Safety:</b> If the provided input is {@code null}, this method safely returns
     * {@code null} without throwing an exception, preserving the original absence of data.
     * <p>
     * <b>Performance Note:</b> This convenience method compiles the regex on the fly.
     * If you are processing inputs inside a tight loop, it is highly recommended to call
     * {@link #sieve()} once and reuse the resulting {@link SiftCompiledPattern} instead.
     *
     * @param input       The character sequence to search within, may be null.
     * @param replacement The replacement string.
     * @return The resulting string with replacements applied, or {@code null} if the input was null.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default String replaceAll(CharSequence input, String replacement) {
        if (input == null) return null;
        return sieve().replaceAll(input, replacement);
    }

    /**
     * Extracts all explicitly named capture groups from the first match found in the input.
     * <p>
     * <b>Java 8 Compatibility Note:</b> Native extraction of group names is safely handled
     * by the underlying engine, ensuring backwards compatibility even when standard API
     * support is lacking.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable map containing the group names as keys and their matched substrings as values.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default Map<String, String> extractGroups(CharSequence input) {
        if (input == null) return Collections.emptyMap();
        return sieve().extractGroups(input);
    }

    /**
     * Extracts all explicitly named capture groups from every match found in the input.
     * <p>
     * <b>Java 8 Compatibility Note:</b> Native extraction of group names is safely handled
     * by the underlying engine, ensuring backwards compatibility.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable list of unmodifiable maps, where each map represents a single match
     * and contains the group names as keys and their matched substrings as values.
     * Returns an empty list if no matches are found.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default List<Map<String, String>> extractAllGroups(CharSequence input) {
        if (input == null) return Collections.emptyList();
        return sieve().extractAllGroups(input);
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     * @param input The character sequence to be split.
     * @return A list of strings computed by splitting the input around matches.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default List<String> splitBy(CharSequence input) {
        if (input == null) return Collections.emptyList();
        return sieve().splitBy(input);
    }

    /**
     * Returns a lazy stream of match results.
     * <p>
     * Unlike {@link #extractAll(CharSequence)}, this method does not load all matches
     * into memory at once. It evaluates the pattern lazily as the stream is consumed,
     * making it ideal for processing massive text inputs or large files without triggering
     * OutOfMemory errors.
     *
     * @param input The character sequence to search within.
     * @return A sequential {@link Stream} of matched substrings.
     */
    @SuppressWarnings("resource") // Intentional: sieve() uses JdkEngine where close() is a no-op. Omitted try-with-resources to prevent coverage bloat from unreachable bytecode.
    default Stream<String> streamMatches(CharSequence input) {
        if (input == null) return Stream.empty();
        return sieve().streamMatches(input);
    }

    /**
     * Provides a human-readable ASCII tree explanation of the pattern structure in English.
     * <p>
     * This is a convenience method that delegates to {@link com.mirkoddd.sift.core.SiftExplainer}.
     * It is useful for debugging and logging the logic of the composed regex.
     * </p>
     *
     * @return a formatted string representing the pattern hierarchy.
     */
    default String explain() {
        return SiftExplainer.explain(this);
    }

    /**
     * Provides a localized human-readable ASCII tree explanation of the pattern structure.
     * <p>
     * If the translation bundle for the requested {@code locale} is missing or incomplete,
     * the explainer will automatically fall back to the default English translation.
     * </p>
     *
     * @param locale the target locale for the explanation. If null, it defaults to English.
     * @return a localized string explaining the pattern, or the English fallback if the locale is unsupported.
     */
    default String explain(Locale locale) {
        return SiftExplainer.explain(this, locale);
    }
}