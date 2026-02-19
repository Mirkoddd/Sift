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
package com.mirkoddd.sift;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mirkoddd.sift.dsl.SiftPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mirkoddd.sift.Sift.*;
import static com.mirkoddd.sift.SiftPatterns.*;

/**
 * Demonstrates a "Real World" use case: Data Mining / Log Extraction.
 * <p>
 * Unlike validation tests (which check if the WHOLE string matches a pattern),
 * this class uses Sift to scan unstructured text and extract specific information
 * (the "needle in the haystack" approach).
 */
@DisplayName("Real World: Log Mining & Extraction")
class SiftLogMiningTest {

    // =================================================================================
    // 1. THE WALL OF TEXT
    // =================================================================================
    // Simulates a messy server log file with mixed formats and noise.
    // Using Text Blocks for readability.
    private final String SERVER_LOGS = """
            [INFO] [2026-02-18] System: Booting up services... OK.
            [INFO] [2026-02-18] User: 'Mirko' -> {Action: Login} - IP: 192.168.1.1
            [WARN] [2026-02-18] Disk: Usage at 85%.
            garbage_data_noise_#$@#$
            [ERR] [2026-02-18] User: 'HackBot' -> {Action: Inject} - BLOCKED
            [INFO] [2026-02-18] User: 'Alice' -> {Action: Upload} - File: data.csv
            [DEBUG] Connection established.
            [INFO] [2026-02-18] User: 'Bob' -> {Action: Logout}
            """;

    // =================================================================================
    // 2. THE GRAMMAR (The "Business Language")
    // =================================================================================
    /**
     * Encapsulates domain-specific syntax.
     * This adheres to the Open/Closed Principle: we extend the language vocabulary
     * without modifying the core Sift library.
     */
    static class LogGrammar {

        // Matches content enclosed in single quotes: '...'
        static SiftPattern quoted(SiftPattern inner) {
            return anywhere().followedBy('\'').followedBy(inner).followedBy('\'');
        }

        // Matches content enclosed in curly braces: { ... }
        static SiftPattern braced(SiftPattern inner) {
            return anywhere().followedBy('{').followedBy(inner).followedBy('}');
        }
    }

    // =================================================================================
    // 3. THE GREP TOOL (Extraction Utility)
    // =================================================================================
    /**
     * Scans the provided text for substrings that match the SiftPattern.
     * <p>
     * Note: We use matcher.find() (substring search) instead of matches() (full validation).
     *
     * @param text The unstructured text to scan.
     * @param siftPattern The pattern defining the "needle" we are looking for.
     * @return A list of all matching substrings found.
     */
    private List<String> grep(String text, SiftPattern siftPattern) {
        List<String> results = new ArrayList<>();

        // We do not enforce anchors (^ or $) because we are searching INSIDE the text.
        String regex = siftPattern.shake();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    // =================================================================================
    // 4. THE USE CASES
    // =================================================================================
    @Test
    @DisplayName("Should extract only User Actions from the wall of text")
    void mineUserActions() {
        System.out.println("--- Mining User Actions ---");

        // Define the structural literals of our log lines
        SiftPattern userLabel = literal("User: ");
        SiftPattern arrow     = literal(" -> ");
        SiftPattern action    = literal("Action: ");

        // Define complex domain objects using our Grammar
        SiftPattern validUsername = LogGrammar.quoted(anywhere().oneOrMore().letters());
        SiftPattern validAction   = LogGrammar.braced(anywhere().followedBy(action).followedBy().oneOrMore().letters());

        // Build the extraction query: "Find a User... followed by an arrow... followed by an Action"
        SiftPattern userActionQuery = anywhere()
                .followedBy(userLabel)
                .followedBy(validUsername)
                .followedBy(arrow)
                .followedBy(validAction);

        List<String> matches = grep(SERVER_LOGS, userActionQuery);

        matches.forEach(System.out::println);

        // Assert that we correctly ignored system logs and noise
        assert(matches.size() == 4); // Expected: Mirko, HackBot, Alice, Bob
        assert(matches.get(0).contains("'Mirko'"));
        assert(matches.get(1).contains("'HackBot'"));
    }

    @Test
    @DisplayName("Should extract only Login actions")
    void mineSpecificLogins() {
        System.out.println("\n--- Mining Only Logins ---");

        SiftPattern userLabel   = literal("User: ");
        SiftPattern arrow       = literal(" -> ");
        SiftPattern actionLogin = literal("Action: Login"); // We are looking for this specific literal

        // Define the specific target action
        SiftPattern loginAction = LogGrammar.braced(anywhere().followedBy(actionLogin));

        // Build the specific query
        SiftPattern loginQuery = anywhere()
                .followedBy(userLabel)
                .followedBy(LogGrammar.quoted(anywhere().oneOrMore().letters()))
                .followedBy(arrow)
                .followedBy(loginAction);

        List<String> matches = grep(SERVER_LOGS, loginQuery);

        matches.forEach(System.out::println);

        // Only one user performed a Login
        assert(matches.size() == 1);
        assert(matches.get(0).contains("'Mirko'"));
    }
}