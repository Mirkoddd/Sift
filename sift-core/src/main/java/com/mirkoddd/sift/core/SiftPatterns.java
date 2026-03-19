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
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.RegexFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * <h2>SiftPatterns - Component Factory</h2>
 * Static utilities to create complex patterns (groups, logic, literals).
 * <p>
 * <b>Usage Recommendation:</b> Statically import methods from this class to keep your code readable.
 * <pre>
 * {@code import static com.mirkoddd.sift.core.SiftPatterns.*;}
 * {@code .followedBy(anyOf(literal("A"), literal("B")))}
 * </pre>
 * <p>
 * <b>AST Architecture:</b> Every method in this class acts as a factory for an isolated,
 * immutable AST branch. These nodes are lazily evaluated only when attached to a main chain
 * and compiled via {@code shake()}.
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

        List<SiftPattern<?>> allOptions = new ArrayList<>();
        allOptions.add(option1);
        allOptions.add(option2);
        allOptions.addAll(Arrays.asList(additionalOptions));

        return new SiftConnector<>(null, visitor -> visitor.visitAnyOf(allOptions));
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
    public static SiftPattern<Fragment> anyOf(List<? extends SiftPattern<Fragment>> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            throw new IllegalArgumentException("anyOf() requires at least one pattern in the list.");
        }
        if (patterns.size() == 1) {
            return patterns.get(0);
        }

        return new SiftConnector<>(null, visitor -> visitor.visitAnyOf(patterns));
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
        return new SiftConnector<>(null, visitor -> visitor.visitCaptureGroup(pattern));
    }

    // --- LOOKAROUND ASSERTIONS ---

    public static SiftPattern<Assertion> positiveLookahead(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return new SiftConnector<>(null, visitor -> visitor.visitLookaround(pattern, true, true));
    }

    public static SiftPattern<Assertion> negativeLookahead(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookahead pattern cannot be null");
        return new SiftConnector<>(null, visitor -> visitor.visitLookaround(pattern, false, true));
    }

    public static SiftPattern<Assertion> positiveLookbehind(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return new SiftConnector<>(null, visitor -> visitor.visitLookaround(pattern, true, false));
    }

    public static SiftPattern<Assertion> negativeLookbehind(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Lookbehind pattern cannot be null");
        return new SiftConnector<>(null, visitor -> visitor.visitLookaround(pattern, false, false));
    }

    public static ConditionalThen ifFollowedBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(positiveLookahead(condition), negativeLookahead(condition));
    }

    public static ConditionalThen ifNotFollowedBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(negativeLookahead(condition), positiveLookahead(condition));
    }

    public static ConditionalThen ifPrecededBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(positiveLookbehind(condition), negativeLookbehind(condition));
    }

    public static ConditionalThen ifNotPrecededBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Condition pattern cannot be null");
        return new ConditionalAssembler(negativeLookbehind(condition), positiveLookbehind(condition));
    }

    public static NamedCapture capture(String groupName, SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Pattern to capture cannot be null");
        GroupName validatedName = GroupName.of(groupName);
        return new NamedCapture(validatedName, pattern);
    }

    @SafeVarargs
    public static SiftPattern<Fragment> group(
            SiftPattern<Fragment> first,
            SiftPattern<Fragment>... then) {

        Objects.requireNonNull(first, "First pattern in group cannot be null");
        Objects.requireNonNull(then, "Additional patterns array cannot be null");

        for (SiftPattern<Fragment> opt : then) {
            Objects.requireNonNull(opt, "Additional option cannot be null");
        }

        List<SiftPattern<?>> allNodes = new ArrayList<>();
        allNodes.add(first);
        allNodes.addAll(Arrays.asList(then));

        return new SiftConnector<>(null, visitor -> visitor.visitNonCapturingGroup(allNodes));
    }

    public static SiftPattern<Fragment> literal(String text) {
        Objects.requireNonNull(text, "Literal text cannot be null");

        if (text.isEmpty()) {
            throw new IllegalArgumentException("Literal text cannot be empty. Use zero-width assertions if intentional.");
        }

        return new SiftConnector<>(null, visitor -> {
            StringBuilder sb = new StringBuilder();
            RegexEscaper.escapeString(text, sb);
            visitor.visitAnchor(sb.toString());
        });
    }

    public static SiftPattern<Fragment> anythingBut(String chars) {
        Objects.requireNonNull(chars, "Excluded characters string cannot be null");

        if (chars.isEmpty()) {
            throw new IllegalArgumentException("Excluded characters string cannot be empty");
        }

        return new SiftConnector<>(null, visitor -> {
            StringBuilder sb = new StringBuilder(3 + (chars.length() * 2));
            sb.append(RegexSyntax.NEGATION);
            for (int i = 0; i < chars.length(); i++) {
                RegexEscaper.escapeInsideBrackets(chars.charAt(i), sb);
            }
            // Treat the custom exclusion logic as a class range
            visitor.visitClassRange(sb.toString());
        });
    }

    public static NestingAssembler nesting(int depth) {
        return new NestingAssembler(depth);
    }

    static SiftPattern<Fragment> recursive(
            int maxDepth,
            Function<SiftPattern<Fragment>, SiftPattern<Fragment>> definition) {

        if (maxDepth < 2 || maxDepth > 10) {
            throw new IllegalArgumentException(
                    "Recursion depth must be strictly between 2 and 10 to ensure JVM stability and prevent StackOverflowErrors."
            );
        }
        Objects.requireNonNull(definition, "Recursive definition cannot be null.");

        SiftPattern<Fragment> current = new SiftConnector<>(null, visitor -> visitor.visitAnchor("(?!)"));

        for (int i = 0; i < maxDepth; i++) {
            current = definition.apply(current);
        }

        SiftPattern<Fragment> finalPattern = current;
        return new SiftConnector<>(null, visitor -> {
            visitor.visitFeature(RegexFeature.RECURSION);
            visitor.visitPattern(finalPattern);
        });
    }

    public static SiftPattern<Fragment> withFlags(
            SiftPattern<Fragment> pattern,
            SiftGlobalFlag... flags) {

        Objects.requireNonNull(pattern, "Pattern cannot be null");
        Objects.requireNonNull(flags, "Flags cannot be null");

        return new SiftConnector<>(null, visitor -> visitor.visitLocalFlags(pattern, flags));
    }
}