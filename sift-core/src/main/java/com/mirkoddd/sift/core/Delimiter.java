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

import java.util.Objects;

/**
 * Represents a symmetric pair of delimiters used to define boundaries for nested structures.
 * <p>
 * In formal language theory (specifically Dyck languages), balanced structures require a strict
 * 1-to-1 relationship between an opening symbol and a closing symbol. This class enforces that
 * contract, ensuring that developers cannot accidentally create asymmetric recursive patterns
 * (e.g., opening with a parenthesis but closing with a square bracket).
 * </p>
 * <p>
 * Standard symmetric pairs are provided as immutable constants. For domain-specific parsers, 
 * custom boundaries can be generated via the {@link #custom(String, String)} factory method.
 * </p>
 */
public final class Delimiter {

    private final String open;
    private final String close;

    /**
     * Standard parentheses pair: {@code (} and {@code )}.
     */
    public static final Delimiter PARENTHESES = new Delimiter("(", ")");

    /**
     * Standard square brackets pair: {@code [} and {@code ]}.
     */
    public static final Delimiter BRACKETS = new Delimiter("[", "]");

    /**
     * Standard curly braces pair: {@code \{} and {@code \}}.
     */
    public static final Delimiter BRACES = new Delimiter("{", "}");

    /**
     * Standard angle brackets (chevrons) pair: {@code <} and {@code >}.
     */
    public static final Delimiter CHEVRONS = new Delimiter("<", ">");

    /**
     * Internal constructor to enforce symmetry.
     *
     * @param open  The exact literal string that opens the structure.
     * @param close The exact literal string that closes the structure.
     */
    private Delimiter(String open, String close) {
        this.open = open;
        this.close = close;
    }

    /**
     * Factory method to create a custom symmetric pair for domain-specific nested structures
     * (e.g., HTML tags, multi-character comment blocks like {@code /*} and {@code *\/}).
     *
     * @param open  The exact literal string that opens the structure. Cannot be null.
     * @param close The exact literal string that closes the structure. Cannot be null.
     * @return A new {@link Delimiter} instance representing the symmetric contract.
     * @throws NullPointerException if either parameter is null.
     */
    public static Delimiter custom(String open, String close) {
        return new Delimiter(
                Objects.requireNonNull(open, "Opening delimiter cannot be null."),
                Objects.requireNonNull(close, "Closing delimiter cannot be null.")
        );
    }

    /**
     * Retrieves the opening boundary literal.
     *
     * @return The literal string that triggers a new nesting level.
     */
    public String open() {
        return open;
    }

    /**
     * Retrieves the closing boundary literal.
     *
     * @return The literal string that terminates the current nesting level.
     */
    public String close() {
        return close;
    }
}