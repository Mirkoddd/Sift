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

import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;

import java.util.Objects;
import java.util.function.Supplier;

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
        // Prevents instantiation
    }

    /**
     * Creates an alternation (OR) logic between multiple pattern options.
     * <p>
     * Internally, this wraps the options in a <b>non-capturing group</b> to preserve
     * logical boundaries: {@code (?:Option1|Option2|OptionN)}.
     *
     * @param option1           The first mandatory option.
     * @param option2           The second mandatory option.
     * @param additionalOptions Any extra options to include in the alternation.
     * @return A fragment representing the OR logic.
     */
    @SafeVarargs
    public static SiftPattern<SiftContext.Fragment> anyOf(
            SiftPattern<SiftContext.Fragment> option1,
            SiftPattern<SiftContext.Fragment> option2,
            SiftPattern<SiftContext.Fragment>... additionalOptions) {

        Objects.requireNonNull(option1, "First option cannot be null");
        Objects.requireNonNull(option2, "Second option cannot be null");
        Objects.requireNonNull(additionalOptions, "Additional options array cannot be null");

        for (SiftPattern<SiftContext.Fragment> opt : additionalOptions) {
            Objects.requireNonNull(opt, "Additional option cannot be null");
        }

        return memoize(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);

            sb.append(option1.shake());
            sb.append(RegexSyntax.OR);
            sb.append(option2.shake());

            for (SiftPattern<SiftContext.Fragment> opt : additionalOptions) {
                sb.append(RegexSyntax.OR);
                sb.append(opt.shake());
            }

            sb.append(RegexSyntax.GROUP_CLOSE);
            return sb.toString();
        });
    }

    /**
     * Creates an alternation (OR) logic from a dynamic list of pattern options.
     * <p>
     * Internally wraps the elements in a non-capturing group {@code (?:...|...)}.
     * If the list contains only one element, it returns the element directly without grouping.
     *
     * @param patterns A list of fragment patterns to alternate between.
     * @return A fragment representing the OR logic.
     * @throws IllegalArgumentException if the list is null or empty.
     */
    public static SiftPattern<SiftContext.Fragment> anyOf(java.util.List<? extends SiftPattern<SiftContext.Fragment>> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            throw new IllegalArgumentException("anyOf() requires at least one pattern in the list.");
        }
        if (patterns.size() == 1) {
            return patterns.get(0);
        }

        return memoize(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);
            for (int i = 0; i < patterns.size(); i++) {
                sb.append(patterns.get(i).shake());
                if (i < patterns.size() - 1) {
                    sb.append(RegexSyntax.OR);
                }
            }
            sb.append(RegexSyntax.GROUP_CLOSE);
            return sb.toString();
        });
    }

    /**
     * Wraps a pattern in a standard capturing group: {@code (pattern)}.
     * <p>
     * This allows you to extract the matched text after the regex executes
     * using standard {@code Matcher.group(int)} methods.
     *
     * @param pattern The pattern to isolate and capture.
     * @return A capturing group fragment.
     */
    public static SiftPattern<SiftContext.Fragment> capture(SiftPattern<SiftContext.Fragment> pattern) {
        Objects.requireNonNull(pattern, "Pattern to capture cannot be null");
        return memoize(() -> RegexSyntax.GROUP_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE);
    }

    //

    /**
     * Creates a <b>Positive Lookahead</b> assertion: {@code (?=pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>immediately follows</i>
     * the current position, but does <b>not</b> consume any characters.
     *
     * @param pattern The condition that must be met ahead.
     * @return A lookahead fragment.
     */
    public static SiftPattern<SiftContext.Fragment> positiveLookahead(SiftPattern<SiftContext.Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return memoize(() -> RegexSyntax.POSITIVE_LOOKAHEAD_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE);
    }

    /**
     * Creates a <b>Negative Lookahead</b> assertion: {@code (?!pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>does not follow</i>
     * the current position.
     *
     * @param pattern The condition that must NOT be met ahead.
     * @return A negative lookahead fragment.
     */
    public static SiftPattern<SiftContext.Fragment> negativeLookahead(SiftPattern<SiftContext.Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return memoize(() -> RegexSyntax.NEGATIVE_LOOKAHEAD_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE);
    }

    /**
     * Creates a <b>Positive Lookbehind</b> assertion: {@code (?<=pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>immediately precedes</i>
     * the current position.
     *
     * @param pattern The condition that must be met behind.
     * @return A lookbehind fragment.
     */
    public static SiftPattern<SiftContext.Fragment> positiveLookbehind(SiftPattern<SiftContext.Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return memoize(() -> RegexSyntax.POSITIVE_LOOKBEHIND_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE);
    }

    /**
     * Creates a <b>Negative Lookbehind</b> assertion: {@code (?<!pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>does not precede</i>
     * the current position.
     *
     * @param pattern The condition that must NOT be met behind.
     * @return A negative lookbehind fragment.
     */
    public static SiftPattern<SiftContext.Fragment> negativeLookbehind(SiftPattern<SiftContext.Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return memoize(() -> RegexSyntax.NEGATIVE_LOOKBEHIND_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE);
    }

    /**
     * Creates a <b>Named Capturing Group</b>: {@code (?<name>pattern)}.
     * <p>
     * Returns a special {@link NamedCapture} object that can be safely used
     * to refer back to this group without hardcoding string names, eliminating typos.
     *
     * @param groupName The alphanumeric name for the group.
     * @param pattern   The pattern to capture.
     * @return A strongly-typed NamedCapture object.
     */
    public static NamedCapture capture(String groupName, SiftPattern<SiftContext.Fragment> pattern) {
        Objects.requireNonNull(pattern, "Pattern to capture cannot be null");
        GroupName validatedName = GroupName.of(groupName);
        return new NamedCapture(validatedName, pattern);
    }

    /**
     * Sequentially groups multiple patterns together <b>without capturing</b> them: {@code (?:P1P2P3)}.
     * <p>
     * Useful when you need to apply a quantifier to an entire sequence of components
     * without cluttering the extraction result with unwanted groups.
     *
     * @param first The first pattern in the sequence.
     * @param then  Additional patterns to append sequentially inside the group.
     * @return A non-capturing group fragment.
     */
    @SafeVarargs
    public static SiftPattern<SiftContext.Fragment> group(
            SiftPattern<SiftContext.Fragment> first,
            SiftPattern<SiftContext.Fragment>... then) {

        Objects.requireNonNull(first, "First pattern in group cannot be null");
        Objects.requireNonNull(then, "Additional patterns array cannot be null");

        for (SiftPattern<SiftContext.Fragment> opt : then) {
            Objects.requireNonNull(opt, "Additional option cannot be null");
        }

        return memoize(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);

            sb.append(first.shake());

            for (SiftPattern<SiftContext.Fragment> p : then) {
                sb.append(p.shake());
            }

            sb.append(RegexSyntax.GROUP_CLOSE);
            return sb.toString();
        });
    }

    /**
     * Matches exact literal text safely.
     * <p>
     * This method automatically escapes any regex metacharacters (like {@code *}, {@code +}, {@code .})
     * present in the text, preventing regex injection attacks or structural breakage.
     *
     * @param text The exact literal string to match.
     * @return A safely escaped pattern fragment.
     * @throws IllegalArgumentException if the text is empty.
     */
    public static SiftPattern<SiftContext.Fragment> literal(String text) {
        Objects.requireNonNull(text, "Literal text cannot be null");

        if (text.isEmpty()) {
            throw new IllegalArgumentException("Literal text cannot be empty. Use zero-width assertions if intentional.");
        }

        return memoize(() -> {
            StringBuilder sb = new StringBuilder();
            RegexEscaper.escapeString(text, sb);
            return sb.toString();
        });
    }

    /**
     * Matches any single character that is <b>NOT</b> in the provided string.
     * <p>
     * Creates a negated character class: {@code [^...]}. Metacharacters inside the
     * brackets are automatically and safely escaped.
     *
     * @param chars A string of characters to exclude.
     * @return A negated character class fragment.
     * @throws IllegalArgumentException if the characters string is empty.
     */
    public static SiftPattern<SiftContext.Fragment> anythingBut(String chars) {
        Objects.requireNonNull(chars, "Excluded characters string cannot be null");

        if (chars.isEmpty()) {
            throw new IllegalArgumentException("Excluded characters string cannot be empty");
        }

        return memoize(() -> {
            StringBuilder sb = new StringBuilder(3 + (chars.length() * 2));
            sb.append(RegexSyntax.CLASS_OPEN);
            sb.append(RegexSyntax.NEGATION);

            for (int i = 0; i < chars.length(); i++) {
                RegexEscaper.escapeInsideBrackets(chars.charAt(i), sb);
            }

            sb.append(RegexSyntax.CLASS_CLOSE);
            return sb.toString();
        });
    }

    /**
     * Wraps a string generator in a memoized pattern to ensure the string building
     * logic is executed only once, optimizing performance.
     */
    private static SiftPattern<SiftContext.Fragment> memoize(Supplier<String> generator) {
        return new MemoizedPattern(generator);
    }

    private static final class MemoizedPattern extends BaseSiftPattern<SiftContext.Fragment> {
        private final Supplier<String> generator;

        private MemoizedPattern(Supplier<String> generator) {
            this.generator = generator;
        }

        @Override
        protected String buildRegex() {
            return generator.get();
        }
    }
}