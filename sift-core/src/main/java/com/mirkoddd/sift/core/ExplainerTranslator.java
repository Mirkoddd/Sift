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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

class ExplainerTranslator {
    // Regex Escape Constants
    private static final String ESCAPE_REGEX = "\\\\(.)";
    private static final String ESCAPE_REPLACEMENT = "$1";

    // Quantifier Symbols
    private static final String Q_OPTIONAL = "?";
    private static final String Q_ZERO_OR_MORE = "*";
    private static final String Q_ONE_OR_MORE = "+";

    // Fallback format
    private static final String QUANTIFIER_FALLBACK_PREFIX = " (quantifier: ";
    private static final String QUANTIFIER_FALLBACK_SUFFIX = ")";

    // Localization Keys
    private static final class Msg {
        static final String QUANT_OPTIONAL = "explain.quantifier.optional";
        static final String QUANT_ZERO_OR_MORE = "explain.quantifier.zeroOrMore";
        static final String QUANT_ONE_OR_MORE = "explain.quantifier.oneOrMore";
        static final String QUANT_EXACT = "explain.quantifier.exact";
        static final String QUANT_MIN = "explain.quantifier.min";
        static final String QUANT_RANGE = "explain.quantifier.range";
    }

    private final ResourceBundle bundle;
    private static final Map<String, String> RANGE_KEYS = new HashMap<>();

    static {
        // Standard Ranges
        RANGE_KEYS.put(RegexSyntax.RANGE_DIGITS, "explain.range.digits");
        RANGE_KEYS.put(RegexSyntax.NON_DIGITS, "explain.range.non_digits");
        RANGE_KEYS.put(RegexSyntax.RANGE_LETTERS, "explain.range.letters");
        RANGE_KEYS.put(RegexSyntax.NON_LETTERS, "explain.range.non_letters");
        RANGE_KEYS.put(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY, "explain.range.letters_uppercase");
        RANGE_KEYS.put(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY, "explain.range.letters_lowercase");
        RANGE_KEYS.put(RegexSyntax.RANGE_ALPHANUMERIC, "explain.range.alphanumeric");
        RANGE_KEYS.put(RegexSyntax.NON_ALPHANUMERIC, "explain.range.non_alphanumeric");
        RANGE_KEYS.put(RegexSyntax.WORD_CHARACTERS, "explain.range.word_chars");
        RANGE_KEYS.put(RegexSyntax.NON_WORD_CHARACTERS, "explain.range.non_word_chars");
        RANGE_KEYS.put(RegexSyntax.WHITESPACE, "explain.range.whitespace");
        RANGE_KEYS.put(RegexSyntax.NON_WHITESPACE, "explain.range.non_whitespace");
        RANGE_KEYS.put(RegexSyntax.HORIZONTAL_WHITESPACE, "explain.range.horizontal_whitespace");
        RANGE_KEYS.put(RegexSyntax.VERTICAL_WHITESPACE, "explain.range.vertical_whitespace");
        RANGE_KEYS.put(RegexSyntax.RANGE_HEX_DIGITS, "explain.range.hex_digits");
        RANGE_KEYS.put(RegexSyntax.PUNCTUATION, "explain.range.punctuation");
        RANGE_KEYS.put(RegexSyntax.BLANK, "explain.range.blank");

        // Unicode Ranges
        RANGE_KEYS.put(RegexSyntax.UNICODE_DIGITS, "explain.range.unicode_digits");
        RANGE_KEYS.put(RegexSyntax.NON_UNICODE_DIGITS, "explain.range.unicode_non_digits");
        RANGE_KEYS.put(RegexSyntax.UNICODE_LETTERS, "explain.range.unicode_letters");
        RANGE_KEYS.put(RegexSyntax.NON_UNICODE_LETTERS, "explain.range.unicode_non_letters");
        RANGE_KEYS.put(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY, "explain.range.unicode_letters_uppercase");
        RANGE_KEYS.put(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY, "explain.range.unicode_letters_lowercase");
        RANGE_KEYS.put(RegexSyntax.UNICODE_LETTERS_CASELESS, "explain.range.unicode_letters_caseless");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SYMBOLS, "explain.range.unicode_symbols");
        RANGE_KEYS.put(RegexSyntax.UNICODE_ALPHANUMERIC, "explain.range.unicode_alphanumeric");
        RANGE_KEYS.put(RegexSyntax.NON_UNICODE_ALPHANUMERIC, "explain.range.unicode_non_alphanumeric");
        RANGE_KEYS.put(RegexSyntax.UNICODE_WORD_CHARACTERS, "explain.range.unicode_word_chars");
        RANGE_KEYS.put(RegexSyntax.NON_UNICODE_WORD_CHARACTERS, "explain.range.unicode_non_word_chars");
        RANGE_KEYS.put(RegexSyntax.UNICODE_WHITESPACE, "explain.range.unicode_whitespace");
        RANGE_KEYS.put(RegexSyntax.NON_UNICODE_WHITESPACE, "explain.range.unicode_non_whitespace");
        RANGE_KEYS.put(RegexSyntax.UNICODE_PUNCTUATION, "explain.range.unicode_punctuation");
        RANGE_KEYS.put(RegexSyntax.UNICODE_BLANK, "explain.range.unicode_blank");

        // Script Ranges
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_GREEK, "explain.range.script_greek");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_CYRILLIC, "explain.range.script_cyrillic");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_ARABIC, "explain.range.script_arabic");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_HEBREW, "explain.range.script_hebrew");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_HAN, "explain.range.script_han");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_HIRAGANA, "explain.range.script_hiragana");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_KATAKANA, "explain.range.script_katakana");
        RANGE_KEYS.put(RegexSyntax.UNICODE_SCRIPT_LATIN, "explain.range.script_latin");
    }

    ExplainerTranslator(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    String translate(String key, Object... args) {
        String pattern = bundle.getString(key);
        return args.length > 0 ? MessageFormat.format(pattern, args) : pattern;
    }

    String translateRange(String range) {
        String key = RANGE_KEYS.get(range);
        return key != null ? bundle.getString(key) : null;
    }

    String translateQuantifier(String q) {
        if (Q_OPTIONAL.equals(q)) return bundle.getString(Msg.QUANT_OPTIONAL);
        if (Q_ZERO_OR_MORE.equals(q)) return bundle.getString(Msg.QUANT_ZERO_OR_MORE);
        if (Q_ONE_OR_MORE.equals(q)) return bundle.getString(Msg.QUANT_ONE_OR_MORE);

        if (q.startsWith("{") && q.endsWith("}")) {
            String inner = q.substring(1, q.length() - 1);
            if (!inner.contains(",")) return translate(Msg.QUANT_EXACT, inner);

            String[] parts = inner.split(",");
            if (parts.length == 1 || parts[1].isEmpty()) {
                return translate(Msg.QUANT_MIN, parts[0]);
            } else {
                return translate(Msg.QUANT_RANGE, parts[0], parts[1]);
            }
        }
        return QUANTIFIER_FALLBACK_PREFIX + q + QUANTIFIER_FALLBACK_SUFFIX;
    }

    String formatFlags(String flagsString) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (char c : flagsString.toCharArray()) {
            String sym = String.valueOf(c);
            String name = sym;
            for (SiftGlobalFlag f : SiftGlobalFlag.values()) {
                if (String.valueOf(f.getSymbol()).equals(sym)) {
                    name = formatEnum(f.name());
                    break;
                }
            }
            if (!first) sb.append(", ");
            sb.append(name);
            first = false;
        }
        return sb.toString();
    }

    String formatEnum(String name) {
        StringBuilder formatted = new StringBuilder();
        for (String word : name.split("_")) {
            if (formatted.length() > 0) formatted.append(" ");
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase());
        }
        return formatted.toString();
    }

    String escape(String text) {
        return text.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    String unescapeAndClean(String text) {
        String unescaped = text.replaceAll(ESCAPE_REGEX, ESCAPE_REPLACEMENT);
        return escape(unescaped);
    }
}