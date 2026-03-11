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

import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import java.util.Objects;

import static com.mirkoddd.sift.core.SiftPatterns.anyOf;
import static com.mirkoddd.sift.core.SiftPatterns.literal;
import static com.mirkoddd.sift.core.SiftPatterns.recursive;

/**
 * A fluent builder designed to safely construct recursive nested structures.
 * <p>
 * It enforces the use of a symmetric {@link Delimiter} pair and handles the
 * complex injection of the 'self' recursive reference, shielding the developer
 * from logic errors and unbalanced grammar definitions.
 * </p>
 */
public final class NestingAssembler {
    private final int depth;
    private Delimiter pair;

    NestingAssembler(int depth) {
        this.depth = depth;
    }

    /**
     * Defines the symmetric boundaries for this nested structure.
     *
     * @param pair The {@link Delimiter} pair (e.g., {@link Delimiter#PARENTHESES}).
     * @return This builder instance for method chaining.
     * @throws NullPointerException if the pair is null.
     */
    public NestingAssembler using(Delimiter pair) {
        this.pair = Objects.requireNonNull(pair, "Delimiter pair cannot be null.");
        return this;
    }

    /**
     * Finalizes the nested structure by defining what content is allowed inside.
     * <p>
     * The builder automatically wraps the provided content pattern (and the hidden
     * recursive call to itself) within the specified opening and closing delimiters.
     * Both delimiters are strictly treated as literal strings to prevent regex injection.
     * </p>
     *
     * @param content The pattern representing the allowed content inside the structure.
     * @return A deeply nested fragment capable of parsing balanced structures.
     * @throws NullPointerException  if the content is null.
     * @throws IllegalStateException if {@code using()} was not called prior to this method.
     */
    public SiftPattern<Fragment> containing(SiftPattern<Fragment> content) {
        if (this.pair == null) {
            throw new IllegalStateException(
                    "A Delimiter pair must be specified using .using() before defining content."
            );
        }
        Objects.requireNonNull(content, "Content pattern cannot be null.");

        // THE SECRET SAUCE:
        // We invoke the private unrolling engine. The 'self' parameter representing
        // the recursive call is injected as an alternative to the base content,
        // strictly bounded by the literal opening and closing delimiters.
        return recursive(depth, self ->
                Sift.fromAnywhere()
                        .of(literal(pair.open()))
                        .then()
                        .zeroOrMore().of(anyOf(content, self))
                        .followedBy(literal(pair.close()))
        );
    }
}

