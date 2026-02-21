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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class RegexEscaperTest {

    @Test
    void testPrivateConstructorIsPrivateAndInvokable() throws Exception {
        // Reflection to check if the constructor is private and invokable
        Constructor<RegexEscaper> constructor = RegexEscaper.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");

        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void testEscapeString_HitsAllSpecialCharacters() {
        String allSpecials = ".?*+^$[](){}|\\<>=!";
        StringBuilder sb = new StringBuilder();

        RegexEscaper.escapeString(allSpecials, sb);

        // Check if every char is escaped
        assertEquals("\\.\\?\\*\\+\\^\\$\\[\\]\\(\\)\\{\\}\\|\\\\\\<\\>\\=\\!", sb.toString());
    }

    @Test
    void testEscapeString_NormalCharactersAreNotEscaped() {
        String normalText = "abc123 ";
        StringBuilder sb = new StringBuilder();

        RegexEscaper.escapeString(normalText, sb);

        assertEquals("abc123 ", sb.toString());
    }

    @Test
    void testEscapeInsideBrackets_HitsAllSpecialCharacters() {
        char[] bracketSpecials = {'\\', '-', '^', ']', '&'};
        StringBuilder sb = new StringBuilder();

        for (char c : bracketSpecials) {
            RegexEscaper.escapeInsideBrackets(c, sb);
        }

        assertEquals("\\\\\\-\\^\\]\\&", sb.toString());
    }

    @Test
    void testEscapeInsideBrackets_NormalCharactersAreNotEscaped() {
        StringBuilder sb = new StringBuilder();

        RegexEscaper.escapeInsideBrackets('a', sb);
        RegexEscaper.escapeInsideBrackets('5', sb);
        RegexEscaper.escapeInsideBrackets('_', sb);

        assertEquals("a5_", sb.toString());
    }
}
