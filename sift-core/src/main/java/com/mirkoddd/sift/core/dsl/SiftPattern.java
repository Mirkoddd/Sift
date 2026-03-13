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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
     * Checks if the pattern can be found anywhere within the input sequence.
     * <p>
     * <b>Semantics:</b> This strictly delegates to {@link java.util.regex.Matcher#find()}.
     * It returns {@code true} if the input contains at least one subsequence that matches the pattern.
     *
     * @param input The character sequence to search within.
     * @return {@code true} if any part of the input matches this pattern.
     */
    default boolean containsMatchIn(CharSequence input) {
        if (input == null) return false;
        return sieve().matcher(input).find();
    }

    /**
     * Checks if the entire input sequence matches the pattern exactly.
     * <p>
     * <b>Semantics:</b> This strictly delegates to {@link java.util.regex.Matcher#matches()}.
     * It returns {@code true} only if the pattern matches the sequence from start to finish.
     *
     * @param input The character sequence to validate.
     * @return {@code true} if the entire sequence matches this pattern.
     */
    default boolean matchesEntire(CharSequence input) {
        if (input == null) return false;
        return sieve().matcher(input).matches();
    }

    /**
     * Performs a substring search (grep-style) on the provided input.
     *
     * @param input The character sequence to search within.
     * @return {@code true} if any part of the input matches this pattern.
     * @deprecated The name {@code matches} is semantically misleading in Java as it implies
     * strict full-string validation (like {@link String#matches(String)}).
     * <p>
     * <b>Migration guide:</b>
     * <ul>
     * <li>Use {@link #containsMatchIn(CharSequence)} for partial matching (Matcher.find).</li>
     * <li>Use {@link #matchesEntire(CharSequence)} for strict validation (Matcher.matches).</li>
     * </ul>
     */
    @Deprecated
    default boolean matches(CharSequence input) {
        return containsMatchIn(input);
    }

    /**
     * Extracts the first subsequence that matches the pattern.
     *
     * @param input The character sequence to search within.
     * @return An {@link Optional} containing the matched string, or empty if not found.
     */
    default Optional<String> extractFirst(CharSequence input) {
        if (input == null) return Optional.empty();
        Matcher matcher = sieve().matcher(input);
        return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
    }

    /**
     * Extracts all subsequences that match the pattern.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable list of all matched strings. Returns an empty list if none are found.
     */
    default List<String> extractAll(CharSequence input) {
        if (input == null) return Collections.emptyList();
        Matcher matcher = sieve().matcher(input);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Replaces the first subsequence of the input that matches the pattern with the given string.
     *
     * @param input       The character sequence to search within.
     * @param replacement The replacement string.
     * @return The resulting string with the first matching replacement applied.
     */
    default String replaceFirst(CharSequence input, String replacement) {
        if (input == null) return "";
        return sieve().matcher(input).replaceFirst(replacement);
    }

    /**
     * Replaces every subsequence of the input that matches the pattern with the given string.
     *
     * @param input       The character sequence to search within.
     * @param replacement The replacement string.
     * @return The resulting string with replacements applied.
     */
    default String replaceAll(CharSequence input, String replacement) {
        if (input == null) return "";
        return sieve().matcher(input).replaceAll(replacement);
    }

    /**
     * Extracts all explicitly named capture groups from the first match found in the input.
     * <p>
     * <b>Java 8 Compatibility Note:</b> Native extraction of group names is not supported
     * by the {@code Matcher} API in early Java versions. This method safely parses the generated
     * pattern string to discover the defined group names and maps them to their matched values.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable map containing the group names as keys and their matched substrings as values.
     */
    default Map<String, String> extractGroups(CharSequence input) {
        if (input == null) return Collections.emptyMap();

        Pattern compiledPattern = sieve();
        Matcher matcher = compiledPattern.matcher(input);

        if (!matcher.find()) {
            return Collections.emptyMap();
        }

        Map<String, String> extractedGroups = new HashMap<>();

        // Safely extract group names from the pattern string: (?<GroupName>...)
        Matcher nameExtractor = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(compiledPattern.pattern());

        while (nameExtractor.find()) {
            String groupName = nameExtractor.group(1);
            try {
                String matchValue = matcher.group(groupName);
                if (matchValue != null) {
                    extractedGroups.put(groupName, matchValue);
                }
            } catch (IllegalArgumentException e) {
                // Defensive catch: ignores malformed or uncaptured group names
            }
        }

        return Collections.unmodifiableMap(extractedGroups);
    }

    /**
     * Extracts all explicitly named capture groups from every match found in the input.
     * <p>
     * <b>Java 8 Compatibility Note:</b> Native extraction of group names is not supported
     * by the {@code Matcher} API in early Java versions. This method safely parses the generated
     * pattern string to discover the defined group names and maps them to their matched values
     * for each successful match.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable list of unmodifiable maps, where each map represents a single match
     * and contains the group names as keys and their matched substrings as values.
     * Returns an empty list if no matches are found.
     */
    default List<Map<String, String>> extractAllGroups(CharSequence input) {
        if (input == null) return Collections.emptyList();

        Pattern compiledPattern = sieve();
        Matcher matcher = compiledPattern.matcher(input);
        Matcher nameExtractor = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>")
                .matcher(compiledPattern.pattern());

        List<String> groupNames = new ArrayList<>();
        while (nameExtractor.find()) {
            groupNames.add(nameExtractor.group(1));
        }

        List<Map<String, String>> results = new ArrayList<>();
        while (matcher.find()) {
            Map<String, String> groups = new HashMap<>();
            for (String name : groupNames) {
                try {
                    String value = matcher.group(name);
                    if (value != null) groups.put(name, value);
                } catch (IllegalArgumentException e) {
                    // Defensive catch: ignores malformed or uncaptured group names
                }
            }
            results.add(Collections.unmodifiableMap(groups));
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     * @param input The character sequence to be split.
     * @return A list of strings computed by splitting the input around matches.
     */
    default List<String> splitBy(CharSequence input) {
        if (input == null) return Collections.emptyList();
        return Arrays.asList(sieve().split(input));
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
    default Stream<String> streamMatches(CharSequence input) {
        if (input == null) return Stream.empty();

        Matcher matcher = sieve().matcher(input);

        // Custom Spliterator to provide lazy Matcher.find() evaluation in Java 8
        Spliterator<String> lazySpliterator = new Spliterators.AbstractSpliterator<String>(
                Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
            @Override
            public boolean tryAdvance(Consumer<? super String> action) {
                if (matcher.find()) {
                    action.accept(matcher.group());
                    return true;
                }
                return false;
            }
        };

        return StreamSupport.stream(lazySpliterator, false);
    }
}