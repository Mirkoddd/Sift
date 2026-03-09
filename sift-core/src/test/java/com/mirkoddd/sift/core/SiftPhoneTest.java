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

import static org.junit.jupiter.api.Assertions.*;
import static com.mirkoddd.sift.core.Sift.*;
import static com.mirkoddd.sift.core.SiftPatterns.*;

import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;

@DisplayName("Phone Number Validation Tests")
class SiftPhoneTest {

    @Test
    @DisplayName("Should validate Italian phone numbers with various prefix formats")
    void testItalyPhone() {
        // 0. Define the prefix options: (+39 or 0039)
        SiftPattern<Fragment> italyPrefix = anyOf(
                literal("+39"),
                literal("0039")
        );

        // 1. Define the prefix block: may contain a space after the prefix
        SiftPattern<Fragment> prefixBlock = fromAnywhere()
                .of(italyPrefix)
                .then().optional().whitespace();

        // 2. Build the main regex
        // We use fromStart() and andNothingElse() to ensure strict validation

        String regexPhone = fromStart()
                .optional().of(prefixBlock)
                .then().exactly(10).digits()
                .andNothingElse()
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