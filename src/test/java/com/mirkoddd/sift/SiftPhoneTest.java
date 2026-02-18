package com.mirkoddd.sift;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.mirkoddd.sift.Sift.*;
import static com.mirkoddd.sift.SiftPatterns.*;

import com.mirkoddd.sift.dsl.SiftPattern;

@DisplayName("Phone Number Validation Tests")
class SiftPhoneTest {

    @Test
    @DisplayName("Should validate Italian phone numbers with various prefix formats")
    void testItalyPhone() {
        // 0. Define the prefix options: (+39 or 0039)
        SiftPattern italyPrefix = anyOf(
                literal("+39"),
                literal("0039")
        );

        // 1. Define the prefix block: may contain a space after the prefix
        SiftPattern prefixBlock = anywhere()
                .followedBy(italyPrefix)
                .withOptional(' ');

        // 2. Build the main regex
        // We use fromStart() and untilEnd() to ensure strict validation

        /*  Same but less readable
          String regexPhone = fromStart()
                          .optional().followedBy(prefixBlock)
                          .followedBy().exactly(10).digits()
                          .untilEnd()
                          .shake();
         */

        String regexPhone = fromStart()
                .withOptional(prefixBlock)
                .followedBy().exactly(10).digits()
                .untilEnd()
                .shake();

        // Matches
        assertTrue("+39 3331234567".matches(regexPhone));
        assertTrue("+393331234567".matches(regexPhone));
        assertTrue("0039 3331234567".matches(regexPhone));
        assertTrue("3331234567".matches(regexPhone));
        assertTrue("00393331234567".matches(regexPhone));

        // Non-Matches
        assertFalse(" 3331234567".matches(regexPhone), "Leading space not allowed");
        assertFalse("+39 333123456".matches(regexPhone), "Too short");
        assertFalse("+3 3331234567".matches(regexPhone), "Missing '9'");
        assertFalse("39 333123456".matches(regexPhone), "Missing + or 00");
        assertFalse("OO393331234567".matches(regexPhone), "Typo with letters O instead of zeros");
        assertFalse("+39 333 1234567".matches(regexPhone), "Too many spaces");
    }
}