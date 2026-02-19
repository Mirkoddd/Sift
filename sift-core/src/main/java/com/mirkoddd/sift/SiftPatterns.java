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

import com.mirkoddd.sift.dsl.SiftPattern;
import com.mirkoddd.sift.internal.RegexSyntax;

/**
 * <h2>SiftPatterns - Component Factory</h2>
 * Static utilities to create complex patterns (groups, logic, literals).
 * <p>
 * <b>Usage Recommendation:</b> Statically import methods from this class to keep your code readable.
 * <pre>
 *  {@code import static com.mirkoddd.sift.SiftPatterns.*;}
 *  {@code .followedBy(anyOf(literal("A"), literal("B")))}
 * </pre>
 */
public final class SiftPatterns {

    private SiftPatterns() {}

    /**
     * Creates a pattern that matches ANY ONE of the provided options (Logical OR).
     * <p>
     * This generates a non-capturing group: {@code (?:pattern1|pattern2|...)}.
     * Useful for matching specific sets of words or sub-patterns.
     *
     * @param options The alternative patterns to match.
     * @return A composable {@link SiftPattern} representing the logic OR.
     */
    public static SiftPattern anyOf(SiftPattern... options) {
        return () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);
            for (int i = 0; i < options.length; i++) {
                sb.append(options[i].shake());
                if (i < options.length - 1) {
                    sb.append(RegexSyntax.OR);
                }
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
        return () -> RegexSyntax.GROUP_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE;
    }

    /**
     * Wraps a pattern in a <b>Named Capturing Group</b> {@code (?<name>...)}.
     * <p>
     * Named groups allow you to extract data by name instead of index, making the
     * extraction code more readable (e.g., {@code Matcher.group("userId")}).
     *
     * @param groupName The unique name for this group (must differ from other group names).
     * @param pattern   The pattern to capture.
     * @return A SiftPattern wrapped in a named group.
     */
    public static SiftPattern capture(String groupName, SiftPattern pattern) {
        return () -> RegexSyntax.NAMED_GROUP_OPEN +
                groupName + RegexSyntax.NAMED_GROUP_NAME_CLOSE +
                pattern.shake() + RegexSyntax.GROUP_CLOSE;
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
        return () -> {
            StringBuilder sb = new StringBuilder();
            RegexEscaper.escapeString(text, sb);
            return sb.toString();
        };
    }

}