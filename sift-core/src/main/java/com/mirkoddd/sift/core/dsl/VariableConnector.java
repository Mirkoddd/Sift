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
 * A specialized {@link Connector} that follows variable-length quantifiers.
 * <p>
 * This interface exposes modifiers that are only mathematically or logically valid
 * when the preceding element has a variable width (e.g., {@code +}, {@code *}, or {@code {min,max}}).
 * By restricting these modifiers to this specific interface, the DSL prevents nonsensical
 * operations like making an exact quantifier (e.g., {@code .exactly(3)}) lazy or possessive.
 *
 * @author Mirko Dimartino
 * @since 1.5.0
 */
public interface VariableConnector<Ctx extends SiftContext> extends Connector<Ctx> {

    /**
     * Makes the preceding quantifier "possessive" (e.g., {@code *+} or {@code ++}),
     * preventing Catastrophic Backtracking (ReDoS).
     * <p>
     * A possessive quantifier will match as many characters as possible and will <b>never</b>
     * give them back to the engine to try and satisfy the rest of the pattern.
     * <p>
     * <b>Performance Note:</b> Use this when you are certain that the matched sequence
     * should not be re-evaluated, as it significantly reduces the engine's search space.
     *
     * @return A standard {@link Connector}, as possessive modifiers cannot be stacked.
     */
    Connector<Ctx> withoutBacktracking();

    /**
     * Makes the preceding quantifier "lazy" (or reluctant) (e.g., {@code *?} or {@code +?}).
     * <p>
     * A lazy quantifier will match as few characters as possible to make the pattern succeed.
     * <p>
     * <b>Performance Note:</b> Use this when you want to stop matching at the first occurrence
     * of the subsequent pattern, rather than the last (which is the default greedy behavior).
     *
     * @return A standard {@link Connector}, as lazy modifiers cannot be stacked.
     */
    Connector<Ctx> asFewAsPossible();

    /**
     * Anchors the pattern to the end of the input string using {@code $}.
     * <p>
     * Use this terminal operation when you want to ensure that there are no trailing
     * characters after the matched sequence. Calling this method immediately concludes
     * the fluent chain and returns the final executable pattern.
     * <p>
     * <b>State Mutation:</b> This effectively seals the fragment, returning a {@code Root} context.
     *
     * @return The final {@link SiftPattern} ready for evaluation.
     */
    @Override
    SiftPattern<Root> andNothingElse();

    @Override
    SiftPattern<Root> andNothingElseAbsolutely();

    @Override
    SiftPattern<Root> andNothingElseBeforeFinalNewline();
}