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
}