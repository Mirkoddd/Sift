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
package com.mirkoddd.sift.core.engine;

/**
 * Represents advanced regular expression features that may not be universally
 * supported across all third-party regex engines (e.g., Google RE2J, GraalVM).
 * <p>
 * This enumeration is used to track the structural requirements of a generated
 * pattern, allowing external engines to fail-fast if an unsupported feature
 * is requested by the DSL.
 *
 * @since 6.0.0
 */
public enum RegexFeature {
    /** Represents lookahead assertions, such as {@code (?=...)} or {@code (?!...)}. */
    LOOKAHEAD,

    /** Represents lookbehind assertions, such as {@code (?<=...)} or {@code (?<!...)}. */
    LOOKBEHIND,

    /** Represents backreferences to previously captured groups, such as {@code \1} or {@code \k<name>}. */
    BACKREFERENCE,

    /** Represents explicitly named capturing groups, such as {@code (?<name>...)}. */
    NAMED_CAPTURE,

    /** Represents atomic groups, such as {@code (?>...)}, which prevent backtracking. */
    ATOMIC_GROUP,

    /** Represents recursive structures, such as {@code (?R)}. */
    RECURSION,

    /** * Represents the use of inline regex flags, such as {@code (?i:...)} or {@code (?m)}.
     * Some engines may not support specific flags (like comments {@code ?x}) or the scoped syntax.
     */
    INLINE_FLAGS,

    /**
     * Represents the use of the \G anchor (Previous Match End).
     * This is highly specific to the JDK Matcher API and is strictly unsupported by
     * alternative engines like RE2J.
     */
    PREVIOUS_MATCH_ANCHOR,

    /**
     * Represents a conditional statement, e.g., (?(condition)true-pattern|false-pattern).
     * This feature requires backtracking.
     */
    CONDITIONAL,

    /**
     * Represents the \Z anchor (End before optional newline).
     * While natively supported by the standard JDK engine and GraalVM, this specific
     * anchor is explicitly unsupported by engines like RE2/RE2J, which strictly require
     * the absolute end anchor (\z) for end-of-string validation.
     */
    END_BEFORE_NEWLINE_ANCHOR}