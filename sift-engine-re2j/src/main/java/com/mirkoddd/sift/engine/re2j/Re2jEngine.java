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
package com.mirkoddd.sift.engine.re2j;

import static com.mirkoddd.sift.core.Sift.exactly;
import static com.mirkoddd.sift.core.Sift.oneOrMore;
import static com.mirkoddd.sift.core.SiftPatterns.capture;
import static com.mirkoddd.sift.core.SiftPatterns.literal;
import static com.mirkoddd.sift.engine.re2j.Re2jDictionary.*;

import com.mirkoddd.sift.core.NamedCapture;
import com.mirkoddd.sift.core.engine.AbstractSiftEngine;
import com.mirkoddd.sift.core.engine.RegexFeature;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * RE2J execution engine for Sift.
 * <p>
 * This engine guarantees linear-time execution (O(n)) to prevent ReDoS attacks,
 * but restricts the use of certain advanced regex features like Lookarounds and Backreferences.
 *
 * @since 6.1.0
 */
public final class Re2jEngine extends AbstractSiftEngine {

    /**
     * Stateless singleton instance of the RE2J Engine.
     */
    public static final Re2jEngine INSTANCE = new Re2jEngine();

    private static final NamedCapture GROUP_NAME = capture(
            NAME, oneOrMore().wordCharacters()
    );

    private static final SiftCompiledPattern JDK_NAMED_GROUP_SYNTAX = exactly(1)
            .of(literal(JDK_GROUP_PREFIX))
            .then().namedCapture(GROUP_NAME)
            .followedBy(JDK_GROUP_SUFFIX)
            .sieve();

    private static final Map<RegexFeature, String> UNSUPPORTED = new EnumMap<>(RegexFeature.class);
    static {
        UNSUPPORTED.put(RegexFeature.LOOKAHEAD, "RE2J Engine does not support Lookaround assertions.");
        UNSUPPORTED.put(RegexFeature.LOOKBEHIND, "RE2J Engine does not support Lookaround assertions.");
        UNSUPPORTED.put(RegexFeature.BACKREFERENCE, "RE2J Engine does not support Backreferences, as they break linear-time guarantees.");
        UNSUPPORTED.put(RegexFeature.PREVIOUS_MATCH_ANCHOR, "RE2J Engine does not support the \\G anchor (Previous Match End).");
        UNSUPPORTED.put(RegexFeature.ATOMIC_GROUP, "RE2J Engine does not support Atomic Groups (?>...). RE2J is natively immune to backtracking.");
        UNSUPPORTED.put(RegexFeature.RECURSION, "RE2J Engine does not support recursive patterns.");
        UNSUPPORTED.put(RegexFeature.CONDITIONAL, "RE2J Engine does not support Conditional assertions. They break linear-time guarantees.");
    }

    private Re2jEngine() {
        // Prevent instantiation
    }

    @Override
    protected Map<RegexFeature, String> getUnsupportedFeatures() {
        return UNSUPPORTED;
    }

    @Override
    protected SiftCompiledPattern doCompile(String rawRegex, Set<RegexFeature> usedFeatures) {
        try {
            String re2jCompatibleRegex = rawRegex;

            if (usedFeatures.contains(RegexFeature.NAMED_CAPTURE)) {
                re2jCompatibleRegex = JDK_NAMED_GROUP_SYNTAX.replaceAll(rawRegex, RE2J_GROUP_REPLACEMENT);
            }

            com.google.re2j.Pattern re2jPattern = com.google.re2j.Pattern.compile(re2jCompatibleRegex);
            return new Re2jCompiledPattern(re2jPattern, rawRegex);
        } catch (com.google.re2j.PatternSyntaxException e) {
            throw new IllegalArgumentException("RE2J engine rejected the generated syntax: " + e.getMessage(), e);
        }
    }
}