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
 * Dictionary of all special Regular Expression characters used by Sift.
 * <p>
 * This class centralizes the syntax, removing "magic strings" from the builder logic.
 * The constants are organized to perfectly mirror the methods in
 * {@link com.mirkoddd.sift.core.dsl.TypeStep}.
 */
final class RegexSyntax {

    // ===================================================================================
    // BASE SYNTAX & GENERAL
    // ===================================================================================
    public static final String EMPTY = "";
    public static final String ANY_CHAR = ".";
    public static final String COMMA = ",";

    // ===================================================================================
    // ANCHORS
    // ===================================================================================
    public static final String START_OF_LINE = "^";
    public static final String END_OF_LINE = "$";
    public static final String WORD_BOUNDARY = "\\b";

    // ===================================================================================
    // QUANTIFIERS
    // ===================================================================================
    public static final String ZERO_OR_MORE = "*";
    public static final String ONE_OR_MORE = "+";
    public static final String OPTIONAL = "?";
    public static final String POSSESSIVE = "+";
    public static final String QUANTIFIER_OPEN = "{";
    public static final String QUANTIFIER_CLOSE = "}";

    // ===================================================================================
    // GROUPS & LOGIC
    // ===================================================================================
    public static final String NON_CAPTURING_GROUP_OPEN = "(?:";
    public static final String GROUP_OPEN = "(";
    public static final String GROUP_CLOSE = ")";
    public static final String NAMED_GROUP_OPEN = "(?<";
    public static final String NAMED_GROUP_NAME_CLOSE = ">";
    public static final String OR = "|";

    // ===================================================================================
    // CHARACTER CLASSES (SYNTAX)
    // ===================================================================================
    public static final String CLASS_OPEN = "[";
    public static final String CLASS_CLOSE = "]";
    public static final String CLASS_INTERSECTION_NEGATION = "&&[^";

    // ===================================================================================
    // ASCII TYPES (Default)
    // ===================================================================================

    // Digits
    public static final String RANGE_DIGITS = "0-9";
    public static final String NON_DIGITS = "\\D";

    // Letters
    public static final String RANGE_LETTERS = "a-zA-Z";
    public static final String NON_LETTERS = "^a-zA-Z"; // Stripped outer brackets
    public static final String RANGE_LETTERS_UPPERCASE_ONLY = "A-Z";
    public static final String RANGE_LETTERS_LOWERCASE_ONLY = "a-z";

    // Alphanumeric
    public static final String RANGE_ALPHANUMERIC = "a-zA-Z0-9";
    public static final String NON_ALPHANUMERIC = "^a-zA-Z0-9"; // Stripped outer brackets

    // Word Characters
    public static final String WORD_CHARACTERS = "\\w";
    public static final String NON_WORD_CHARACTERS = "\\W";

    // Whitespace
    public static final String WHITESPACE = "\\s";
    public static final String NON_WHITESPACE = "\\S";

    // ===================================================================================
    // UNICODE TYPES
    // ===================================================================================

    // Digits
    public static final String UNICODE_DIGITS = "\\p{Nd}";
    public static final String NON_UNICODE_DIGITS = "\\P{Nd}";

    // Letters
    public static final String UNICODE_LETTERS = "\\p{L}";
    public static final String NON_UNICODE_LETTERS = "\\P{L}";
    public static final String UNICODE_LETTERS_UPPERCASE_ONLY = "\\p{Lu}";
    public static final String UNICODE_LETTERS_LOWERCASE_ONLY = "\\p{Ll}";

    // Alphanumeric
    public static final String UNICODE_ALPHANUMERIC = "\\p{L}\\p{Nd}"; // Stripped outer brackets
    public static final String NON_UNICODE_ALPHANUMERIC = "^\\p{L}\\p{Nd}"; // Stripped outer brackets

    // Word Characters
    public static final String UNICODE_WORD_CHARACTERS = "\\p{L}\\p{Nd}_"; // Stripped outer brackets
    public static final String NON_UNICODE_WORD_CHARACTERS = "^\\p{L}\\p{Nd}_"; // Stripped outer brackets

    // Whitespace
    public static final String UNICODE_WHITESPACE = "\\p{IsWhite_Space}";
    public static final String NON_UNICODE_WHITESPACE = "\\P{IsWhite_Space}";

    // ===================================================================================

    private RegexSyntax() {
        // Prevent instantiation of utility class
    }
}