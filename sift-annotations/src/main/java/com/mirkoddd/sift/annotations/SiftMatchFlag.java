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
package com.mirkoddd.sift.annotations;

import java.util.regex.Pattern;

/**
 * Type-safe wrapper for {@link java.util.regex.Pattern} compilation flags.
 * <p>
 * These flags can be passed to the {@code flags} attribute of the {@link SiftMatch} annotation
 * to modify how the regular expression engine interprets the pattern.
 *
 * @author Mirko Dimartino
 * @version {@value BuildInfo#VERSION}
 * @since 1.1.0
 */
public enum SiftMatchFlag {
    /**
     * Enables case-insensitive matching.
     * By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched.
     */
    CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),

    /**
     * Enables multiline mode.
     * In multiline mode the expressions ^ and $ match just after or just before,
     * respectively, a line terminator or the end of the input sequence.
     */
    MULTILINE(Pattern.MULTILINE),

    /**
     * Enables dotall mode.
     * In dotall mode, the expression . matches any character, including a line terminator.
     */
    DOTALL(Pattern.DOTALL),

    /**
     * Enables Unicode-aware case folding.
     * When this flag is specified then case-insensitive matching, when enabled by the CASE_INSENSITIVE flag,
     * is done in a manner consistent with the Unicode Standard.
     */
    UNICODE_CASE(Pattern.UNICODE_CASE),

    /**
     * Enables UNIX lines mode.
     * In this mode, only the '\n' line terminator is recognized in the behavior of ., ^, and $.
     */
    UNIX_LINES(Pattern.UNIX_LINES),

    /**
     * Enables literal parsing of the pattern.
     * When this flag is specified then the input string that specifies the pattern
     * is treated as a sequence of literal characters.
     */
    LITERAL(Pattern.LITERAL),

    /**
     * Permits whitespace and comments in the pattern.
     * In this mode, whitespace is ignored, and embedded comments starting with # are ignored until the end of a line.
     */
    COMMENTS(Pattern.COMMENTS);

    private final int value;

    SiftMatchFlag(int value) {
        this.value = value;
    }

    /**
     * Returns the underlying int value of the {@link java.util.regex.Pattern} flag.
     *
     * @return the integer bitmask for the flag
     */
    public int getValue() {
        return value;
    }
}