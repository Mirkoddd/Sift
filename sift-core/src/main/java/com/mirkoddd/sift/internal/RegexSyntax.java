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
package com.mirkoddd.sift.internal;

/**
 * Dictionary of all special Regular Expression characters used by Sift.
 * <p>
 * This class centralizes the syntax, removing "magic strings" from the builder logic.
 */
public final class RegexSyntax {
    public static final String EMPTY = "";

    // Anchors
    public static final String START_OF_LINE = "^";
    public static final String END_OF_LINE = "$";
    public static final String WORD_BOUNDARY = "\\b";

    // Quantifiers
    public static final String ZERO_OR_MORE = "*";
    public static final String ONE_OR_MORE = "+";
    public static final String OPTIONAL = "?";
    public static final String QUANTIFIER_OPEN = "{";
    public static final String QUANTIFIER_CLOSE = "}";
    public static final String COMMA = ",";
    public static final String POSSESSIVE = "+";

    // Groups & Logic
    public static final String NON_CAPTURING_GROUP_OPEN = "(?:";
    public static final String GROUP_OPEN = "(";
    public static final String GROUP_CLOSE = ")";
    public static final String NAMED_GROUP_OPEN = "(?<";
    public static final String NAMED_GROUP_NAME_CLOSE = ">";
    public static final String ATOMIC_GROUP_OPEN = "(?>";
    public static final String OR = "|";

    // Character Classes
    public static final String CLASS_OPEN = "[";
    public static final String CLASS_CLOSE = "]";
    public static final String CLASS_INTERSECTION_NEGATION = "&&[^";
    public static final String ANY_CHAR = ".";

    // Ranges (Pre-defined)
    public static final String RANGE_DIGITS = "0-9";
    public static final String RANGE_LETTERS = "a-zA-Z";
    public static final String RANGE_LOWERCASE = "a-z";
    public static final String RANGE_UPPERCASE = "A-Z";
    public static final String RANGE_ALPHANUMERIC = "a-zA-Z0-9";

    private RegexSyntax() {}
}