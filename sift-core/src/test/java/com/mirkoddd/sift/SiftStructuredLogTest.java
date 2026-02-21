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
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Advanced: Structured Log Parsing")
class SiftStructuredParsingTest {

    // =================================================================================
    // 1. DATA & MODEL
    // =================================================================================
    private final String SERVER_LOGS = """
            [INFO] [2026-02-18] System: Booting up services... OK.
            [INFO] [2026-02-18] User: 'Mirko' -> {Action: Login} - IP: 192.168.1.1
            [WARN] [2026-02-18] Disk: Usage at 85%.
            garbage_data_noise_#$@#$
            [ERR] [2026-02-18] User: 'HackBot' -> {Action: Inject} - BLOCKED
            [INFO] [2026-02-19] User: 'Alice' -> {Action: Upload} - File: data.csv
            [DEBUG] Connection established.
            [INFO] [2026-02-18] User: 'Bob' -> {Action: Logout}
            """;

    record LogEntry(String timestamp, String user, String action) {}

    // =================================================================================
    // 2. GRAMMAR EXTENSION (Semantic & Elegant)
    // =================================================================================
    static class LogGrammar {

        /**
         * Matches the log header structure: [LEVEL] [TIMESTAMP]
         * Encapsulates the brackets and spacing.
         */
        static SiftPattern header(String level, SiftPattern timestampPattern) {
            return anywhere()
                    .pattern(literal("[" + level + "]"))
                    .followedBy(literal(" ["))
                    .followedBy(timestampPattern)
                    .followedBy(literal("]"));
        }

        /**
         * Matches the user field: User: 'NAME'
         * Encapsulates the label, spacing and quotes.
         */
        static SiftPattern userField(SiftPattern namePattern) {
            return anywhere()
                    .pattern(literal(" User: '"))
                    .followedBy(namePattern)
                    .followedBy('\'');
        }

        /**
         * Matches the action field: -> {Action: ACTION}
         * Encapsulates the arrow, braces and label.
         */
        static SiftPattern actionField(SiftPattern actionPattern) {
            return anywhere()
                    .pattern(literal(" -> {Action: "))
                    .followedBy(actionPattern)
                    .followedBy('}');
        }
    }

    // =================================================================================
    // 3. PARSING ENGINE
    // =================================================================================
    private List<LogEntry> parse(String text, SiftPattern pattern) {
        List<LogEntry> list = new ArrayList<>();
        Pattern regex = Pattern.compile(pattern.shake());
        Matcher matcher = regex.matcher(text);

        while (matcher.find()) {
            list.add(new LogEntry(matcher.group(1), matcher.group(2), matcher.group(3)));
        }
        return list;
    }

    // =================================================================================
    // 4. THE TEST (Declarative)
    // =================================================================================
    @Test
    @DisplayName("Should extract Timestamp, User and Action into LogEntry objects")
    void parseLogToObjects() {
        System.out.println("--- Parsing Logs to Objects ---");

        // --- DEFINITION OF CORE PATTERNS ---

        SiftPattern datePattern = anywhere()
                .exactly(4).digits().followedBy('-')
                .then().exactly(2).digits().followedBy('-')
                .then().exactly(2).digits();

        SiftPattern wordPattern = anywhere().oneOrMore().letters();

        // --- SEMANTIC COMPOSITION ---
        // We compose the log structure using our Grammar methods.
        // No more ugly literals floating around in the main logic.

        // 1. Header: [INFO] [DATE] -> Capture Date
        SiftPattern headerPart = LogGrammar.header("INFO", capture(datePattern));

        // 2. User: User: 'NAME' -> Capture Name
        SiftPattern userPart = LogGrammar.userField(capture(wordPattern));

        // 3. Action: -> {Action: ACTION} -> Capture Action
        SiftPattern actionPart = LogGrammar.actionField(capture(wordPattern));

        // --- FINAL ASSEMBLY ---

        SiftPattern logParser = anywhere()
                .pattern(headerPart)
                .followedBy(userPart)
                .followedBy(actionPart);

        // --- EXECUTION ---
        List<LogEntry> entries = parse(SERVER_LOGS, logParser);

        // --- VERIFICATION ---
        entries.forEach(System.out::println);

        assertEquals(3, entries.size());

        LogEntry mirko = entries.get(0);
        assertEquals("2026-02-18", mirko.timestamp());
        assertEquals("Mirko", mirko.user());
        assertEquals("Login", mirko.action());

        LogEntry alice = entries.get(1);
        assertEquals("2026-02-19", alice.timestamp());
        assertEquals("Alice", alice.user());
        assertEquals("Upload", alice.action());
    }
}