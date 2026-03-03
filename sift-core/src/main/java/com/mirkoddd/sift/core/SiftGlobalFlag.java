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

/**
 * Inline flags to modify the regular expression engine behavior globally.
 * <p>
 * These flags are applied at the very beginning of the pattern 
 * (e.g., {@code (?imx)}) to instruct the Regex engine.
 */
public enum SiftGlobalFlag {

    /**
     * Enables case-insensitive matching ({@code (?i)}).
     * <p>
     * By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched.
     * To enable Unicode-aware case-insensitive matching, combine this with {@link #UNICODE_CASE}.
     */
    CASE_INSENSITIVE("i"),

    /**
     * Enables multiline mode ({@code (?m)}).
     * <p>
     * In multiline mode, the expressions {@code ^} (fromStart) and {@code $} (andNothingElse)
     * match just after or just before a line terminator, rather than only at the
     * beginning or end of the entire input string.
     */
    MULTILINE("m"),

    /**
     * Enables dotall mode ({@code (?s)}).
     * <p>
     * In dotall mode, the {@code .any()} expression matches any character,
     * including a line terminator (which it normally ignores).
     */
    DOTALL("s"),

    /**
     * Enables verbose/comments mode ({@code (?x)}).
     * <p>
     * In this mode, whitespace is ignored, and embedded comments starting with
     * {@code #} are ignored until the end of a line.
     * <p>
     * <i>Note: Sift's internal {@code literal()} generator automatically escapes spaces
     * and {@code #} characters to ensure your literals remain strictly matched even when
     * this flag is active.</i>
     */
    COMMENTS("x"),

    /**
     * Enables Unicode-aware case folding ({@code (?u)}).
     * <p>
     * When this flag is specified alongside {@link #CASE_INSENSITIVE}, case-insensitive matching,
     * it will be done in a manner consistent with the Unicode Standard.
     */
    UNICODE_CASE("u");

    private final String symbol;

    SiftGlobalFlag(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Retrieves the regex symbol associated with this flag.
     *
     * @return The literal character used by the regex engine (e.g., 'i', 'm', 's', 'x', 'u').
     */
    public String getSymbol() {
        return symbol;
    }
}