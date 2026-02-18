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
package com.mirkoddd.sift.dsl;

/**
 * Defines <b>HOW MANY TIMES</b> the next element should appear.
 * <p>
 * This interface extends {@link TypeStep}, which allows for a concise syntax:
 * if no quantifier is explicitly chosen, it defaults to matching <b>exactly once</b>.
 * <p>
 * <b>Flow Examples:</b>
 * <ul>
 * <li>Explicit: {@code .exactly(3).digits()}</li>
 * <li>Implicit: {@code .digits()} (implies exactly 1)</li>
 * <li>Sugar: {@code .withOptional(pattern)} (applies quantifier directly to argument)</li>
 * </ul>
 */
public interface QuantifierStep extends TypeStep {

    /**
     * Matches exactly {@code n} times. Generates {@code {n}}.
     *
     * @param n The exact number of repetitions (must be >= 0).
     * @return The next step to define WHAT to repeat.
     * @throws IllegalArgumentException if n is negative.
     */
    TypeStep exactly(int n);

    /**
     * Matches at least {@code n} times. Generates {@code {n,}}.
     * <p>
     * Note: While {@code atLeast(0)} is valid, it is semantically equivalent to {@code zeroOrMore()}.
     * Use this for explicit numeric bounds (e.g., at least 3).
     *
     * @param n The minimum number of repetitions (must be >= 0).
     * @return The next step to define WHAT to repeat.
     * @throws IllegalArgumentException if n is negative.
     */
    TypeStep atLeast(int n);

    /**
     * Matches one or more times. Generates {@code +}.
     * <p>
     * Equivalent to {@code atLeast(1)}.
     *
     * @return The next step to define WHAT to repeat.
     */
    TypeStep oneOrMore();

    /**
     * Matches zero or more times (Greedy). Generates {@code *}.
     * <p>
     * This matches as many occurrences as possible.
     *
     * @return The next step to define WHAT to repeat.
     */
    TypeStep zeroOrMore();

    /**
     * Matches zero or one time. Generates {@code ?}.
     * <p>
     * Marks the <b>next</b> type definition as optional.
     * <br>Example: {@code .optional().digits()} matches a digit or nothing.
     *
     * @return The next step to define WHAT is optional.
     */
    TypeStep optional();

    /**
     * <b>Syntactic Sugar:</b> Matches the provided {@code pattern} zero or one time.
     * <p>
     * This is a shortcut for {@code .optional().followedBy(pattern)}.
     * It automatically wraps the pattern in a non-capturing group {@code (?:...)?} to ensure
     * the quantifier applies to the entire block.
     * <p>
     * <b>Usage:</b>
     * <pre>
     * .withOptional(italyPrefix) // e.g., (?:(?:\+39|0039))?
     * </pre>
     *
     * @param pattern The complex pattern to be made optional.
     * @return A {@link ConnectorStep} to continue chaining, as the optional block is complete.
     */
    ConnectorStep withOptional(SiftPattern pattern);

    /**
     * <b>Syntactic Sugar:</b> Matches the provided {@code character} zero or one time.
     * <p>
     * This is a shortcut for {@code .optional().followedBy(character)}.
     * Extremely useful for separators like spaces or dashes.
     * <p>
     * <b>Usage:</b>
     * <pre>
     * .withOptional(' ') // e.g., (?: )? or  ?
     * </pre>
     *
     * @param character The character literal to be made optional.
     * @return A {@link ConnectorStep} to continue chaining.
     */
    ConnectorStep withOptional(char character);
}