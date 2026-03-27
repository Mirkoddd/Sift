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

import com.mirkoddd.sift.core.SiftGlobalFlag;
import com.mirkoddd.sift.core.SiftPatterns;

/**
 * The continuation node in the Sift State-Machine DSL.
 * <p>
 * This interface represents the point in the fluent chain where a pattern component
 * has been successfully defined. From here, the developer can either chain a new
 * component, append a specific pattern, or terminate the regex sequence.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
public interface Connector<Ctx extends SiftContext> extends SiftPattern<Ctx> {

    /**
     * Transitions the builder state to define the quantifier for the <b>next</b> sequence element.
     * <p>
     * <b>Example:</b>
     * <pre>
     * .exactly(3).digits()
     * .then() // Moves to the next component setup
     * .oneOrMore().letters()
     * </pre>
     *
     * @return A {@link Quantifier} to configure the repetition of the upcoming token.
     */
    Quantifier<Ctx> then();

    /**
     * Safely appends a single literal character to the current sequence.
     * The character is automatically escaped to prevent regex injection.
     *
     * @param c The literal character to append.
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> followedBy(char c);

    /**
     * Appends an existing, pre-compiled SiftPattern to the current sequence.
     * <p>
     * <b>Type Safety:</b> This method strictly requires a {@code SiftContext.Fragment}.
     * Attempting to append an anchored {@code Root} pattern here will cause a compile-time
     * error, preventing logical impossibilities (like placing a start-of-line anchor
     * in the middle of a string).
     *
     * @param p1 The fragment pattern to append.
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> followedBy(SiftPattern<Fragment> p1);

    /**
     * Appends a zero-width assertion (like a lookahead) to the current sequence.
     * <p>
     * Assertions do not consume characters and cannot be quantified.
     *
     * @param assertion The assertion pattern to append.
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> followedByAssertion(SiftPattern<Assertion> assertion);

    /**
     * Convenience overload for composing two patterns sequentially.
     *
     * @param p1 The first fragment pattern to append.
     * @param p2 The second fragment pattern to append.
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> followedBy(SiftPattern<Fragment> p1, SiftPattern<Fragment> p2);

    /**
     * Appends a collection of patterns sequentially.
     * <p>
     * This method provides a clean way to compose dynamically generated lists of patterns.
     * It completely bypasses Java's generic varargs array creation warnings (Heap Pollution)
     * while maintaining full type safety.
     *
     * @param patterns An iterable of fragment patterns to be appended in order.
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> followedBy(Iterable<? extends SiftPattern<Fragment>> patterns);

    /**
     * Asserts that the current sequence is <b>not</b> followed by the given pattern.
     * <p>
     * This uses a Negative Lookahead {@code (?!...)} under the hood. It checks the upcoming
     * characters to ensure the pattern does not match, but it <b>does not consume</b> them.
     *
     * @param pattern The condition that must NOT be met ahead.
     * @return The current connector step for further chaining.
     */
    default Connector<Ctx> notFollowedBy(SiftPattern<Fragment> pattern) {
        return this.followedByAssertion(SiftPatterns.negativeLookahead(pattern));
    }

    /**
     * Asserts that the current sequence <b>must</b> be followed by the given pattern.
     * <p>
     * This uses a Positive Lookahead {@code (?=...)} under the hood. It checks the upcoming
     * characters to ensure the pattern matches, but it <b>does not consume</b> them.
     *
     * @param pattern The condition that must be met ahead.
     * @return The current connector step for further chaining.
     */
    default Connector<Ctx> mustBeFollowedBy(SiftPattern<Fragment> pattern) {
        return this.followedByAssertion(SiftPatterns.positiveLookahead(pattern));
    }

    /**
     * Prepends an existing, pre-compiled SiftPattern to the current sequence.
     * <p>
     * This method structurally inserts the given pattern <b>before</b> the current sequence,
     * maintaining the existing generic context.
     *
     * @param p1 The fragment pattern to prepend.
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> precededBy(SiftPattern<Fragment> p1);

    /**
     * Prepends a zero-width assertion (like a lookbehind) to the current sequence.
     * <p>
     * Assertions do not consume characters and cannot be quantified.
     *
     * @param assertion The assertion pattern to prepend.
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> precededByAssertion(SiftPattern<Assertion> assertion);

    /**
     * Asserts that the current sequence is <b>not</b> preceded by the given pattern.
     * <p>
     * This uses a Negative Lookbehind {@code (?<!...)} under the hood. It checks the preceding
     * characters to ensure the pattern does not match, but it <b>does not consume</b> them.
     *
     * @param pattern The condition that must NOT be met behind.
     * @return A new connector with the lookbehind applied before the current sequence.
     */
    default Connector<Ctx> notPrecededBy(SiftPattern<Fragment> pattern) {
        return this.precededByAssertion(SiftPatterns.negativeLookbehind(pattern));
    }

    /**
     * Asserts that the current sequence <b>must</b> be preceded by the given pattern.
     * <p>
     * This uses a Positive Lookbehind {@code (?<=...)} under the hood. It checks the preceding
     * characters to ensure the pattern matches, but it <b>does not consume</b> them.
     *
     * @param pattern The condition that must be met behind.
     * @return A new connector with the lookbehind applied before the current sequence.
     */
    default Connector<Ctx> mustBePrecededBy(SiftPattern<Fragment> pattern) {
        return this.precededByAssertion(SiftPatterns.positiveLookbehind(pattern));
    }

    /**
     * Appends a Word Boundary {@code \b} to the sequence.
     * <p>
     * A word boundary matches the position where a word character is not followed or
     * preceded by another word-character, ensuring exact word matches.
     *
     * @return The current connector step for further chaining.
     */
    Connector<Ctx> wordBoundary();

    /**
     * Finalizes the regex chain by appending an end-of-line anchor {@code $}.
     * <p>
     * <b>State Mutation:</b> Calling this method fundamentally changes the structural
     * context of the pattern. It forces the return type to {@code SiftPattern<SiftContext.Root>},
     * effectively sealing the regex and making it illegal to embed this pattern inside
     * any other Sift sequence.
     *
     * @return A sealed, anchored Root pattern ready for compilation.
     */
    SiftPattern<Root> andNothingElse();

    /**
     * Finalizes the regex chain by appending {@code \z} — the absolute end of the string.
     * <p>
     * Unlike {@link #andNothingElse()} which appends {@code $}, this anchor is never
     * affected by the {@link SiftGlobalFlag#MULTILINE} flag and never matches before
     * a trailing newline. Use this when you need strict end-of-string semantics
     * regardless of flags or input content.
     *
     * @return A sealed, anchored Root pattern ready for compilation.
     */
    SiftPattern<Root> andNothingElseAbsolutely();

    /**
     * Finalizes the regex chain by appending {@code \Z} — end of string, or just
     * before a trailing newline.
     * <p>
     * Matches at the very end of the string, or at the position immediately before
     * a final line terminator. Unlike {@link #andNothingElse()} ({@code $}), it is
     * not affected by {@link SiftGlobalFlag#MULTILINE} and will not match at internal
     * line breaks.
     *
     * @return A sealed, anchored Root pattern ready for compilation.
     */
    SiftPattern<Root> andNothingElseBeforeFinalNewline();
}