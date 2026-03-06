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

/**
 * The continuation node in the Sift State-Machine DSL.
 * <p>
 * This interface represents the point in the fluent chain where a pattern component
 * has been successfully defined. From here, the developer can either chain a new
 * component, append a specific pattern, or terminate the regex sequence.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
public interface ConnectorStep<Ctx extends SiftContext> extends SiftPattern<Ctx> {

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
     * @return A {@link QuantifierStep} to configure the repetition of the upcoming token.
     */
    QuantifierStep<Ctx> then();

    /**
     * Safely appends a single literal character to the current sequence.
     * The character is automatically escaped to prevent regex injection.
     *
     * @param c The literal character to append.
     * @return The current connector step for further chaining.
     */
    ConnectorStep<Ctx> followedBy(char c);

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
    ConnectorStep<Ctx> followedBy(SiftPattern<SiftContext.Fragment> p1);

    /**
     * Convenience overload for composing two patterns sequentially.
     *
     * @param p1 The first fragment pattern to append.
     * @param p2 The second fragment pattern to append.
     * @return The current connector step for further chaining.
     */
    ConnectorStep<Ctx> followedBy(SiftPattern<SiftContext.Fragment> p1, SiftPattern<SiftContext.Fragment> p2);

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
    ConnectorStep<Ctx> followedBy(Iterable<? extends SiftPattern<SiftContext.Fragment>> patterns);

    /**
     * Appends a Word Boundary {@code \b} to the sequence.
     * <p>
     * A word boundary matches the position where a word character is not followed or
     * preceded by another word-character, ensuring exact word matches.
     *
     * @return The current connector step for further chaining.
     */
    ConnectorStep<Ctx> wordBoundary();

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
    SiftPattern<SiftContext.Root> andNothingElse();
}