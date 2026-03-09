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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;

import static com.mirkoddd.sift.core.Sift.*;
import static com.mirkoddd.sift.core.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

@DisplayName("Open/Closed Principle Showcase")
class SiftOcpTest {

    // =================================================================================
    // 1. THE EXTENSION (Open for Extension)
    // This class adds new capabilities (grammar) using only the public API of Sift.
    // We are NOT modifying Sift's core classes (like Sift.java or SiftConnector.java).
    // =================================================================================
    static class LogGrammar {

        // Adds semantics for [ ... ]
        static SiftPattern<SiftContext.Fragment> bracketed(SiftPattern<SiftContext.Fragment> inner) {
            return fromAnywhere()
                    .character('[')
                    .followedBy(inner)
                    .followedBy(']');
        }

        // Adds semantics for ' ... '
        static SiftPattern<SiftContext.Fragment> quoted(SiftPattern<SiftContext.Fragment> inner) {
            return fromAnywhere()
                    .character('\'')
                    .followedBy(inner)
                    .followedBy('\'');
        }

        // Adds semantics for { ... }
        static SiftPattern<SiftContext.Fragment> braced(SiftPattern<SiftContext.Fragment> inner) {
            return fromAnywhere()
                    .character('{')
                    .followedBy(inner)
                    .followedBy('}');
        }

        // Reusable timestamp pattern: YYYY-MM-DD
        static SiftPattern<SiftContext.Fragment> simpleDate() {
            return fromAnywhere()
                    .exactly(4).digits().followedBy('-')
                    .then().exactly(2).digits().followedBy('-')
                    .then().exactly(2).digits();
        }
    }

    // =================================================================================
    // 2. THE TEST (Putting it together)
    // We build a complex parser for a custom log format using our extension.
    // =================================================================================
    @Test
    @DisplayName("Should parse complex Log lines using external extensions")
    void parseCustomLogFormat() {

        // Target: [INFO] [2026-02-18] User: 'Mirko' -> {Action: Login}

        // Step A: Define the parts using our Grammar Extension
        SiftPattern<SiftContext.Fragment> user     = SiftPatterns.literal("User: ");
        SiftPattern<SiftContext.Fragment> arrow     = SiftPatterns.literal(" -> ");
        SiftPattern<SiftContext.Fragment> action     = SiftPatterns.literal("Action: ");
        SiftPattern<SiftContext.Fragment> logLevel   = LogGrammar.bracketed(literal("INFO"));
        SiftPattern<SiftContext.Fragment> timestamp  = LogGrammar.bracketed(LogGrammar.simpleDate());
        SiftPattern<SiftContext.Fragment> username   = LogGrammar.quoted(fromAnywhere().oneOrMore().letters());
        SiftPattern<SiftContext.Fragment> actionData = LogGrammar.braced(fromAnywhere().of(action).then().oneOrMore().letters());

        // Step B: Compose the final Regex (Declarative & Clean)
        String logRegex = fromStart()
                .of(logLevel)
                .then().optional().character(' ')
                .followedBy(timestamp)
                .then().optional().character(' ')
                .followedBy(Arrays.asList(user, username, arrow, actionData))
                .andNothingElse()
                .shake();

        System.out.println("OCP Log Regex: " + logRegex);
        // Expected approx: ^(?:\[INFO\])(?: )?(?:\[[0-9]{4}-[0-9]{2}-[0-9]{2}\])(?: )?User: (?:'[a-zA-Z]+') -> (?:\{Action: [a-zA-Z]+\})$

        // Verification
        String validLog = "[INFO] [2026-02-18] User: 'Mirko' -> {Action: Login}";

        assertTrue(validLog.matches(logRegex));
    }
}