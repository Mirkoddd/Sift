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

import com.mirkoddd.sift.core.NamedCapture;
import com.mirkoddd.sift.core.SiftPatterns;

/**
 * Defines <b>HOW MANY TIMES</b> the next element should appear, or injects structural elements.
 * <p>
 * This interface extends {@link Type}, which allows for a concise syntax:
 * if no quantifier is explicitly chosen, it defaults to matching <b>exactly once</b>.
 * <p>
 * <b>Type-Safe Modifiers:</b><br>
 * Notice the different return types. Methods producing a fixed repetition (like {@code exactly()})
 * return a standard {@code Connector}. Methods producing a variable repetition (like {@code oneOrMore()})
 * return a {@code VariableConnector}, which safely unlocks greedy/reluctant/possessive modifiers.
 * <p>
 * <b>Flow Examples:</b>
 * <ul>
 * <li>Explicit Quantity: {@code .exactly(3).digits()}</li>
 * <li>Implicit Quantity: {@code .digits()} (implies exactly 1)</li>
 * <li>Named Capture: {@code .namedCapture(groupDefinition)} (injects a named group)</li>
 * <li>Backreference: {@code .backreference(groupDefinition)} (references a previous group)</li>
 * </ul>
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
public interface Quantifier<Ctx extends SiftContext> extends Type<Ctx, Connector<Ctx>, CharacterConnector<Ctx>> {

    /**
     * Matches exactly {@code n} times. Generates {@code {n}}.
     *
     * @param n The exact number of repetitions (must be >= 0).
     * @return The next step to define WHAT to repeat (fixed length).
     * @throws IllegalArgumentException if n is negative.
     */
    Type<Ctx, Connector<Ctx>, CharacterConnector<Ctx>> exactly(int n);

    /**
     * Matches at least {@code n} times. Generates {@code {n,}}.
     * <p>
     * <b>Note:</b> While {@code atLeast(0)} is valid, it is semantically equivalent to {@code zeroOrMore()}.
     * Use this for explicit numeric bounds (e.g., at least 3).
     *
     * @param n The minimum number of repetitions (must be >= 0).
     * @return The next step to define WHAT to repeat (variable length, allowing relational modifiers).
     * @throws IllegalArgumentException if n is negative.
     */
    Type<Ctx, VariableConnector<Ctx>, VariableCharacterConnector<Ctx>> atLeast(int n);

    /**
     * Matches one or more times. Generates {@code +}.
     * <p>
     * Equivalent to {@code atLeast(1)}.
     *
     * @return The next step to define WHAT to repeat (variable length, allowing relational modifiers).
     */
    Type<Ctx, VariableConnector<Ctx>, VariableCharacterConnector<Ctx>> oneOrMore();

    /**
     * Matches zero or more times (Greedy by default). Generates {@code *}.
     * <p>
     * This matches as many occurrences as possible.
     *
     * @return The next step to define WHAT to repeat (variable length, allowing relational modifiers).
     */
    Type<Ctx, VariableConnector<Ctx>, VariableCharacterConnector<Ctx>> zeroOrMore();

    /**
     * Matches zero or one time. Generates {@code ?}.
     * <p>
     * Marks the <b>next</b> type definition as optional.
     * <br>Example: {@code .optional().digits()} matches a digit or nothing.
     *
     * @return The next step to define WHAT is optional (variable length, allowing relational modifiers).
     */
    Type<Ctx, VariableConnector<Ctx>, VariableCharacterConnector<Ctx>> optional();

    /**
     * Matches at most {@code max} times. Generates {@code {0,max}}.
     *
     * @param max The maximum number of repetitions (must be strictly positive, i.e., > 0).
     * @return The next step to define WHAT to repeat (variable length, allowing relational modifiers).
     * @throws IllegalArgumentException if max is zero or negative.
     */
    Type<Ctx, VariableConnector<Ctx>, VariableCharacterConnector<Ctx>> atMost(int max);

    /**
     * Matches between {@code min} and {@code max} times inclusive. Generates {@code {min,max}}.
     *
     * @param min The minimum number of repetitions (must be >= 0).
     * @param max The maximum number of repetitions (must be strictly positive, i.e., > 0, and >= min).
     * @return The next step to define WHAT to repeat (variable length, allowing relational modifiers).
     * @throws IllegalArgumentException if min is negative, max is zero or negative, or if min is greater than max.
     */
    Type<Ctx, VariableConnector<Ctx>, VariableCharacterConnector<Ctx>> between(int min, int max);

    /**
     * Injects a zero-width assertion immediately at this point in the sequence.
     * Assertions do not consume characters and bypass the quantification step.
     *
     * @param assertion The assertion to inject.
     * @return A connector to continue building the sequence.
     */
    Connector<Ctx> followedByAssertion(SiftPattern<Assertion> assertion);

    /**
     * Asserts that the current sequence <b>must</b> be followed by the given pattern.
     *
     * @param pattern The condition that must be met ahead.
     * @return A connector to continue building the sequence.
     */
    default Connector<Ctx> mustBeFollowedBy(SiftPattern<Fragment> pattern) {
        return this.followedByAssertion(SiftPatterns.positiveLookahead(pattern));
    }

    /**
     * Asserts that the current sequence is <b>not</b> followed by the given pattern.
     *
     * @param pattern The condition that must NOT be met ahead.
     * @return A connector to continue building the sequence.
     */
    default Connector<Ctx> notFollowedBy(SiftPattern<Fragment> pattern) {
        return this.followedByAssertion(SiftPatterns.negativeLookahead(pattern));
    }

    /**
     * Starts a named capturing group using the provided definition.
     *
     * @param group The named capture definition containing the name and the pattern.
     * @return A connector step to continue the chain.
     */
    Connector<Ctx> namedCapture(NamedCapture group);

    /**
     * References a previously captured group by its name.
     *
     * @param group The named capture definition to refer back to.
     * @return A connector step to continue the chain.
     * @throws IllegalStateException if the group has not been captured yet in this builder sequence.
     */
    Connector<Ctx> backreference(NamedCapture group);
}