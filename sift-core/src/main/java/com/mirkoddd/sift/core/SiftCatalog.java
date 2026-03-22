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

import static com.mirkoddd.sift.core.Sift.exactly;
import static com.mirkoddd.sift.core.SiftPatterns.anyOf;

import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;

/**
 * <h2>SiftCatalog - Ready-to-use Standard Patterns</h2>
 * A curated collection of highly optimized, ReDoS-safe regular expressions for common data formats.
 * <p>
 * These patterns can be used as standalone validators or seamlessly integrated into larger
 * {@link SiftPattern} chains using {@code .of(SiftCatalog.xxx())}.
 *
 * @author Mirko Dimartino
 * @since 2.0.0
 */
public final class SiftCatalog {

    private SiftCatalog() {
        // Prevent instantiation
    }

    /**
     * Matches a standard 128-bit UUID/GUID (Universally Unique Identifier).
     * <p>
     * Format: {@code 8-4-4-4-12} hexadecimal characters (case-insensitive).
     * Example: {@code 123e4567-e89b-12d3-a456-426614174000}
     *
     * @return A SiftPattern representing a UUID.
     */
    public static SiftPattern<Fragment> uuid() {
        return Sift.fromAnywhere()
                .exactly(8).hexDigits()
                .followedBy('-')
                .then().exactly(4).hexDigits()
                .followedBy('-')
                .then().exactly(4).hexDigits()
                .followedBy('-')
                .then().exactly(4).hexDigits()
                .followedBy('-')
                .then().exactly(12).hexDigits()
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
    public static SiftPattern<Fragment> ipv4() {
        // Octet logic: 25[0-5] OR 2[0-4][0-9] OR [01]?[0-9][0-9]?
        SiftPattern<Fragment> octet = anyOf(
                // 250-255
                Sift.fromAnywhere().character('2').followedBy('5').then().exactly(1).range('0', '5'),
                // 200-249
                Sift.fromAnywhere().character('2').then().exactly(1).range('0', '4').then().exactly(1).digits(),
                // 0-199
                Sift.fromAnywhere().optional().range('0', '1').then().exactly(1).digits().then().optional().digits()
        );

        return Sift.fromAnywhere()
                .of(octet)
                .followedBy('.')
                .then().exactly(1).of(octet)
                .followedBy('.')
                .then().exactly(1).of(octet)
                .followedBy('.')
                .then().exactly(1).of(octet)
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
    public static SiftPattern<Fragment> macAddress() {
        SiftPattern<Fragment> hexPair = Sift.fromAnywhere()
                .exactly(2).hexDigits();

        SiftPattern<Fragment> colonGroup = SiftPatterns.group(SiftPatterns.literal(":"), hexPair);
        SiftPattern<Fragment> hyphenGroup = SiftPatterns.group(SiftPatterns.literal("-"), hexPair);

        SiftPattern<Fragment> colonSeparated = Sift.fromAnywhere()
                .of(hexPair)
                .then().exactly(5).of(colonGroup);

        SiftPattern<Fragment> hyphenSeparated = Sift.fromAnywhere()
                .of(hexPair)
                .then().exactly(5).of(hyphenGroup);

        return anyOf(colonSeparated, hyphenSeparated)
                .preventBacktracking();
    }

    /**
     * Matches a standard, everyday Email address.
     * <p>
     * <b>Important:</b> This pattern provides a pragmatic validation for the vast majority of standard email formats
     * (e.g., {@code local@domain.tld}). It safely supports subdomains, plus-addressing, and common special characters.
     * <p>
     * It intentionally makes simplifications and does <b>not</b> support extreme edge-cases from RFC 5322 such as:
     * <ul>
     * <li>Quoted local parts (e.g., {@code "user name"@domain.com})</li>
     * <li>IP addresses enclosed in square brackets (e.g., {@code user@[192.168.2.1]})</li>
     * <li>Internationalized Domain Names (IDN) requiring Punycode</li>
     * </ul>
     * For absolute, pedantic RFC compliance, a dedicated email parsing library is recommended.
     *
     * @return A SiftPattern representing a practical, ReDoS-safe Email format.
     */
    public static SiftPattern<Fragment> email() {
        SiftPattern<Fragment> localPart = Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('.', '_', '%', '+', '-');

        SiftPattern<Fragment> domainPart = Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('.', '-');

        SiftPattern<Fragment> tld = Sift.fromAnywhere()
                .between(2, 63).letters();

        return Sift.fromAnywhere()
                .of(localPart)
                .followedBy('@')
                .then().exactly(1).of(domainPart)
                .followedBy('.')
                .then().exactly(1).of(tld)
                .preventBacktracking();
    }

    /**
     * Matches a standard HTTP/HTTPS URL.
     * <p>
     * Enforces the protocol, a valid domain/TLD structure, and allows an optional path.
     *
     * @return A SiftPattern representing a Web URL.
     */
    public static SiftPattern<Fragment> webUrl() {
        SiftPattern<Fragment> protocol = anyOf(
                SiftPatterns.literal("http://"),
                SiftPatterns.literal("https://")
        );

        SiftPattern<Fragment> domain = Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('.', '-');

        SiftPattern<Fragment> tld = Sift.fromAnywhere()
                .between(2, 63).letters();

        // Optional path allowing anything except whitespace and angle brackets/quotes
        SiftPattern<Fragment> pathChar = SiftPatterns.anythingBut(" \t\n\r<>\"'");

        return Sift.fromAnywhere()
                .of(protocol)
                .then().exactly(1).of(domain)
                .followedBy('.')
                .then().exactly(1).of(tld)
                .then().zeroOrMore().of(pathChar)
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
    public static SiftPattern<Fragment> isoDate() {
        SiftPattern<Fragment> year = Sift.fromAnywhere().exactly(4).digits();

        SiftPattern<Fragment> month = anyOf(
                Sift.fromAnywhere().character('0').then().exactly(1).range('1', '9'),
                Sift.fromAnywhere().character('1').then().exactly(1).range('0', '2')
        );

        SiftPattern<Fragment> day = anyOf(
                Sift.fromAnywhere().character('0').then().exactly(1).range('1', '9'),
                Sift.fromAnywhere().range('1', '2').then().exactly(1).digits(),
                Sift.fromAnywhere().character('3').then().exactly(1).range('0', '1')
        );

        return Sift.fromAnywhere()
                .of(year)
                .followedBy('-')
                .then().exactly(1).of(month)
                .followedBy('-')
                .then().exactly(1).of(day)
                .preventBacktracking();
    }

    /**
     * Matches a structural International Bank Account Number (IBAN).
     * <p>
     * <b>Important:</b> This pattern performs <i>syntactic</i> validation of the IBAN format.
     * It ensures the string matches the international standard shape:
     * <ul>
     * <li>2 letters for the Country Code</li>
     * <li>2 digits for the Check Digits</li>
     * <li>Between 11 and 30 alphanumeric characters for the Basic Bank Account Number (BBAN)</li>
     * </ul>
     * <p>
     * It does <b>not</b> perform <i>semantic</i> validation, such as:
     * <ul>
     * <li>Verifying if the Country Code is a valid ISO 3166-1 alpha-2 code</li>
     * <li>Calculating the MOD-97 check digits to verify the IBAN's integrity</li>
     * <li>Enforcing country-specific BBAN lengths (e.g., exactly 27 for Italy, 22 for Germany)</li>
     * </ul>
     * For financial-grade validation, the matched string should be processed by a dedicated
     * IBAN validation library.
     *
     * @return A SiftPattern representing the structural format of an IBAN.
     */
    public static SiftPattern<Fragment> iban() {
        return Sift.fromAnywhere()
                .exactly(2).letters()                   // Country Code
                .then().exactly(2).digits()             // Check Digits
                .then().between(11, 30).alphanumeric()  // Basic Bank Account Number
                .preventBacktracking();
    }

    /**
     * Matches a structural JSON Web Token (JWT).
     * <p>
     * Verifies the three-part structure (header.payload.signature) where each part
     * is a valid Base64Url encoded string.
     * <p>
     * <b>Note:</b> Each segment uses the Base64URL alphabet ({@code [a-zA-Z0-9\-_]}),
     * which differs from standard Base64 ({@code +} and {@code /} are replaced by
     * {@code -} and {@code _}) and does not use padding ({@code =}).
     *
     * @return A SiftPattern representing a JWT structure.
     */
    public static SiftPattern<Fragment> jwt() {
        SiftPattern<Fragment> b64UrlPart = Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('-', '_');

        return Sift.fromAnywhere()
                .of(b64UrlPart)                         // Header
                .followedBy('.')
                .then().exactly(1).of(b64UrlPart)       // Payload
                .followedBy('.')
                .then().exactly(1).of(b64UrlPart)       // Signature
                .preventBacktracking();
    }

    /**
     * Matches major Credit Card formats (Visa, Mastercard, American Express).
     * <p>
     * <b>Important:</b> Validates structural length and common BIN prefixes only.
     * It does <b>not</b> perform Luhn algorithm checksum validation.
     * <ul>
     * <li><b>Visa:</b> Starts with 4, length 16.</li>
     * <li><b>Mastercard:</b> Starts with 51-55 (legacy) or 2x (modern 2221-2720 range).
     *     The modern range uses a structural approximation ({@code 2[2-6]XXXXXXXXXXXXXX})
     *     that may accept a small number of prefixes outside the strict 2221-2720 boundaries
     *     (e.g., 2200-2220 and 2721-2699). For strict BIN validation, use a dedicated
     *     payment library.</li>
     * <li><b>American Express:</b> Starts with 34 or 37, length 15.</li>
     * </ul>
     *
     * @return A SiftPattern representing common credit card formats.
     */
    public static SiftPattern<Fragment> creditCard() {
        SiftPattern<Fragment> visa = Sift.fromAnywhere()
                .character('4')
                .then().exactly(15).digits();

        SiftPattern<Fragment> amex = Sift.fromAnywhere()
                .of(anyOf(SiftPatterns.literal("34"), SiftPatterns.literal("37")))
                .then().exactly(13).digits();

        SiftPattern<Fragment> mastercard = anyOf(
                // Legacy 51-55
                Sift.fromAnywhere().character('5').then().range('1', '5').then().exactly(14).digits(),
                // Modern 2221-2720 (structural approximation: 2[2-6]XXXXXXXXXXXXXX)
                Sift.fromAnywhere().character('2').then().range('2', '6').then().exactly(14).digits()
        );

        return anyOf(visa, amex, mastercard)
                .preventBacktracking();
    }

    /**
     * Matches a standard Base64 encoded string (RFC 4648).
     * <p>
     * Validates that the string uses the standard Base64 alphabet
     * ({@code A-Z}, {@code a-z}, {@code 0-9}, {@code +}, {@code /}).
     * The total length must be a multiple of 4. Padding ({@code =} or {@code ==})
     * is required only when the encoded data does not align to a 3-byte boundary
     * (i.e., when the number of Base64 characters before padding is not a multiple of 4).
     * <p>
     * <b>Note:</b> This pattern matches <b>standard Base64</b>, not Base64URL.
     * Base64URL replaces {@code +} with {@code -} and {@code /} with {@code _},
     * and does not use padding. For Base64URL validation (e.g., individual JWT segments),
     * use {@link #base64Url()} instead.
     *
     * @return A SiftPattern representing a standard Base64 encoded string.
     */
    public static SiftPattern<Fragment> base64() {
        SiftPattern<Fragment> b64Chars = Sift.fromAnywhere()
                .alphanumeric().including('+', '/');

        SiftPattern<Fragment> fullGroup = SiftPatterns.group(exactly(4).of(b64Chars));

        SiftPattern<Fragment> singlePad = Sift.fromAnywhere()
                .exactly(3).of(b64Chars)
                .followedBy(SiftPatterns.literal("="));

        SiftPattern<Fragment> doublePad = Sift.fromAnywhere()
                .exactly(2).of(b64Chars)
                .followedBy(SiftPatterns.literal("=="));

        // Case 1: zero or more full groups + mandatory partial group with padding
        SiftPattern<Fragment> withPadding = Sift.fromAnywhere()
                .zeroOrMore().of(fullGroup)
                .then().exactly(1).of(anyOf(singlePad, doublePad));

        // Case 2: one or more full groups, no padding
        SiftPattern<Fragment> noPadding = Sift.fromAnywhere()
                .oneOrMore().of(fullGroup);

        return anyOf(withPadding, noPadding)
                .preventBacktracking();
    }

    /**
     * Matches a Base64URL encoded string (RFC 4648 §5).
     * <p>
     * Base64URL is a URL-safe variant of Base64 that replaces {@code +} with {@code -}
     * and {@code /} with {@code _}, and omits padding ({@code =}).
     * It is used in contexts where standard Base64 characters would require percent-encoding,
     * such as JWT segments, OAuth tokens, and URL-safe identifiers.
     * <p>
     * <b>Note:</b> For standard Base64 with padding, use {@link #base64()} instead.
     *
     * @return A SiftPattern representing a Base64URL encoded string.
     */
    public static SiftPattern<Fragment> base64Url() {
        return Sift.fromAnywhere()
                .oneOrMore().alphanumeric().including('-', '_')
                .preventBacktracking();
    }
}