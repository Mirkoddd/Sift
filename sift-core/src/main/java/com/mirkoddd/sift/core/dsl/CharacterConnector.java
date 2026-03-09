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
 * A specialized {@link Connector} that safely exposes modifiers for character classes.
 * <p>
 * This interface enforces the <b>Interface Segregation Principle (ISP)</b> by ensuring that inclusion
 * and exclusion modifiers can only be invoked immediately after a character class definition.
 * Attempting to apply these modifiers to incompatible constructs (like a literal or a capturing group)
 * is physically prevented at compile-time by the type system.
 * <p>
 * <b>Example Usage:</b>
 * <pre>
 * Sift.exactly(1).letters().including('-') // Matches any letter or a hyphen
 * </pre>
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
public interface CharacterConnector<Ctx extends SiftContext> extends Connector<Ctx> {

    /**
     * Safely adds specific characters to the currently built character class.
     * <p>
     * All provided characters are automatically escaped by the engine, preventing regex injection.
     * For example, calling {@code including('[')} will safely match a literal bracket
     * without breaking the underlying character class syntax.
     *
     * @param extra            The primary mandatory character to include.
     * @param additionalExtras Optional additional primitive characters to include (varargs are safe here).
     * @return The current builder step for further fluent chaining.
     */
    CharacterConnector<Ctx> including(char extra, char... additionalExtras);

    /**
     * Safely excludes specific characters from the currently built character class.
     * <p>
     * This effectively creates a subtracted character class. All provided characters
     * are automatically escaped by the engine to maintain structural integrity.
     *
     * @param excluded           The primary mandatory character to exclude.
     * @param additionalExcluded Optional additional primitive characters to exclude.
     * @return The current builder step for further fluent chaining.
     */
    CharacterConnector<Ctx> excluding(char excluded, char... additionalExcluded);
}