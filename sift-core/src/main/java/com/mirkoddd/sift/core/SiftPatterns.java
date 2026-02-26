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

import java.util.Objects;

/**
 * <h2>SiftPatterns - Component Factory</h2>
 * Static utilities to create complex patterns (groups, logic, literals).
 * <p>
 * <b>Usage Recommendation:</b> Statically import methods from this class to keep your code readable.
 * <pre>
 * {@code import static com.mirkoddd.sift.core.SiftPatterns.*;}
 * {@code .followedBy(anyOf(literal("A"), literal("B")))}
 * </pre>
 */
public final class SiftPatterns {

    private SiftPatterns() {
    }

    /**
     * Creates a pattern that matches ANY ONE of the provided options (Logical OR).
     * <p>
     * This generates a non-capturing group: {@code (?:pattern1|pattern2|...)}.
     * Requires at least two options to form a valid logical OR expression.
     *
     * @param option1           The first mandatory alternative.
     * @param option2           The second mandatory alternative.
     * @param additionalOptions Any further alternative patterns.
     * @return A composable {@link SiftPattern} representing the logical OR.
     */
    public static SiftPattern anyOf(SiftPattern option1, SiftPattern option2, SiftPattern... additionalOptions) {
        Objects.requireNonNull(option1, "First option cannot be null");
        Objects.requireNonNull(option2, "Second option cannot be null");
        Objects.requireNonNull(additionalOptions, "Additional options array cannot be null");

        for (SiftPattern opt : additionalOptions) {
            Objects.requireNonNull(opt, "Additional option cannot be null");
        }

        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);

            sb.append(option1.shake());
            sb.append(RegexSyntax.OR);
            sb.append(option2.shake());

            for (SiftPattern opt : additionalOptions) {
                sb.append(RegexSyntax.OR);
                sb.append(opt.shake());
            }

            sb.append(RegexSyntax.GROUP_CLOSE);
            return sb.toString();
        };
    }

    /**
     * Wraps a pattern in a <b>Capturing Group</b> {@code (...)}.
     * <p>
     * Capturing groups allow you to extract specific parts of the matched string
     * using {@code Matcher.group(int)}.
     *
     * @param pattern The pattern to capture.
     * @return A SiftPattern wrapped in parentheses.
     */
    public static SiftPattern capture(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "Pattern to capture cannot be null");
        return () -> RegexSyntax.GROUP_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE;
    }

    /**
     * Creates a <b>Positive Lookahead</b> {@code (?=...)}.
     * <p>
     * Asserts that the given pattern CAN be matched next, but does not consume any characters.
     * <br><b>Example:</b>
     * <pre>{@code
     * // Match "test" only if followed by "123", without consuming "123"
     * String regex = Sift.fromAnywhere()
     *         .pattern(literal("test"))
     *         .followedBy(positiveLookahead(literal("123")))
     *         .shake();
     * // Result: test(?=123)
     * // "test123" matches "test" at position 0-4 (doesn't consume "123")
     * }</pre>
     *
     * @param pattern The pattern that must follow.
     * @return A SiftPattern representing the positive lookahead.
     */
    public static SiftPattern positiveLookahead(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return () -> RegexSyntax.POSITIVE_LOOKAHEAD_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE;
    }

    /**
     * Creates a <b>Negative Lookahead</b> {@code (?!...)}.
     * <p>
     * Asserts that the given pattern CANNOT be matched next, and does not consume any characters.
     * <br><b>Example:</b>
     * <pre>{@code
     * // Match "test" only if NOT followed by "123"
     * String regex = Sift.fromAnywhere()
     *         .pattern(literal("test"))
     *         .followedBy(negativeLookahead(literal("123")))
     *         .shake();
     * // Result: test(?!123)
     * // "test999" matches "test", but "test123" fails entirely
     * }</pre>
     *
     * @param pattern The pattern that must not follow.
     * @return A SiftPattern representing the negative lookahead.
     */
    public static SiftPattern negativeLookahead(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return () -> RegexSyntax.NEGATIVE_LOOKAHEAD_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE;
    }

    /**
     * Creates a <b>Positive Lookbehind</b> {@code (?<=...)}.
     * <p>
     * Asserts that the given pattern CAN be matched immediately before the current position.
     * <br><b>Example:</b>
     * <pre>{@code
     * // Match "123" only if immediately preceded by "EUR"
     * String regex = Sift.fromAnywhere()
     *         .pattern(positiveLookbehind(literal("EUR")))
     *         .followedBy(literal("123"))
     *         .shake();
     * // Result: (?<=EUR)123
     * // Matches "123" in "EUR123", but fails in "USD123"
     * }</pre>
     *
     * @param pattern The pattern that must precede.
     * @return A SiftPattern representing the positive lookbehind.
     */
    public static SiftPattern positiveLookbehind(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return () -> RegexSyntax.POSITIVE_LOOKBEHIND_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE;
    }

    /**
     * Creates a <b>Negative Lookbehind</b> {@code (?<!...)}.
     * <p>
     * Asserts that the given pattern CANNOT be matched immediately before the current position.
     * <br><b>Example:</b>
     * <pre>{@code
     * // Match "123" only if NOT preceded by "EUR"
     * String regex = Sift.fromAnywhere()
     *         .pattern(negativeLookbehind(literal("EUR")))
     *         .followedBy(literal("123"))
     *         .shake();
     * // Result: (?<!EUR)123
     * // Matches "123" in "USD123", but fails in "EUR123"
     * }</pre>
     *
     * @param pattern The pattern that must not precede.
     * @return A SiftPattern representing the negative lookbehind.
     */
    public static SiftPattern negativeLookbehind(SiftPattern pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return () -> RegexSyntax.NEGATIVE_LOOKBEHIND_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE;
    }

    /**
     * Defines a <b>Named Capturing Group</b> {@code (?<name>...)}.
     * <p>
     * This method returns a definition that must be passed to
     * {@code .namedCapture(NamedCapture)} in the builder.
     *
     * @param groupName The unique name for this group. Must start with a letter and contain only alphanumeric characters.
     * @param pattern   The pattern to capture within this group.
     * @return A NamedCapture definition.
     * @throws IllegalArgumentException if {@code groupName} is null, empty, starts with a digit, or contains non-alphanumeric characters (e.g., spaces, underscores, or symbols).
     */
    public static NamedCapture capture(String groupName, SiftPattern pattern) {
        Objects.requireNonNull(pattern, "Pattern to capture cannot be null");
        GroupName validatedName = GroupName.of(groupName);
        return new NamedCapture(validatedName, pattern);
    }

    /**
     * Combines multiple patterns into a single Non-Capturing Group {@code (?:...)}.
     * <p>
     * By requiring at least one mandatory pattern, this method prevents the creation
     * of empty groups at compile-time.
     *
     * @param first The first required pattern.
     * @param then  Optional additional patterns to include in the same group.
     * @return A SiftPattern representing the concatenated non-capturing group.
     */
    public static SiftPattern group(SiftPattern first, SiftPattern... then) {
        Objects.requireNonNull(first, "First pattern in group cannot be null");
        Objects.requireNonNull(then, "Additional patterns array cannot be null");

        for (SiftPattern opt : then) {
            Objects.requireNonNull(opt, "Additional option cannot be null");
        }

        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);

            sb.append(first.shake());

            for (SiftPattern p : then) {
                sb.append(p.shake());
            }

            sb.append(RegexSyntax.GROUP_CLOSE);
            return sb.toString();
        };
    }

    /**
     * Creates a literal pattern, automatically escaping special regex characters.
     * <p>
     * This ensures that characters like {@code .}, {@code *}, {@code +}, or {@code ?} are treated
     * as text, not as regex operators.
     *
     * @param text The literal text to match (e.g., "12.50").
     * @return A safe literal pattern (e.g., "12\.50").
     */
    public static SiftPattern literal(String text) {
        Objects.requireNonNull(text, "Literal text cannot be null");

        if (text.isEmpty()) {
            throw new IllegalArgumentException("Literal text cannot be empty. Use zero-width assertions if intentional.");
        }

        return () -> {
            StringBuilder sb = new StringBuilder();
            RegexEscaper.escapeString(text, sb);
            return sb.toString();
        };
    }

    /**
     * Creates a <b>Negated Character Class</b> {@code [^...]}.
     * <p>
     * Matches any single character that is <b>not</b> present in the provided string.
     * Special characters are automatically safely escaped to ensure they are treated
     * as literals within the regex brackets.
     *
     * @param chars A string containing the exact characters to exclude (e.g., "aeiou" to exclude vowels).
     * @return A SiftPattern representing the negated character set.
     */
    public static SiftPattern anythingBut(String chars) {
        Objects.requireNonNull(chars, "Excluded characters string cannot be null");

        if (chars.isEmpty()) {
            throw new IllegalArgumentException("Excluded characters string cannot be empty");
        }

        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.CLASS_OPEN);
            sb.append(RegexSyntax.NEGATION);

            for (char c : chars.toCharArray()) {
                RegexEscaper.escapeInsideBrackets(c, sb);
            }

            sb.append(RegexSyntax.CLASS_CLOSE);
            return sb.toString();
        };
    }
}