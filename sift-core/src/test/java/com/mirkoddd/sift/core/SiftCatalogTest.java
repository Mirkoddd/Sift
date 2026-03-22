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

import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.constraints.UpperChars;

import java.util.UUID;

class SiftCatalogTest {

    @Test
    void shouldHavePrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<SiftCatalog> constructor = SiftCatalog.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                "Constructor of SiftCatalog should be private");
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    // -------------------------------------------------------------------------
    // UUID
    // -------------------------------------------------------------------------

    @Property
    void uuidPatternShouldAcceptAnyValidUUID(@ForAll("validUuids") String uuid) {
        try (SiftCompiledPattern pattern = SiftCatalog.uuid().sieve()) {
            assertTrue(pattern.matchesEntire(uuid), "Failed to match valid UUID: " + uuid);
        }
    }

    @Provide
    Arbitrary<String> validUuids() {
        return Arbitraries
                .randomValue(r -> UUID.randomUUID())
                .map(UUID::toString);
    }

    @Test
    void shouldValidateUuid() {
        try (SiftCompiledPattern uuidPattern = SiftCatalog.uuid().sieve()) {
            assertTrue(uuidPattern.matchesEntire("123e4567-e89b-12d3-a456-426614174000"));
            assertTrue(uuidPattern.matchesEntire("123E4567-E89B-12D3-A456-426614174000"));

            assertFalse(uuidPattern.matchesEntire("123e4567e89b12d3a456426614174000"), "Should reject UUIDs without dashes");
            assertFalse(uuidPattern.matchesEntire("123e4567-e89b-12d3-a456-42661417400g"), "Should reject invalid hex character");
            assertFalse(uuidPattern.matchesEntire("123e4567-e89b-12d3-a456-4266141740000"), "Should reject UUID too long");
            assertFalse(uuidPattern.matchesEntire("123e456-e89b-12d3-a456-426614174000"), "Should reject wrong segment length");
        }
    }

    // -------------------------------------------------------------------------
    // IPv4
    // -------------------------------------------------------------------------

    @Property
    void ipv4PatternShouldAcceptAllValidIPs(
            @ForAll @IntRange(max = 255) int o1,
            @ForAll @IntRange(max = 255) int o2,
            @ForAll @IntRange(max = 255) int o3,
            @ForAll @IntRange(max = 255) int o4
    ) {
        String ip = String.format("%d.%d.%d.%d", o1, o2, o3, o4);
        try (SiftCompiledPattern pattern = SiftCatalog.ipv4().sieve()) {
            assertTrue(pattern.matchesEntire(ip), "Failed on: " + ip);
        }
    }

    @Property
    void ipv4ShouldRejectInvalidOctets(
            @ForAll @IntRange(min = 256, max = 999) int badOctet,
            @ForAll @IntRange(max = 3) int pos
    ) {
        Integer[] octets = {127, 0, 0, 1};
        octets[pos] = badOctet;
        String invalidIp = String.format("%d.%d.%d.%d", octets[0], octets[1], octets[2], octets[3]);
        try (SiftCompiledPattern pattern = SiftCatalog.ipv4().sieve()) {
            assertFalse(pattern.matchesEntire(invalidIp), "Should have rejected: " + invalidIp);
        }
    }

    @Test
    void shouldValidateIpv4() {
        try (SiftCompiledPattern ipv4Pattern = SiftCatalog.ipv4().sieve()) {
            assertTrue(ipv4Pattern.matchesEntire("192.168.1.1"));
            assertTrue(ipv4Pattern.matchesEntire("255.255.255.255"));
            assertTrue(ipv4Pattern.matchesEntire("0.0.0.0"));
            assertTrue(ipv4Pattern.matchesEntire("10.0.0.1"));
            assertTrue(ipv4Pattern.matchesEntire("172.16.254.1"));

            assertFalse(ipv4Pattern.matchesEntire("256.168.1.1"), "Should reject octet > 255");
            assertFalse(ipv4Pattern.matchesEntire("192.168.1.999"), "Should reject octet > 255 at end");
            assertFalse(ipv4Pattern.matchesEntire("192.168.1"), "Should reject missing octets");
            assertFalse(ipv4Pattern.matchesEntire("192.168.1.1.1"), "Should reject too many octets");
            assertFalse(ipv4Pattern.matchesEntire("abc.def.ghi.jkl"), "Should reject non-numeric IPs");
        }
    }

    // -------------------------------------------------------------------------
    // MAC Address
    // -------------------------------------------------------------------------

    @Property
    void macAddressShouldAcceptValidColonSeparatedHexPairs(@ForAll("validMacsColon") String mac) {
        try (SiftCompiledPattern pattern = SiftCatalog.macAddress().sieve()) {
            assertTrue(pattern.matchesEntire(mac), "Failed on: " + mac);
        }
    }

    @Property
    void macAddressShouldAcceptValidHyphenSeparatedHexPairs(@ForAll("validMacsHyphen") String mac) {
        try (SiftCompiledPattern pattern = SiftCatalog.macAddress().sieve()) {
            assertTrue(pattern.matchesEntire(mac), "Failed on: " + mac);
        }
    }

    @Property
    void macAddressShouldRejectInvalidCharacters(@ForAll("validMacsColon") String mac) {
        // Replace a hex character with a non-hex one
        String invalidMac = mac.replace('a', 'G').replace('A', 'G');
        if (invalidMac.equals(mac)) return; // skip if no replacement happened
        try (SiftCompiledPattern pattern = SiftCatalog.macAddress().sieve()) {
            assertFalse(pattern.matchesEntire(invalidMac), "Should have rejected: " + invalidMac);
        }
    }

    @Property
    void macAddressShouldRejectMixedSeparators(@ForAll("validMacsColon") String colonMac) {
        // Replace the first colon with a hyphen to create a mixed-separator MAC
        String mixedMac = colonMac.replaceFirst(":", "-");
        try (SiftCompiledPattern pattern = SiftCatalog.macAddress().sieve()) {
            assertFalse(pattern.matchesEntire(mixedMac), "Should reject mixed separators: " + mixedMac);
        }
    }

    @Provide
    Arbitrary<String> validMacsColon() {
        Arbitrary<String> hexPair = Arbitraries.strings()
                .withCharRange('a', 'f').withCharRange('A', 'F').withCharRange('0', '9')
                .ofLength(2);
        return hexPair.list().ofSize(6).map(list -> String.join(":", list));
    }

    @Provide
    Arbitrary<String> validMacsHyphen() {
        Arbitrary<String> hexPair = Arbitraries.strings()
                .withCharRange('a', 'f').withCharRange('A', 'F').withCharRange('0', '9')
                .ofLength(2);
        return hexPair.list().ofSize(6).map(list -> String.join("-", list));
    }

    @Test
    void shouldValidateMacAddress() {
        try (SiftCompiledPattern macPattern = SiftCatalog.macAddress().sieve()) {
            assertTrue(macPattern.matchesEntire("00:1A:2B:3C:4D:5E"));
            assertTrue(macPattern.matchesEntire("00-1A-2B-3C-4D-5E"));
            assertTrue(macPattern.matchesEntire("00:1a:2b:3c:4d:5e"));

            assertFalse(macPattern.matchesEntire("00:1A:2B:3C:4D"), "Should reject too short MAC");
            assertFalse(macPattern.matchesEntire("00:1A:2B:3C:4D:5E:6F"), "Should reject too long MAC");
            assertFalse(macPattern.matchesEntire("001A2B3C4D5E"), "Should reject MAC without separators");
            assertFalse(macPattern.matchesEntire("00:1A:2B:3C:4D:5G"), "Should reject invalid hex character");
            assertFalse(macPattern.matchesEntire("00:1a-2b:3c:4d:5e"), "Should reject mixed separators");
        }
    }

    // -------------------------------------------------------------------------
    // Email
    // -------------------------------------------------------------------------

    @Property
    void emailPatternShouldAcceptValidEmails(
            @ForAll("validEmailUsers") String user,
            @ForAll("validDomains") String domain,
            @ForAll("validTlds") String tld
    ) {
        String email = user + "@" + domain + "." + tld;
        try (SiftCompiledPattern pattern = SiftCatalog.email().sieve()) {
            assertTrue(pattern.matchesEntire(email), "Failed to match email: " + email);
        }
    }

    @Property
    void emailShouldRejectMissingAtSymbol(@ForAll("validEmailUsers") String user, @ForAll("validDomains") String domain) {
        String invalidEmail = user + domain + ".com";
        try (SiftCompiledPattern pattern = SiftCatalog.email().sieve()) {
            assertFalse(pattern.matchesEntire(invalidEmail));
        }
    }

    @Provide
    Arbitrary<String> validEmailUsers() {
        return Arbitraries.strings().withCharRange('a', 'z').withCharRange('0', '9')
                .withChars('.', '+', '_')
                .ofMinLength(1).ofMaxLength(20)
                .filter(s -> !s.startsWith(".") && !s.endsWith("."));
    }

    @Provide
    Arbitrary<String> validDomains() {
        return Arbitraries.strings().withCharRange('a', 'z').withCharRange('0', '9')
                .withChars('-')
                .ofMinLength(1).ofMaxLength(15)
                .filter(s -> !s.startsWith("-") && !s.endsWith("-"));
    }

    @Provide
    Arbitrary<String> validTlds() {
        return Arbitraries.strings().withCharRange('a', 'z')
                .ofMinLength(2).ofMaxLength(6);
    }

    @Test
    void shouldValidateEmail() {
        try (SiftCompiledPattern emailPattern = SiftCatalog.email().sieve()) {
            assertTrue(emailPattern.matchesEntire("test@example.com"));
            assertTrue(emailPattern.matchesEntire("user.name+tag@domain.co.uk"));
            assertTrue(emailPattern.matchesEntire("123_456@test-domain.org"));

            assertFalse(emailPattern.matchesEntire("plainaddress"));
            assertFalse(emailPattern.matchesEntire("@missinguser.com"));
            assertFalse(emailPattern.matchesEntire("user@.com"));
            assertFalse(emailPattern.matchesEntire("user@domain.c"), "TLD too short");
        }
    }

    // -------------------------------------------------------------------------
    // Web URL
    // -------------------------------------------------------------------------

    @Property
    void webUrlPatternShouldAcceptValidUrls(
            @ForAll("protocols") String protocol,
            @ForAll("validDomains") String domain,
            @ForAll("validTlds") String tld,
            @ForAll("validPaths") String path
    ) {
        String url = protocol + "://" + domain + "." + tld + path;
        try (SiftCompiledPattern pattern = SiftCatalog.webUrl().sieve()) {
            assertTrue(pattern.matchesEntire(url), "Failed to match URL: " + url);
        }
    }

    @Provide
    Arbitrary<String> protocols() {
        return Arbitraries.of("http", "https");
    }

    @Provide
    Arbitrary<String> validPaths() {
        return Arbitraries.oneOf(
                Arbitraries.just(""),
                Arbitraries.just("/"),
                Arbitraries.strings().withCharRange('a', 'z').withChars('/', '?', '=', '&', '-', '_')
                        .ofMinLength(1).ofMaxLength(30)
                        .map(s -> s.startsWith("/") ? s : "/" + s)
        );
    }

    @Test
    void shouldValidateWebUrl() {
        try (SiftCompiledPattern urlPattern = SiftCatalog.webUrl().sieve()) {
            assertTrue(urlPattern.matchesEntire("https://www.google.com"));
            assertTrue(urlPattern.matchesEntire("http://example.org/path/to/resource?query=1"));
            assertTrue(urlPattern.matchesEntire("https://my-domain.net/"));

            assertFalse(urlPattern.matchesEntire("ftp://example.com"), "Should reject non http/https protocols");
            assertFalse(urlPattern.matchesEntire("https://example"), "Should reject missing TLD");
            assertFalse(urlPattern.matchesEntire("https://example.com/ space"), "Should reject spaces in path");
        }
    }

    // -------------------------------------------------------------------------
    // ISO Date
    // -------------------------------------------------------------------------

    @Property
    void isoDateShouldAcceptValidDates(
            @ForAll @IntRange(min = 1000, max = 9999) int y,
            @ForAll @IntRange(min = 1, max = 12) int m,
            @ForAll @IntRange(min = 1, max = 28) int d
    ) {
        String date = String.format("%04d-%02d-%02d", y, m, d);
        try (SiftCompiledPattern pattern = SiftCatalog.isoDate().sieve()) {
            assertTrue(pattern.matchesEntire(date));
        }
    }

    @Property
    void isoDateShouldRejectInvalidMonths(@ForAll @IntRange(min = 13, max = 99) int badMonth) {
        String date = String.format("2026-%02d-01", badMonth);
        try (SiftCompiledPattern pattern = SiftCatalog.isoDate().sieve()) {
            assertFalse(pattern.matchesEntire(date), "Should reject month: " + badMonth);
        }
    }

    @Test
    void shouldValidateIsoDate() {
        try (SiftCompiledPattern datePattern = SiftCatalog.isoDate().sieve()) {
            assertTrue(datePattern.matchesEntire("2026-03-02"));
            assertTrue(datePattern.matchesEntire("1999-12-31"));
            assertTrue(datePattern.matchesEntire("2000-01-01"));

            assertFalse(datePattern.matchesEntire("2026-13-01"), "Should reject invalid month 13");
            assertFalse(datePattern.matchesEntire("2026-00-01"), "Should reject invalid month 00");
            assertFalse(datePattern.matchesEntire("2026-01-32"), "Should reject invalid day 32");
            assertFalse(datePattern.matchesEntire("2026-01-00"), "Should reject invalid day 00");
            assertFalse(datePattern.matchesEntire("26-01-01"), "Should reject non-4-digit years");
        }
    }

    // -------------------------------------------------------------------------
    // IBAN
    // -------------------------------------------------------------------------

    @Property
    void ibanPatternShouldAcceptValidStructure(
            @ForAll @UpperChars @StringLength(2) String country,
            @ForAll @IntRange(min = 10, max = 99) int check,
            @ForAll("alphanumericBban") String bban
    ) {
        String iban = country + check + bban;
        try (SiftCompiledPattern pattern = SiftCatalog.iban().sieve()) {
            assertTrue(pattern.matchesEntire(iban), "Failed on IBAN: " + iban);
        }
    }

    @Provide
    Arbitrary<String> alphanumericBban() {
        // BBAN contains both letters and digits in real IBANs
        return Arbitraries.strings()
                .withCharRange('A', 'Z').withCharRange('0', '9')
                .ofMinLength(11).ofMaxLength(30);
    }

    @Test
    void shouldValidateIban() {
        try (SiftCompiledPattern ibanPattern = SiftCatalog.iban().sieve()) {
            // Italian IBAN (27 chars after IT)
            assertTrue(ibanPattern.matchesEntire("IT60X0542811101000000123456"));
            // German IBAN (22 chars after DE)
            assertTrue(ibanPattern.matchesEntire("DE89370400440532013000"));
            // UK IBAN
            assertTrue(ibanPattern.matchesEntire("GB29NWBK60161331926819"));

            assertFalse(ibanPattern.matchesEntire("1234567890"), "Should reject no country code");
            assertFalse(ibanPattern.matchesEntire("IT60X054"), "Should reject BBAN too short");
        }
    }

    // -------------------------------------------------------------------------
    // JWT
    // -------------------------------------------------------------------------

    @Property
    void jwtPatternShouldAcceptValidStructure(
            @ForAll("base64Url") String h,
            @ForAll("base64Url") String p,
            @ForAll("base64Url") String s
    ) {
        String jwt = String.format("%s.%s.%s", h, p, s);
        try (SiftCompiledPattern pattern = SiftCatalog.jwt().sieve()) {
            assertTrue(pattern.matchesEntire(jwt));
        }
    }

    @Provide
    Arbitrary<String> base64Url() {
        return Arbitraries.strings()
                .withCharRange('a', 'z').withCharRange('A', 'Z').withCharRange('0', '9')
                .withChars('-', '_')
                .ofMinLength(10).ofMaxLength(50);
    }

    @Test
    void shouldValidateJwt() {
        try (SiftCompiledPattern jwtPattern = SiftCatalog.jwt().sieve()) {
            assertTrue(jwtPattern.matchesEntire("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.abc123-_XYZ"));

            assertFalse(jwtPattern.matchesEntire("onlyone"), "Should reject single segment");
            assertFalse(jwtPattern.matchesEntire("two.parts"), "Should reject two segments");
            assertFalse(jwtPattern.matchesEntire("has.standard+base64/chars.here"), "Should reject standard Base64 chars + and /");
        }
    }

    // -------------------------------------------------------------------------
    // Credit Card
    // -------------------------------------------------------------------------

    @Property
    void creditCardShouldAcceptVisa(@ForAll("visaNumbers") String visa) {
        try (SiftCompiledPattern pattern = SiftCatalog.creditCard().sieve()) {
            assertTrue(pattern.matchesEntire(visa));
        }
    }

    @Property
    void creditCardShouldAcceptAmex(@ForAll("amexNumbers") String amex) {
        try (SiftCompiledPattern pattern = SiftCatalog.creditCard().sieve()) {
            assertTrue(pattern.matchesEntire(amex));
        }
    }

    @Property
    void creditCardShouldAcceptMastercardLegacy(@ForAll("mastercardLegacyNumbers") String mc) {
        try (SiftCompiledPattern pattern = SiftCatalog.creditCard().sieve()) {
            assertTrue(pattern.matchesEntire(mc));
        }
    }

    @Provide
    Arbitrary<String> visaNumbers() {
        return Arbitraries.strings().withCharRange('0', '9').ofLength(15).map(s -> "4" + s);
    }

    @Provide
    Arbitrary<String> amexNumbers() {
        Arbitrary<String> prefix = Arbitraries.of("34", "37");
        Arbitrary<String> body = Arbitraries.strings().withCharRange('0', '9').ofLength(13);
        return Combinators.combine(prefix, body).as((p, b) -> p + b);
    }

    @Provide
    Arbitrary<String> mastercardLegacyNumbers() {
        // Legacy 51-55 prefix
        Arbitrary<Integer> firstDigit = Arbitraries.integers().between(1, 5);
        Arbitrary<String> body = Arbitraries.strings().withCharRange('0', '9').ofLength(14);
        return Combinators.combine(firstDigit, body).as((d, b) -> "5" + d + b);
    }

    @Test
    void shouldValidateCreditCard() {
        try (SiftCompiledPattern ccPattern = SiftCatalog.creditCard().sieve()) {
            // Visa
            assertTrue(ccPattern.matchesEntire("4111111111111111"));
            // Amex
            assertTrue(ccPattern.matchesEntire("371449635398431"));
            assertTrue(ccPattern.matchesEntire("341449635398431"));
            // Mastercard legacy
            assertTrue(ccPattern.matchesEntire("5500005555555559"));
            assertTrue(ccPattern.matchesEntire("5105105105105100"));

            assertFalse(ccPattern.matchesEntire("1234567890123456"), "Should reject unknown prefix");
            assertFalse(ccPattern.matchesEntire("411111111111111"), "Should reject Visa too short");
            assertFalse(ccPattern.matchesEntire("41111111111111111"), "Should reject Visa too long");
        }
    }

    // -------------------------------------------------------------------------
    // Base64
    // -------------------------------------------------------------------------

    @Property
    void base64PatternShouldAcceptValidEncodingWithoutPadding(@ForAll("validBase64NoPadding") String b64) {
        try (SiftCompiledPattern pattern = SiftCatalog.base64().sieve()) {
            assertTrue(pattern.matchesEntire(b64), "Failed on Base64: " + b64);
        }
    }

    @Property
    void base64PatternShouldAcceptValidEncodingWithSinglePad(@ForAll("validBase64SinglePad") String b64) {
        try (SiftCompiledPattern pattern = SiftCatalog.base64().sieve()) {
            assertTrue(pattern.matchesEntire(b64), "Failed on Base64 with = padding: " + b64);
        }
    }

    @Property
    void base64PatternShouldAcceptValidEncodingWithDoublePad(@ForAll("validBase64DoublePad") String b64) {
        try (SiftCompiledPattern pattern = SiftCatalog.base64().sieve()) {
            assertTrue(pattern.matchesEntire(b64), "Failed on Base64 with == padding: " + b64);
        }
    }

    @Provide
    Arbitrary<String> validBase64NoPadding() {
        // Groups of 4 chars with no padding (length % 4 == 0)
        return Arbitraries.strings()
                .withCharRange('a', 'z').withCharRange('A', 'Z').withCharRange('0', '9')
                .withChars('+', '/')
                .ofMinLength(4).ofMaxLength(40)
                .filter(s -> s.length() % 4 == 0);
    }

    @Provide
    Arbitrary<String> validBase64SinglePad() {
        // Length % 4 == 3 → one padding char (=)
        return Arbitraries.strings()
                .withCharRange('a', 'z').withCharRange('A', 'Z').withCharRange('0', '9')
                .withChars('+', '/')
                .ofMinLength(3).ofMaxLength(39)
                .filter(s -> s.length() % 4 == 3)
                .map(s -> s + "=");
    }

    @Provide
    Arbitrary<String> validBase64DoublePad() {
        // Length % 4 == 2 → two padding chars (==)
        return Arbitraries.strings()
                .withCharRange('a', 'z').withCharRange('A', 'Z').withCharRange('0', '9')
                .withChars('+', '/')
                .ofMinLength(2).ofMaxLength(38)
                .filter(s -> s.length() % 4 == 2)
                .map(s -> s + "==");
    }

    @Test
    void shouldValidateBase64() {
        try (SiftCompiledPattern b64Pattern = SiftCatalog.base64().sieve()) {
            assertTrue(b64Pattern.matchesEntire("SGVsbG8gV29ybGQ="));      // "Hello World"
            assertTrue(b64Pattern.matchesEntire("dGVzdA=="));              // "test"
            assertTrue(b64Pattern.matchesEntire("YWJj"));                  // "abc" no padding

            assertFalse(b64Pattern.matchesEntire("SGVsbG8-V29ybGQ="), "Should reject Base64URL char -");
            assertFalse(b64Pattern.matchesEntire("SGVsbG8_V29ybGQ="), "Should reject Base64URL char _");
            assertFalse(b64Pattern.matchesEntire("abc"), "Should reject length not multiple of 4");
        }
    }

    // -------------------------------------------------------------------------
    // Base64URL
    // -------------------------------------------------------------------------

    @Property
    void base64UrlPatternShouldAcceptValidStrings(@ForAll("validBase64UrlStrings") String b64url) {
        try (SiftCompiledPattern pattern = SiftCatalog.base64Url().sieve()) {
            assertTrue(pattern.matchesEntire(b64url), "Failed on Base64URL: " + b64url);
        }
    }

    @Provide
    Arbitrary<String> validBase64UrlStrings() {
        return Arbitraries.strings()
                .withCharRange('a', 'z').withCharRange('A', 'Z').withCharRange('0', '9')
                .withChars('-', '_')
                .ofMinLength(1).ofMaxLength(50);
    }

    @Test
    void shouldValidateBase64Url() {
        try (SiftCompiledPattern b64urlPattern = SiftCatalog.base64Url().sieve()) {
            assertTrue(b64urlPattern.matchesEntire("eyJhbGciOiJIUzI1NiJ9"));
            assertTrue(b64urlPattern.matchesEntire("abc-def_123"));

            // Standard Base64 chars that are NOT valid in Base64URL
            assertFalse(b64urlPattern.matchesEntire("abc+def"), "Should reject standard Base64 char +");
            assertFalse(b64urlPattern.matchesEntire("abc/def"), "Should reject standard Base64 char /");
            assertFalse(b64urlPattern.matchesEntire("abc=="), "Should reject padding");
        }
    }
}