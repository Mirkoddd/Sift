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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mirkoddd.sift.core.Sift.*;
import static com.mirkoddd.sift.core.SiftPatterns.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Advanced: Structured Log Parsing")
class SiftStructuredParsingTest {

    // =================================================================================
    // 1. DATA & MODEL
    // =================================================================================
    // Converted Text Block to Java 8 string concatenation
    private final String SERVER_LOGS =
            "[INFO] [2026-02-18] System: Booting up services... OK.\n" +
                    "[INFO] [2026-02-18] User: 'Mirko' -> {Action: Login} - IP: 192.168.1.1\n" +
                    "[WARN] [2026-02-18] Disk: Usage at 85%.\n" +
                    "garbage_data_noise_#$@#$\n" +
                    "[ERR] [2026-02-18] User: 'HackBot' -> {Action: Inject} - BLOCKED\n" +
                    "[INFO] [2026-02-19] User: 'Alice' -> {Action: Upload} - File: data.csv\n" +
                    "[DEBUG] Connection established.\n" +
                    "[INFO] [2026-02-18] User: 'Bob' -> {Action: Logout}\n";

    // Converted Record to Java 8 Class
    static class LogEntry {
        private final String timestamp;
        private final String user;
        private final String action;

        public LogEntry(String timestamp, String user, String action) {
            this.timestamp = timestamp;
            this.user = user;
            this.action = action;
        }

        public String timestamp() { return timestamp; }
        public String user() { return user; }
        public String action() { return action; }

        @Override
        public String toString() {
            return "LogEntry{" +
                    "timestamp='" + timestamp + '\'' +
                    ", user='" + user + '\'' +
                    ", action='" + action + '\'' +
                    '}';
        }
    }

    // =================================================================================
    // 2. GRAMMAR EXTENSION (Semantic & Elegant)
    // =================================================================================
    static class LogGrammar {

        /**
         * Matches the log header structure: [LEVEL] [TIMESTAMP]
         * Encapsulates the brackets and spacing.
         */
        static SiftPattern<SiftContext.Fragment> header(String level, SiftPattern<SiftContext.Fragment> timestampPattern) {
            return fromAnywhere()
                    .of(literal("[" + level + "]"))
                    .followedBy(literal(" ["))
                    .followedBy(timestampPattern)
                    .followedBy(literal("]"));
        }

        /**
         * Matches the user field: User: 'NAME'
         * Encapsulates the label, spacing and quotes.
         */
        static SiftPattern<SiftContext.Fragment> userField(SiftPattern<SiftContext.Fragment> namePattern) {
            return fromAnywhere()
                    .of(literal(" User: '"))
                    .followedBy(namePattern)
                    .followedBy('\'');
        }

        /**
         * Matches the action field: -> {Action: ACTION}
         * Encapsulates the arrow, braces and label.
         */
        static SiftPattern<SiftContext.Fragment> actionField(SiftPattern<SiftContext.Fragment> actionPattern) {
            return fromAnywhere()
                    .of(literal(" -> {Action: "))
                    .followedBy(actionPattern)
                    .followedBy('}');
        }
    }

    // =================================================================================
    // 3. PARSING ENGINE
    // =================================================================================
    private List<LogEntry> parse(String text, SiftPattern<SiftContext.Fragment> pattern) {
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

        SiftPattern<SiftContext.Fragment> datePattern = fromAnywhere()
                .exactly(4).digits().followedBy('-')
                .then().exactly(2).digits().followedBy('-')
                .then().exactly(2).digits();

        SiftPattern<SiftContext.Fragment> wordPattern = fromAnywhere().oneOrMore().letters();

        // --- SEMANTIC COMPOSITION ---
        // We compose the log structure using our Grammar methods.
        // No more ugly literals floating around in the main logic.

        // 1. Header: [INFO] [DATE] -> Capture Date
        SiftPattern<SiftContext.Fragment> headerPart = LogGrammar.header("INFO", capture(datePattern));

        // 2. User: User: 'NAME' -> Capture Name
        SiftPattern<SiftContext.Fragment> userPart = LogGrammar.userField(capture(wordPattern));

        // 3. Action: -> {Action: ACTION} -> Capture Action
        SiftPattern<SiftContext.Fragment> actionPart = LogGrammar.actionField(capture(wordPattern));

        // --- FINAL ASSEMBLY ---

        SiftPattern<SiftContext.Fragment> logParser = fromAnywhere()
                .of(headerPart)
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