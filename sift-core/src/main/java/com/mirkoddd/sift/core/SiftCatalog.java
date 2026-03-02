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

import com.mirkoddd.sift.core.dsl.SiftPattern;

/**
 * <h2>SiftCatalog - Ready-to-use Standard Patterns</h2>
 * A curated collection of highly optimized, ReDoS-safe regular expressions for common data formats.
 * <p>
 * These patterns can be used as standalone validators or seamlessly integrated into larger
 * {@link SiftBuilder} chains using {@code .pattern(SiftCatalog.xxx())}.
 *
 * @author Mirko Dimartino
 * @since 2.0.0
 */
public final class SiftCatalog {

    private SiftCatalog() {
        // Prevent instantiation
    }

    /**
     * Reusable definition of a single Hexadecimal character [a-fA-F0-9].
     */
    private static SiftPattern hexChar() {
        return SiftPatterns.anyOf(
                Sift.fromAnywhere().range('a', 'f'),
                Sift.fromAnywhere().range('A', 'F'),
                Sift.fromAnywhere().digits()
        );
    }

    /**
     * Matches a standard 128-bit UUID/GUID (Universally Unique Identifier).
     * <p>
     * Format: {@code 8-4-4-4-12} hexadecimal characters (case-insensitive).
     * Example: {@code 123e4567-e89b-12d3-a456-426614174000}
     *
     * @return A SiftPattern representing a UUID.
     */
    public static SiftPattern uuid() {
        return Sift.fromAnywhere()
                .exactly(8).pattern(hexChar())
                .followedBy('-')
                .then().exactly(4).pattern(hexChar())
                .followedBy('-')
                .then().exactly(4).pattern(hexChar())
                .followedBy('-')
                .then().exactly(4).pattern(hexChar())
                .followedBy('-')
                .then().exactly(12).pattern(hexChar())
                .preventBacktracking();
    }

    /**
     * Matches a valid IPv4 address.
     * <p>
     * Validates that each octet is strictly between 0 and 255.
     * Prevents matching invalid IPs like {@code 256.100.50.25}.
     *
     * @return A SiftPattern representing an IPv4 address.
     */
    public static SiftPattern ipv4() {
        // Octet logic: 25[0-5] OR 2[0-4][0-9] OR [01]?[0-9][0-9]?
        SiftPattern octet = SiftPatterns.anyOf(
                // 250-255
                Sift.fromAnywhere().character('2').followedBy('5').then().exactly(1).range('0', '5'),
                // 200-249
                Sift.fromAnywhere().character('2').then().exactly(1).range('0', '4').then().exactly(1).digits(),
                // 0-199
                Sift.fromAnywhere().optional().range('0', '1').then().exactly(1).digits().then().optional().digits()
        );

        return Sift.fromAnywhere()
                .pattern(octet)
                .followedBy('.')
                .then().exactly(1).pattern(octet)
                .followedBy('.')
                .then().exactly(1).pattern(octet)
                .followedBy('.')
                .then().exactly(1).pattern(octet)
                .preventBacktracking();
    }

    /**
     * Matches a standard MAC address (Media Access Control address).
     * <p>
     * Supports both colon ({@code :}) and hyphen ({@code -}) separators,
     * but strictly prevents mixing them within the same address.
     *
     * @return A SiftPattern representing a MAC address.
     */
    public static SiftPattern macAddress() {
        SiftPattern hexPair = Sift.fromAnywhere()
                .exactly(2).pattern(hexChar());

        SiftPattern colonGroup = SiftPatterns.group(SiftPatterns.literal(":"), hexPair);
        SiftPattern hyphenGroup = SiftPatterns.group(SiftPatterns.literal("-"), hexPair);

        SiftPattern colonSeparated = Sift.fromAnywhere()
                .pattern(hexPair)
                .then().exactly(5).pattern(colonGroup);

        SiftPattern hyphenSeparated = Sift.fromAnywhere()
                .pattern(hexPair)
                .then().exactly(5).pattern(hyphenGroup);

        return SiftPatterns.anyOf(colonSeparated, hyphenSeparated)
                .preventBacktracking();
    }

    /**
     * Matches a standard Email address.
     * <p>
     * Validates typical format {@code local@domain.tld}, safely supporting
     * subdomains, plus-addressing, and common special characters.
     *
     * @return A SiftPattern representing an Email.
     */
    public static SiftPattern email() {
        SiftPattern localPart = Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('.', '_', '%', '+', '-');

        SiftPattern domainPart = Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('.', '-');

        SiftPattern tld = Sift.fromAnywhere()
                .between(2, 63).letters();

        return Sift.fromAnywhere()
                .pattern(localPart)
                .followedBy('@')
                .then().exactly(1).pattern(domainPart)
                .followedBy('.')
                .then().exactly(1).pattern(tld)
                .preventBacktracking();
    }

    /**
     * Matches a standard HTTP/HTTPS URL.
     * <p>
     * Enforces the protocol, a valid domain/TLD structure, and allows an optional path.
     *
     * @return A SiftPattern representing a Web URL.
     */
    public static SiftPattern webUrl() {
        SiftPattern protocol = SiftPatterns.anyOf(
                SiftPatterns.literal("http://"),
                SiftPatterns.literal("https://")
        );

        SiftPattern domain = Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('.', '-');

        SiftPattern tld = Sift.fromAnywhere()
                .between(2, 63).letters();

        // Optional path allowing anything except whitespace and angle brackets/quotes
        SiftPattern pathChar = SiftPatterns.anythingBut(" \t\n\r<>\"'");

        return Sift.fromAnywhere()
                .pattern(protocol)
                .then().exactly(1).pattern(domain)
                .followedBy('.')
                .then().exactly(1).pattern(tld)
                .then().zeroOrMore().pattern(pathChar)
                .preventBacktracking();
    }

    /**
     * Matches a structural ISO 8601 Date format (YYYY-MM-DD).
     * <p>
     * <b>Important:</b> This pattern performs <i>syntactic</i> validation only. It ensures the string
     * matches the shape of a valid date (4 digits for year, 01-12 for month, 01-31 for day).
     * It does <b>not</b> perform <i>semantic</i> calendar validation (e.g., it will structurally accept "2026-02-31").
     * Developers should pass the matched string to a dedicated date parser like
     * {@link java.time.LocalDate#parse(CharSequence)} for true leap-year and month-length validation.
     *
     * @return A SiftPattern representing the structural format of an ISO Date.
     */
    public static SiftPattern isoDate() {
        SiftPattern year = Sift.fromAnywhere().exactly(4).digits();

        SiftPattern month = SiftPatterns.anyOf(
                Sift.fromAnywhere().character('0').then().exactly(1).range('1', '9'),
                Sift.fromAnywhere().character('1').then().exactly(1).range('0', '2')
        );

        SiftPattern day = SiftPatterns.anyOf(
                Sift.fromAnywhere().character('0').then().exactly(1).range('1', '9'),
                Sift.fromAnywhere().range('1', '2').then().exactly(1).digits(),
                Sift.fromAnywhere().character('3').then().exactly(1).range('0', '1')
        );

        return Sift.fromAnywhere()
                .pattern(year)
                .followedBy('-')
                .then().exactly(1).pattern(month)
                .followedBy('-')
                .then().exactly(1).pattern(day)
                .preventBacktracking();
    }
}