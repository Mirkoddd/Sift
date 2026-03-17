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
package com.mirkoddd.sift.core.engine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * <b>Thread Safety:</b> All implementations must be deeply immutable and inherently
 * thread-safe.
 * <p>
 * <b>Resource Management:</b> This interface extends {@link AutoCloseable} to support
 * stateful or native regex engines (like GraalVM) that require explicit memory cleanup.
 * While default stateless engines (like the standard JDK engine) do not hold open resources,
 * it is highly recommended to wrap instances in a {@code try-with-resources} block.
 * This ensures your application remains leak-free and engine-agnostic if you decide
 * to swap the underlying execution engine in the future.
 *
 * @since 6.0.0
 */
public interface SiftCompiledPattern extends AutoCloseable{

    /**
     * Checks if the pattern can be found anywhere within the input sequence.
     *
     * @param input The character sequence to search within.
     * @return {@code true} if any part of the input matches this pattern.
     */
    boolean containsMatchIn(CharSequence input);

    /**
     * Checks if the entire input sequence matches the pattern exactly.
     *
     * @param input The character sequence to validate.
     * @return {@code true} only if the entire sequence matches this pattern.
     */
    boolean matchesEntire(CharSequence input);

    /**
     * Extracts the first subsequence that matches the pattern.
     *
     * @param input The character sequence to search within.
     * @return An {@link Optional} containing the matched string, or empty if not found.
     */
    Optional<String> extractFirst(CharSequence input);

    /**
     * Extracts all subsequences that match the pattern.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable list of all matched strings. Returns an empty list if none are found.
     */
    List<String> extractAll(CharSequence input);

    /**
     * Replaces the first subsequence of the input that matches the pattern with the given string.
     *
     * @param input       The character sequence to search within.
     * @param replacement The replacement string.
     * @return The resulting string with the first matching replacement applied.
     */
    String replaceFirst(CharSequence input, String replacement);

    /**
     * Replaces every subsequence of the input that matches the pattern with the given string.
     *
     * @param input       The character sequence to search within.
     * @param replacement The replacement string.
     * @return The resulting string with replacements applied.
     */
    String replaceAll(CharSequence input, String replacement);

    /**
     * Extracts all explicitly named capture groups from the first match found in the input.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable map containing the group names as keys and their matched substrings as values.
     */
    Map<String, String> extractGroups(CharSequence input);

    /**
     * Extracts all explicitly named capture groups from every match found in the input.
     *
     * @param input The character sequence to search within.
     * @return An unmodifiable list of unmodifiable maps, where each map represents a single match.
     */
    List<Map<String, String>> extractAllGroups(CharSequence input);

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     * @param input The character sequence to be split.
     * @return An unmodifiable list of strings computed by splitting the input around matches.
     */
    List<String> splitBy(CharSequence input);

    /**
     * Returns a lazy stream of match results.
     * <p>
     * This method evaluates the pattern lazily as the stream is consumed, making it ideal
     * for processing massive text inputs without triggering OutOfMemory errors.
     *
     * @param input The character sequence to search within.
     * @return A sequential {@link Stream} of matched substrings.
     */
    Stream<String> streamMatches(CharSequence input);

    /**
     * Retrieves the raw regular expression string that was compiled by the engine.
     * <p>
     * Useful for logging, debugging, or auditing purposes.
     *
     * @return The raw regular expression string.
     */
    String getRawRegex();

    /**
     * Releases any thread-local, stateful, or native resources associated with this compiled pattern.
     * <p>
     * <b>Implementation Note:</b> The default implementation is a no-op, which is strictly
     * intended for stateless engines (e.g., standard JDK {@code java.util.regex.Pattern} or RE2J)
     * that rely purely on the Garbage Collector. Engines that allocate native memory or
     * thread-isolated contexts (e.g., GraalVM) must override this method to prevent memory leaks.
     */
    @Override
    default void close() {
        // No-op by default
    }
}