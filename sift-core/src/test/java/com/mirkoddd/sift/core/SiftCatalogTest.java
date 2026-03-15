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

class SiftCatalogTest {

    @Test
    void shouldHavePrivateConstructor() throws Exception {
        // Retrieve the private constructor via Reflection
        java.lang.reflect.Constructor<SiftCatalog> constructor = SiftCatalog.class.getDeclaredConstructor();

        // Assert that it is indeed private
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                "Constructor of SiftCatalog should be private");

        // Force accessibility and invoke it to satisfy coverage tools
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void shouldValidateUuid() {
        SiftCompiledPattern uuidPattern = SiftCatalog.uuid().sieve();

        // Valid UUIDs (lowercase and uppercase)
        assertTrue(uuidPattern.matchesEntire("123e4567-e89b-12d3-a456-426614174000"));
        assertTrue(uuidPattern.matchesEntire("123E4567-E89B-12D3-A456-426614174000"));

        // Invalid UUIDs
        assertFalse(uuidPattern.matchesEntire("123e4567e89b12d3a456426614174000"), "Should reject UUIDs without dashes");
        assertFalse(uuidPattern.matchesEntire("123e4567-e89b-12d3-a456-42661417400g"), "Should reject invalid hex character 'g'");
        assertFalse(uuidPattern.matchesEntire("123e4567-e89b-12d3-a456-4266141740000"), "Should reject UUID that is too long");
        assertFalse(uuidPattern.matchesEntire("123e456-e89b-12d3-a456-426614174000"), "Should reject UUID with wrong segment length");
    }

    @Test
    void shouldValidateIpv4() {
        SiftCompiledPattern ipv4Pattern = SiftCatalog.ipv4().sieve();

        // Valid IPv4 Addresses
        assertTrue(ipv4Pattern.matchesEntire("192.168.1.1"));
        assertTrue(ipv4Pattern.matchesEntire("255.255.255.255"));
        assertTrue(ipv4Pattern.matchesEntire("0.0.0.0"));
        assertTrue(ipv4Pattern.matchesEntire("10.0.0.1"));
        assertTrue(ipv4Pattern.matchesEntire("172.16.254.1"));

        // Invalid IPv4 Addresses
        assertFalse(ipv4Pattern.matchesEntire("256.168.1.1"), "Should reject octet > 255");
        assertFalse(ipv4Pattern.matchesEntire("192.168.1.999"), "Should reject octet > 255 at the end");
        assertFalse(ipv4Pattern.matchesEntire("192.168.1"), "Should reject IPs with missing octets");
        assertFalse(ipv4Pattern.matchesEntire("192.168.1.1.1"), "Should reject IPs with too many octets");
        assertFalse(ipv4Pattern.matchesEntire("abc.def.ghi.jkl"), "Should reject non-numeric IPs");
    }

    @Test
    void shouldValidateMacAddress() {
        SiftCompiledPattern macPattern = SiftCatalog.macAddress().sieve();

        // Valid MAC Addresses (colon, hyphen, uppercase, lowercase)
        assertTrue(macPattern.matchesEntire("00:1A:2B:3C:4D:5E"));
        assertTrue(macPattern.matchesEntire("00-1A-2B-3C-4D-5E"));
        assertTrue(macPattern.matchesEntire("00:1a:2b:3c:4d:5e"));

        // Invalid MAC Addresses
        assertFalse(macPattern.matchesEntire("00:1A:2B:3C:4D"), "Should reject too short MAC");
        assertFalse(macPattern.matchesEntire("00:1A:2B:3C:4D:5E:6F"), "Should reject too long MAC");
        assertFalse(macPattern.matchesEntire("001A2B3C4D5E"), "Should reject MAC without separators");
        assertFalse(macPattern.matchesEntire("00:1A:2B:3C:4D:5G"), "Should reject invalid hex character 'G'");
        assertFalse(macPattern.matchesEntire("00:1a-2b:3c:4d:5e"), "Should reject mixed separators");
    }

    @Test
    void shouldValidateEmail() {
        SiftCompiledPattern emailPattern = SiftCatalog.email().sieve();

        // Valid
        assertTrue(emailPattern.matchesEntire("test@example.com"));
        assertTrue(emailPattern.matchesEntire("user.name+tag@domain.co.uk"));
        assertTrue(emailPattern.matchesEntire("123_456@test-domain.org"));

        // Invalid
        assertFalse(emailPattern.matchesEntire("plainaddress"));
        assertFalse(emailPattern.matchesEntire("@missinguser.com"));
        assertFalse(emailPattern.matchesEntire("user@.com"));
        assertFalse(emailPattern.matchesEntire("user_name@.com"), "underscore not allowed");
        assertFalse(emailPattern.matchesEntire("user@domain.c"), "TLD too short");
    }

    @Test
    void shouldValidateWebUrl() {
        SiftCompiledPattern urlPattern = SiftCatalog.webUrl().sieve();

        // Valid
        assertTrue(urlPattern.matchesEntire("https://www.google.com"));
        assertTrue(urlPattern.matchesEntire("http://example.org/path/to/resource?query=1"));
        assertTrue(urlPattern.matchesEntire("https://my-domain.net/"));

        // Invalid
        assertFalse(urlPattern.matchesEntire("ftp://example.com"), "Should reject non http/https protocols");
        assertFalse(urlPattern.matchesEntire("https://example"), "Should reject missing TLD");
        assertFalse(urlPattern.matchesEntire("https://example.com/ space"), "Should reject spaces in path");
    }

    @Test
    void shouldValidateIsoDate() {
        SiftCompiledPattern datePattern = SiftCatalog.isoDate().sieve();

        // Valid
        assertTrue(datePattern.matchesEntire("2026-03-02"));
        assertTrue(datePattern.matchesEntire("1999-12-31"));
        assertTrue(datePattern.matchesEntire("2000-01-01"));

        // Invalid
        assertFalse(datePattern.matchesEntire("2026-13-01"), "Should reject invalid month 13");
        assertFalse(datePattern.matchesEntire("2026-00-01"), "Should reject invalid month 00");
        assertFalse(datePattern.matchesEntire("2026-01-32"), "Should reject invalid day 32");
        assertFalse(datePattern.matchesEntire("2026-01-00"), "Should reject invalid day 00");
        assertFalse(datePattern.matchesEntire("26-01-01"), "Should reject non-4-digit years");
    }
}