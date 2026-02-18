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
 * The link between steps in the chain.
 * <p>
 * This interface represents a state where a token has just been defined (e.g., "digits").
 * From here, you can:
 * <ul>
 * <li>Refine the current token (using {@code including} or {@code butNot}).</li>
 * <li>Append a new literal or pattern (using {@code followedBy(...)}).</li>
 * <li>Transition back to a quantifier for a new token (using {@code followedBy()}).</li>
 * <li>Finalize the regex structure (using {@code untilEnd()}).</li>
 * </ul>
 */
public interface ConnectorStep extends SiftPattern {

    /**
     * Extends the preceding character class to include specific characters.
     * <p>
     * This transforms a standard class into a custom set.
     * <br>Example: {@code .digits().including('.')} creates {@code [0-9.]}.
     *
     * @param extra The characters to add to the class.
     * @return The current step, allowing further modifications.
     */
    ConnectorStep including(char... extra);

    /**
     * Excludes specific characters from the preceding character class.
     * <p>
     * <br>Example: {@code .letters().excluding('x', 'y')} creates a class that matches
     * all letters <b>except</b> 'x' and 'y'.
     *
     * @param excluded The characters to forbid.
     * @return The current step, allowing further modifications.
     */
    ConnectorStep excluding(char... excluded);

    /**
     * Transitions back to the {@link QuantifierStep} to begin defining a <b>NEW</b> token.
     * <p>
     * Use this method when you want to stop defining the current element and start
     * defining the quantity of the <i>next</i> element.
     *
     * @return The quantifier step for the next element in the chain.
     */
    QuantifierStep followedBy();

    /**
     * Appends a single literal character to the regex.
     * <p>
     * Special regex characters (like {@code .}, {@code *}, {@code ?}) are automatically escaped.
     *
     * @param c The character to match exactly.
     * @return The current connector step, allowing immediate chaining.
     */
    ConnectorStep followedBy(char c);

    /**
     * Appends a complex {@link SiftPattern} (e.g., a capture group, an 'anyOf' block, or another Sift chain).
     *
     * @param pattern The sub-pattern to append.
     * @return The current connector step, allowing immediate chaining.
     */
    ConnectorStep followedBy(SiftPattern pattern);

    /**
     * Asserts a <b>Word Boundary</b> {@code \b} at the current position.
     * <p>
     * This checks that the current position is a boundary between a word character ({@code \w})
     * and a non-word character ({@code \W}). It does not consume any characters.
     *
     * @return The current connector step.
     */
    ConnectorStep wordBoundary();

    /**
     * Anchors the regex to the <b>End of the String</b> using {@code $}.
     * <p>
     * This ensures that no other characters can follow the matched pattern.
     * This is typically the last step before calling {@code shake()}.
     *
     * @return A pattern ready to be finalized.
     */
    SiftPattern untilEnd();

    /**
     * <b>Syntactic Sugar:</b> Appends an optional complex pattern to the regex.
     * <p>
     * This is a convenient shortcut for {@code .followedBy().optional().followedBy(pattern)}.
     * It allows for a more fluid reading of the code.
     * <p>
     * <b>Example:</b>
     * <pre>
     * .digits().withOptional(italyPrefix)
     * </pre>
     *
     * @param pattern The complex pattern that should be optional.
     * @return The current connector step, allowing immediate chaining.
     */
    ConnectorStep withOptional(SiftPattern pattern);

    /**
     * <b>Syntactic Sugar:</b> Appends an optional single character to the regex.
     * <p>
     * This is a convenient shortcut for {@code .followedBy().optional().followedBy(character)}.
     * Useful for optional separators.
     * <p>
     * <b>Example:</b>
     * <pre>
     * .digits().withOptional(' ')
     * </pre>
     *
     * @param character The character literal that should be optional.
     * @return The current connector step, allowing immediate chaining.
     */
    ConnectorStep withOptional(char character);
}