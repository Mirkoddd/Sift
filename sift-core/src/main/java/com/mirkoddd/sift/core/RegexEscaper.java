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

/**
 * Utility class to escape special Regex characters.
 * <p>
 * Optimized for zero-allocation and high performance using a pre-computed ASCII lookup table.
 */
final class RegexEscaper {
    private static final boolean[] SPECIAL_CHAR_TABLE = new boolean[128];

    static {
        String specialChars = ".?*+^$[](){}|\\<>=!";
        for (char c : specialChars.toCharArray()) {
            SPECIAL_CHAR_TABLE[c] = true;
        }
    }

    private RegexEscaper() {}

    /**
     * Escapes characters that are special in a general Regex context.
     * <p>
     * Avoids {@code toCharArray()} to prevent object allocation.
     * Uses a boolean array for true O(1) lookup speed on ASCII characters.
     *
     * @param text The input string to escape.
     * @param sb   The StringBuilder to append the escaped string to.
     */
    static void escapeString(String text, StringBuilder sb) {
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (isSpecial(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
    }

    /**
     * Escapes characters that are special inside a character class {@code [...]}.
     * <p>
     * Inside brackets, only {@code \}, {@code -}, {@code ^}, and {@code ]} need escaping.
     * {@code &} is also escaped to prevent accidental intersection logic ({@code &&}).
     *
     * @param c  The character to escape and append.
     * @param sb The StringBuilder to append to.
     */
    static void escapeInsideBrackets(char c, StringBuilder sb) {
        switch (c) {
            case '\\':
            case '-':
            case '^':
            case ']':
            case '&': // Safety against "&&" (intersection)
                sb.append('\\');
                break;
        }
        sb.append(c);
    }

    private static boolean isSpecial(char c) {
        return c < 128 && SPECIAL_CHAR_TABLE[c];
    }
}