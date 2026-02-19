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
 * Defines the <b>TYPE</b> of character or pattern to match.
 * <p>
 * This interface represents the state immediately after a quantifier has been set
 * (e.g., {@code exactly(3)} or {@code oneOrMore()}). The method selected here defines
 * <i>what</i> exactly applies to that quantifier.
 * <p>
 * Selecting a type consumes the pending quantifier and transitions the builder to the
 * {@link ConnectorStep}.
 */
public interface TypeStep {

    /**
     * Matches any numeric digit.
     * <p>
     * Equivalent to the regex range {@code [0-9]}.
     *
     * @return The connector step to continue building.
     */
    ConnectorStep digits();

    /**
     * Matches any letter from the alphabet (both uppercase and lowercase).
     * <p>
     * Equivalent to the regex range {@code [a-zA-Z]}.
     *
     * @return The connector step to continue building.
     */
    ConnectorStep letters();

    /**
     * Matches only uppercase letters.
     * <p>
     * Equivalent to the regex range {@code [A-Z]}.
     *
     * @return The connector step to continue building.
     */
    ConnectorStep lettersUppercaseOnly();

    /**
     * Matches only lowercase letters.
     * <p>
     * Equivalent to the regex range {@code [a-z]}.
     *
     * @return The connector step to continue building.
     */
    ConnectorStep lettersLowercaseOnly();

    /**
     * Matches any alphanumeric character (letters and digits).
     * <p>
     * Equivalent to the regex range {@code [a-zA-Z0-9]}.
     *
     * @return The connector step to continue building.
     */
    ConnectorStep alphanumeric();

    /**
     * Matches <b>ANY</b> single character (the Dot {@code .}).
     * <p>
     * <b>Note:</b> This usually includes whitespace and symbols, but excludes line terminators
     * (like {@code \n}) unless specific matcher flags are enabled.
     *
     * @return The connector step to continue building.
     */
    ConnectorStep any();

    /**
     * Applies the pending quantifier to a specific literal character.
     * <p>
     * Example: {@code .exactly(3).followedBy('a')} will match "aaa".
     * Special regex characters are automatically escaped.
     *
     * @param literal The character to match.
     * @return The connector step to continue building.
     */
    ConnectorStep followedBy(char literal);

    /**
     * Applies the pending quantifier to a complex SiftPattern.
     * <p>
     * Example: {@code .optional().followedBy(myGroup)} makes the entire group optional.
     * Internally, this wraps the pattern in a non-capturing group {@code (?:...)} if necessary.
     *
     * @param pattern The sub-pattern to apply the quantifier to.
     * @return The connector step to continue building.
     */
    ConnectorStep followedBy(SiftPattern pattern);
}