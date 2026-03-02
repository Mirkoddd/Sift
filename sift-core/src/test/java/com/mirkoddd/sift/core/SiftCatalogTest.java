package com.mirkoddd.sift.core;

import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;

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
        Pattern uuidPattern = SiftCatalog.uuid().sieve();

        // Valid UUIDs (lowercase and uppercase)
        assertTrue(uuidPattern.matcher("123e4567-e89b-12d3-a456-426614174000").matches());
        assertTrue(uuidPattern.matcher("123E4567-E89B-12D3-A456-426614174000").matches());

        // Invalid UUIDs
        assertFalse(uuidPattern.matcher("123e4567e89b12d3a456426614174000").matches(), "Should reject UUIDs without dashes");
        assertFalse(uuidPattern.matcher("123e4567-e89b-12d3-a456-42661417400g").matches(), "Should reject invalid hex character 'g'");
        assertFalse(uuidPattern.matcher("123e4567-e89b-12d3-a456-4266141740000").matches(), "Should reject UUID that is too long");
        assertFalse(uuidPattern.matcher("123e456-e89b-12d3-a456-426614174000").matches(), "Should reject UUID with wrong segment length");
    }

    @Test
    void shouldValidateIpv4() {
        Pattern ipv4Pattern = SiftCatalog.ipv4().sieve();

        // Valid IPv4 Addresses
        assertTrue(ipv4Pattern.matcher("192.168.1.1").matches());
        assertTrue(ipv4Pattern.matcher("255.255.255.255").matches());
        assertTrue(ipv4Pattern.matcher("0.0.0.0").matches());
        assertTrue(ipv4Pattern.matcher("10.0.0.1").matches());
        assertTrue(ipv4Pattern.matcher("172.16.254.1").matches());

        // Invalid IPv4 Addresses
        assertFalse(ipv4Pattern.matcher("256.168.1.1").matches(), "Should reject octet > 255");
        assertFalse(ipv4Pattern.matcher("192.168.1.999").matches(), "Should reject octet > 255 at the end");
        assertFalse(ipv4Pattern.matcher("192.168.1").matches(), "Should reject IPs with missing octets");
        assertFalse(ipv4Pattern.matcher("192.168.1.1.1").matches(), "Should reject IPs with too many octets");
        assertFalse(ipv4Pattern.matcher("abc.def.ghi.jkl").matches(), "Should reject non-numeric IPs");
    }

    @Test
    void shouldValidateMacAddress() {
        Pattern macPattern = SiftCatalog.macAddress().sieve();

        // Valid MAC Addresses (colon, hyphen, uppercase, lowercase)
        assertTrue(macPattern.matcher("00:1A:2B:3C:4D:5E").matches());
        assertTrue(macPattern.matcher("00-1A-2B-3C-4D-5E").matches());
        assertTrue(macPattern.matcher("00:1a:2b:3c:4d:5e").matches());

        // Invalid MAC Addresses
        assertFalse(macPattern.matcher("00:1A:2B:3C:4D").matches(), "Should reject too short MAC");
        assertFalse(macPattern.matcher("00:1A:2B:3C:4D:5E:6F").matches(), "Should reject too long MAC");
        assertFalse(macPattern.matcher("001A2B3C4D5E").matches(), "Should reject MAC without separators");
        assertFalse(macPattern.matcher("00:1A:2B:3C:4D:5G").matches(), "Should reject invalid hex character 'G'");
        assertFalse(macPattern.matcher("00:1a-2b:3c:4d:5e").matches(), "Should reject mixed separators");
    }
}