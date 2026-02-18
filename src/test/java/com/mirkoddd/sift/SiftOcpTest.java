package com.mirkoddd.sift;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mirkoddd.sift.dsl.SiftPattern;

import static com.mirkoddd.sift.Sift.*;
import static com.mirkoddd.sift.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Open/Closed Principle Showcase")
class SiftOcpTest {

    // =================================================================================
    // 1. THE EXTENSION (Open for Extension)
    // This class adds new capabilities (grammar) using only the public API of Sift.
    // We are NOT modifying SiftBuilder.java.
    // =================================================================================
    static class LogGrammar {

        // Adds semantics for [ ... ]
        static SiftPattern bracketed(SiftPattern inner) {
            return anywhere()
                    .followedBy('[')
                    .followedBy(inner)
                    .followedBy(']');
        }

        // Adds semantics for ' ... '
        static SiftPattern quoted(SiftPattern inner) {
            return anywhere()
                    .followedBy('\'')
                    .followedBy(inner)
                    .followedBy('\'');
        }

        // Adds semantics for { ... }
        static SiftPattern braced(SiftPattern inner) {
            return anywhere()
                    .followedBy('{')
                    .followedBy(inner)
                    .followedBy('}');
        }

        // Reusable timestamp pattern: YYYY-MM-DD
        static SiftPattern simpleDate() {
            return anywhere()
                    .exactly(4).digits().followedBy('-')
                    .followedBy().exactly(2).digits().followedBy('-')
                    .followedBy().exactly(2).digits();
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
        SiftPattern user     = SiftPatterns.literal("User: ");
        SiftPattern arrow     = SiftPatterns.literal(" -> ");
        SiftPattern action     = SiftPatterns.literal("Action: ");
        SiftPattern logLevel   = LogGrammar.bracketed(literal("INFO"));
        SiftPattern timestamp  = LogGrammar.bracketed(LogGrammar.simpleDate());
        SiftPattern username   = LogGrammar.quoted(anywhere().oneOrMore().letters());
        SiftPattern actionData = LogGrammar.braced(anywhere().followedBy(action).followedBy().oneOrMore().letters());

        // Step B: Compose the final Regex (Declarative & Clean)
        String logRegex = fromStart()
                .followedBy(logLevel)
                .withOptional(' ')
                .followedBy(timestamp)
                .withOptional(' ')
                .followedBy(user)
                .followedBy(username)
                .followedBy(arrow)
                .followedBy(actionData)
                .untilEnd()
                .shake();

        System.out.println("OCP Log Regex: " + logRegex);
        // Expected approx: ^(?:\[INFO\])(?: )?(?:\[[0-9]{4}-[0-9]{2}-[0-9]{2}\])(?: )?User: (?:'[a-zA-Z]+') -> (?:\{Action: [a-zA-Z]+\})$

        // Verification
        String validLog = "[INFO] [2026-02-18] User: 'Mirko' -> {Action: Login}";

        assertTrue(validLog.matches(logRegex));
    }
}