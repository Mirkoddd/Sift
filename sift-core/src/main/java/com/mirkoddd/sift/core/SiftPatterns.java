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

import com.mirkoddd.sift.core.dsl.Assertion;
import com.mirkoddd.sift.core.dsl.ConditionalThen;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.RegexFeature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
    public static SiftPattern<Fragment> anyOf(
            SiftPattern<Fragment> option1,
            SiftPattern<Fragment> option2,
            SiftPattern<Fragment>... additionalOptions) {

        Objects.requireNonNull(option1, "First option cannot be null");
        Objects.requireNonNull(option2, "Second option cannot be null");
        Objects.requireNonNull(additionalOptions, "Additional options array cannot be null");

        for (SiftPattern<Fragment> opt : additionalOptions) {
            Objects.requireNonNull(opt, "Additional option cannot be null");
        }

        SiftPattern<?>[] allOptions = new SiftPattern<?>[2 + additionalOptions.length];
        allOptions[0] = option1;
        allOptions[1] = option2;
        System.arraycopy(additionalOptions, 0, allOptions, 2, additionalOptions.length);

        return memoize(
                () -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);

                    sb.append(option1.shake());
                    sb.append(RegexSyntax.OR);
                    sb.append(option2.shake());

                    for (SiftPattern<Fragment> opt : additionalOptions) {
                        sb.append(RegexSyntax.OR);
                        sb.append(opt.shake());
                    }

                    sb.append(RegexSyntax.GROUP_CLOSE);
                    return sb.toString();
                },
                allOptions
        );
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
    public static SiftPattern<Fragment> anyOf(java.util.List<? extends SiftPattern<Fragment>> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            throw new IllegalArgumentException("anyOf() requires at least one pattern in the list.");
        }
        if (patterns.size() == 1) {
            return patterns.get(0);
        }

        SiftPattern<?>[] allOptions = patterns.toArray(new SiftPattern<?>[0]);

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
        }, allOptions);
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
    public static SiftPattern<Fragment> capture(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Pattern to capture cannot be null");
        return memoize(
                () -> RegexSyntax.GROUP_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE,
                pattern
        );
    }

    // --- LOOKAROUND ASSERTIONS ---

    /**
     * Creates a <b>Positive Lookahead</b> assertion: {@code (?=pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>immediately follows</i>
     * the current position, but does <b>not</b> consume any characters.
     *
     * @param pattern The condition that must be met ahead.
     * @return A lookahead assertion.
     */
    public static SiftPattern<Assertion> positiveLookahead(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return memoize(
                () -> RegexSyntax.POSITIVE_LOOKAHEAD_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE,
                RegexFeature.LOOKAHEAD,
                pattern
        );
    }

    /**
     * Creates a <b>Negative Lookahead</b> assertion: {@code (?!pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>does not follow</i>
     * the current position.
     *
     * @param pattern The condition that must NOT be met ahead.
     * @return A negative lookahead assertion.
     */
    public static SiftPattern<Assertion> negativeLookahead(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return memoize(
                () -> RegexSyntax.NEGATIVE_LOOKAHEAD_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE,
                RegexFeature.LOOKAHEAD,
                pattern
        );
    }

    /**
     * Creates a <b>Positive Lookbehind</b> assertion: {@code (?<=pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>immediately precedes</i>
     * the current position.
     *
     * @param pattern The condition that must be met behind.
     * @return A lookbehind assertion.
     */
    public static SiftPattern<Assertion> positiveLookbehind(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return memoize(
                () -> RegexSyntax.POSITIVE_LOOKBEHIND_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE,
                RegexFeature.LOOKBEHIND,
                pattern
        );
    }

    /**
     * Creates a <b>Negative Lookbehind</b> assertion: {@code (?<!pattern)}.
     * <p>
     * This is a zero-width assertion. It checks that the given pattern <i>does not precede</i>
     * the current position.
     *
     * @param pattern The condition that must NOT be met behind.
     * @return A negative lookbehind assertion.
     */
    public static SiftPattern<Assertion> negativeLookbehind(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return memoize(
                () -> RegexSyntax.NEGATIVE_LOOKBEHIND_OPEN + pattern.shake() + RegexSyntax.GROUP_CLOSE,
                RegexFeature.LOOKBEHIND,
                pattern
        );
    }

    /**
     * Starts a Type-Safe <b>Conditional Regex Block</b> based on a positive lookahead condition.
     * <p>
     * Emulates native conditional evaluation {@code (?(cond)true|false)} for Java Regex.
     * If the specified pattern follows the current cursor position, the engine consumes the
     * {@code Then} branch. Otherwise, it defaults to the {@code Else} branch.
     *
     * @param condition The pattern that must immediately follow to trigger the TRUE branch.
     * @return The next builder state, strictly requiring the definition of the {@code Then} branch.
     */
    public static ConditionalThen ifFollowedBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(positiveLookahead(condition), negativeLookahead(condition));
    }

    /**
     * Starts a Type-Safe <b>Conditional Regex Block</b> based on a negative lookahead condition.
     *
     * @param condition The pattern that must NOT follow to trigger the TRUE branch.
     * @return The next builder state, strictly requiring the definition of the {@code Then} branch.
     */
    public static ConditionalThen ifNotFollowedBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(negativeLookahead(condition), positiveLookahead(condition));
    }

    /**
     * Starts a Type-Safe <b>Conditional Regex Block</b> based on a positive lookbehind condition.
     *
     * @param condition The pattern that must immediately precede to trigger the TRUE branch.
     * @return The next builder state, strictly requiring the definition of the {@code Then} branch.
     */
    public static ConditionalThen ifPrecededBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(positiveLookbehind(condition), negativeLookbehind(condition));
    }

    /**
     * Starts a Type-Safe <b>Conditional Regex Block</b> based on a negative lookbehind condition.
     *
     * @param condition The pattern that must NOT precede to trigger the TRUE branch.
     * @return The next builder state, strictly requiring the definition of the {@code Then} branch.
     */
    public static ConditionalThen ifNotPrecededBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(negativeLookbehind(condition), positiveLookbehind(condition));
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
    public static NamedCapture capture(String groupName, SiftPattern<Fragment> pattern) {
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
    public static SiftPattern<Fragment> group(
            SiftPattern<Fragment> first,
            SiftPattern<Fragment>... then) {

        Objects.requireNonNull(first, "First pattern in group cannot be null");
        Objects.requireNonNull(then, "Additional patterns array cannot be null");

        for (SiftPattern<Fragment> opt : then) {
            Objects.requireNonNull(opt, "Additional option cannot be null");
        }

        SiftPattern<?>[] allNodes = new SiftPattern<?>[1 + then.length];
        allNodes[0] = first;
        System.arraycopy(then, 0, allNodes, 1, then.length);

        return memoize(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);

            sb.append(first.shake());

            for (SiftPattern<Fragment> p : then) {
                sb.append(p.shake());
            }

            sb.append(RegexSyntax.GROUP_CLOSE);
            return sb.toString();
        }, allNodes);
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
    public static SiftPattern<Fragment> literal(String text) {
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
    public static SiftPattern<Fragment> anythingBut(String chars) {
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
     * Initiates the creation of a safely nested recursive pattern.
     * <p>
     * This factory method returns a fluent {@link NestingAssembler} that guarantees
     * structural symmetry by forcing the use of a valid {@link Delimiter} pair.
     * </p>
     *
     * @param depth The maximum nesting depth to unroll (must be between 2 and 10).
     * @return A {@link NestingAssembler} to configure the delimiter and content.
     */
    public static NestingAssembler nesting(int depth) {
        return new NestingAssembler(depth);
    }

    /**
     * Internal unrolling engine that emulates Regular Expression recursion.
     * <p>
     * Since Java's native {@link java.util.regex.Pattern} lacks support for true
     * PCRE-style recursion (like {@code (?R)}), this method achieves a similar result
     * by functionally unrolling the pattern definition from the inside out.
     * </p>
     * <p>
     * <b>JVM Safety Bounds:</b> The depth is strictly clamped between 2 and 10.
     * Compiling deeply nested regex strings grows exponentially and will quickly trigger
     * a {@code StackOverflowError} within the JVM's regex compiler. A depth of 10 is
     * mathematically vast for real-world structured data.
     * </p>
     *
     * @param maxDepth   The maximum number of nesting levels to unroll (must be between 2 and 10).
     * @param definition A functional block where the parameter represents the recursive
     * call to the pattern itself (the 'self' reference).
     * @return A deeply nested fragment capable of parsing recursive structures up to {@code maxDepth}.
     * @throws IllegalArgumentException if {@code maxDepth} is outside the safe bounds [2, 10].
     * @throws NullPointerException     if the {@code definition} function is null.
     */
    static SiftPattern<Fragment> recursive(
            int maxDepth,
            Function<SiftPattern<Fragment>, SiftPattern<Fragment>> definition) {

        if (maxDepth < 2 || maxDepth > 10) {
            throw new IllegalArgumentException(
                    "Recursion depth must be strictly between 2 and 10 to ensure JVM stability and prevent StackOverflowErrors."
            );
        }
        Objects.requireNonNull(definition, "Recursive definition cannot be null.");

        // BASE CASE (The bottom of the Matryoshka):
        // If the parsing engine attempts to go deeper than maxDepth, it hits this empty negative lookahead.
        // In Regex mathematics, (?!) is a logical contradiction that is guaranteed to ALWAYS fail.
        // This ensures the regex stops safely instead of breaking unexpectedly.
        SiftPattern<Fragment> current = memoize(() -> "(?!)");

        // UNROLLING (Inside-Out Injection):
        // We build the nested structure by applying the function repeatedly.
        // At each iteration, the previously built layer is injected as the 'self' parameter
        // for the next layer up, effectively unrolling the recursion.
        for (int i = 0; i < maxDepth; i++) {
            current = definition.apply(current);
        }

        return memoize(current::shake, RegexFeature.RECURSION, current);
    }

    /**
     * Applies specific regex flags ONLY to the provided pattern fragment.
     * <p>
     * Unlike global flags, these <b>Local Flags</b> do not affect the rest of the regex.
     * Internally, it wraps the pattern in a special flag group: {@code (?flags:pattern)}.
     * <p>
     * Example:
     * <pre>
     * {@code
     * SiftPattern<Fragment> partial = withFlags(literal("abc"), SiftGlobalFlag.CASE_INSENSITIVE);
     * // Compiles to: (?i:abc)
     * }
     * </pre>
     *
     * @param pattern The fragment to apply the flags to.
     * @param flags   The flags to enable for this fragment.
     * @return A fragment with local flags applied.
     */
    public static SiftPattern<Fragment> withFlags(
            SiftPattern<Fragment> pattern,
            SiftGlobalFlag... flags) {

        Objects.requireNonNull(pattern, "Pattern cannot be null");
        Objects.requireNonNull(flags, "Flags cannot be null");

        return memoize(
                () -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(RegexSyntax.INLINE_FLAG_OPEN);
                    for (SiftGlobalFlag flag : flags) {
                        sb.append(flag.getSymbol());
                    }
                    sb.append(RegexSyntax.INLINE_FLAG_SEPARATOR);
                    sb.append(pattern.shake());
                    sb.append(RegexSyntax.GROUP_CLOSE);
                    return sb.toString();
                },
                RegexFeature.INLINE_FLAGS,
                pattern
        );
    }

    /**
     * Memoize a pattern generation using a supplier.
     * This overload is intended for simple patterns that do not introduce new
     * advanced regex features (e.g., literals, simple groups).
     *
     * @param <T>       The structural context of the pattern.
     * @param generator A supplier providing the raw regex string.
     * @return A memoized SiftPattern.
     */
    static <T extends SiftContext> SiftPattern<T> memoize(Supplier<String> generator) {
        return new MemoizedPattern<>(generator, null);
    }

    /**
     * Memoize a pattern generation while propagating advanced regex features
     * from nested patterns, without introducing a new specific feature itself.
     * This is intended for structural containers like anyOf(), group(), or capture().
     *
     * @param <T>       The structural context of the pattern.
     * @param generator A supplier providing the raw regex string.
     * @param inners    An array of inner patterns to extract and propagate existing features from.
     */
    static <T extends SiftContext> SiftPattern<T> memoize(Supplier<String> generator, SiftPattern<?>... inners) {
        return new MemoizedPattern<>(generator, null, inners);
    }

    /**
     * Memoize a pattern generation while tracking its advanced regex features and
     * enabling feature propagation from multiple nested patterns.
     *
     * @param <T>       The structural context of the pattern.
     * @param generator A supplier providing the raw regex string.
     * @param feature   The advanced feature introduced by this specific node.
     * @param inners    An array of inner patterns to extract and propagate existing features from.
     */
    static <T extends SiftContext> SiftPattern<T> memoize(Supplier<String> generator, RegexFeature feature, SiftPattern<?>... inners) {
        return new MemoizedPattern<>(generator, EnumSet.of(feature), inners);
    }

    private static final class MemoizedPattern<T extends SiftContext> extends BaseSiftPattern<T> {
        private final Supplier<String> generator;
        private final Set<RegexFeature> features;
        private final SiftPattern<?>[] inners;

        private MemoizedPattern(Supplier<String> generator, Set<RegexFeature> features, SiftPattern<?>... inners) {
            this.generator = generator;
            this.features = features != null ? features : Collections.emptySet();
            this.inners = inners;
        }

        @Override
        protected String buildRegex() {
            return generator.get();
        }

        @Override
        protected Set<RegexFeature> buildFeatures() {
            Set<RegexFeature> totalFeatures = EnumSet.noneOf(RegexFeature.class);
            totalFeatures.addAll(features);

            if (inners != null) {
                for (SiftPattern<?> inner : inners) {
                    if (inner instanceof PatternMetadata) {
                        totalFeatures.addAll(((PatternMetadata) inner).getInternalUsedFeatures());
                    }
                }
            }

            if (totalFeatures.isEmpty()) {
                return Collections.emptySet();
            }

            return Collections.unmodifiableSet(totalFeatures);
        }
    }
}