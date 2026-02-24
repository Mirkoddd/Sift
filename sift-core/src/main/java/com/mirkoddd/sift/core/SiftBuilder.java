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

import com.mirkoddd.sift.core.dsl.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Internal implementation of the State Machine.
 */
class SiftBuilder implements QuantifierStep, TypeStep, ConnectorStep {

    private final StringBuilder mainPattern = new StringBuilder();
    private final StringBuilder pendingClass = new StringBuilder();
    private String currentQuantifier = RegexSyntax.EMPTY;
    private boolean isBuildingClass = false;
    private boolean canMakePossessiveToMain = false;
    private final Set<String> registeredGroups = new HashSet<>();
    /**
     * Default constructor for standard builder.
     */
    SiftBuilder() {
    }

    /**
     * Constructor that initializes the pattern with global inline flags.
     *
     * @param flags The flags to apply (guaranteed to be at least one by the public API).
     */
    SiftBuilder(SiftGlobalFlag... flags) {
        mainPattern.append(RegexSyntax.INLINE_FLAG_OPEN);
        for (SiftGlobalFlag flag : flags) {
            mainPattern.append(flag.getSymbol());
        }
        mainPattern.append(RegexSyntax.GROUP_CLOSE);
    }

    public SiftBuilder anchorStart() {
        mainPattern.append(RegexSyntax.START_OF_LINE);
        return this;
    }

    @Override
    public TypeStep exactly(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        currentQuantifier = (n == 1) ? RegexSyntax.EMPTY :
                RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.QUANTIFIER_CLOSE;
        return this;
    }

    @Override
    public TypeStep atLeast(int n) {
        if (n < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + n);
        currentQuantifier = RegexSyntax.QUANTIFIER_OPEN + n + RegexSyntax.COMMA + RegexSyntax.QUANTIFIER_CLOSE;
        return this;
    }

    @Override
    public TypeStep oneOrMore() {
        currentQuantifier = RegexSyntax.ONE_OR_MORE;
        return this;
    }

    @Override
    public TypeStep zeroOrMore() {
        currentQuantifier = RegexSyntax.ZERO_OR_MORE;
        return this;
    }

    @Override
    public TypeStep optional() {
        currentQuantifier = RegexSyntax.OPTIONAL;
        return this;
    }

    @Override
    public TypeStep atMost(int max) {
        if (max < 0) throw new IllegalArgumentException("Quantity cannot be negative: " + max);
        currentQuantifier = RegexSyntax.QUANTIFIER_OPEN + "0" + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE;
        return this;
    }

    @Override
    public TypeStep between(int min, int max) {
        if (min < 0 || max < 0) throw new IllegalArgumentException("Quantities cannot be negative");
        if (min > max) throw new IllegalArgumentException("Min cannot be greater than max");
        currentQuantifier = RegexSyntax.QUANTIFIER_OPEN + min + RegexSyntax.COMMA + max + RegexSyntax.QUANTIFIER_CLOSE;
        return this;
    }

    @Override
    public ConnectorStep namedCapture(NamedCapture group) {
        if (group == null) throw new IllegalArgumentException("NamedCapture cannot be null.");

        registeredGroups.add(group.getName());

        mainPattern.append(RegexSyntax.NAMED_GROUP_OPEN)
                .append(group.getName())
                .append(RegexSyntax.NAMED_GROUP_NAME_CLOSE)
                .append(group.getPattern().shake())
                .append(RegexSyntax.GROUP_CLOSE);

        return this;
    }

    @Override
    public ConnectorStep backreference(NamedCapture group) {
        if (group == null) throw new IllegalArgumentException("Backreference group cannot be null.");

        if (!registeredGroups.contains(group.getName())) {
            throw new IllegalStateException("The group '" + group.getName() +
                    "' must be captured with .namedCapture() before it can be referenced.");
        }

        mainPattern.append(RegexSyntax.NAMED_BACKREFERENCE_OPEN)
                .append(group.getName())
                .append(RegexSyntax.NAMED_BACKREFERENCE_CLOSE);

        return this;
    }

    @Override
    public ConnectorStep any() {
        flush();
        mainPattern.append(RegexSyntax.ANY_CHAR).append(currentQuantifier);
        canMakePossessiveToMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
        return this;
    }

    @Override
    public ConnectorStep character(char literal) {
        flush();
        StringBuilder esc = new StringBuilder();
        RegexEscaper.escapeString(String.valueOf(literal), esc);
        mainPattern.append(esc).append(currentQuantifier);
        canMakePossessiveToMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
        return this;
    }

    @Override
    public ConnectorStep pattern(SiftPattern pattern) {
        flush();
        // Applies quantifier to the pattern (wrapping in non-capturing group if needed logic is inside the pattern or handled here)
        // Since we don't know the content of SiftPattern, if there is a quantifier we wrap it to be safe.
        if (!currentQuantifier.isEmpty()) {
            mainPattern.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN)
                    .append(pattern.shake())
                    .append(RegexSyntax.GROUP_CLOSE)
                    .append(currentQuantifier);
            canMakePossessiveToMain = true;
        } else {
            mainPattern.append(pattern.shake());
            canMakePossessiveToMain = false;
        }
        currentQuantifier = RegexSyntax.EMPTY;
        return this;
    }

    @Override
    public ConnectorStep digits() {
        return addToClass(RegexSyntax.RANGE_DIGITS);
    }

    @Override
    public ConnectorStep nonDigits() {
        return addToClass(RegexSyntax.NON_DIGITS);
    }

    @Override
    public ConnectorStep unicodeDigits() {
        return addToClass(RegexSyntax.UNICODE_DIGITS);
    }

    @Override
    public ConnectorStep nonUnicodeDigits() {
        return addToClass(RegexSyntax.NON_UNICODE_DIGITS);
    }

    @Override
    public ConnectorStep letters() {
        return addToClass(RegexSyntax.RANGE_LETTERS);
    }

    @Override
    public ConnectorStep nonLetters() {
        return addToClass(RegexSyntax.NON_LETTERS);
    }

    @Override
    public ConnectorStep lettersUppercaseOnly() {
        return addToClass(RegexSyntax.RANGE_LETTERS_UPPERCASE_ONLY);
    }

    @Override
    public ConnectorStep lettersLowercaseOnly() {
        return addToClass(RegexSyntax.RANGE_LETTERS_LOWERCASE_ONLY);
    }

    @Override
    public ConnectorStep unicodeLetters() {
        return addToClass(RegexSyntax.UNICODE_LETTERS);
    }

    @Override
    public ConnectorStep nonUnicodeLetters() {
        return addToClass(RegexSyntax.NON_UNICODE_LETTERS);
    }

    @Override
    public ConnectorStep unicodeLettersUppercaseOnly() {
        return addToClass(RegexSyntax.UNICODE_LETTERS_UPPERCASE_ONLY);
    }

    @Override
    public ConnectorStep unicodeLettersLowercaseOnly() {
        return addToClass(RegexSyntax.UNICODE_LETTERS_LOWERCASE_ONLY);
    }

    @Override
    public ConnectorStep alphanumeric() {
        return addToClass(RegexSyntax.RANGE_ALPHANUMERIC);
    }

    @Override
    public ConnectorStep nonAlphanumeric() {
        return addToClass(RegexSyntax.NON_ALPHANUMERIC);
    }

    @Override
    public ConnectorStep unicodeAlphanumeric() {
        return addToClass(RegexSyntax.UNICODE_ALPHANUMERIC);
    }

    @Override
    public ConnectorStep nonUnicodeAlphanumeric() {
        return addToClass(RegexSyntax.NON_UNICODE_ALPHANUMERIC);
    }

    @Override
    public ConnectorStep wordCharacters() {
        return addToClass(RegexSyntax.WORD_CHARACTERS);
    }

    @Override
    public ConnectorStep nonWordCharacters() {
        return addToClass(RegexSyntax.NON_WORD_CHARACTERS);
    }

    @Override
    public ConnectorStep unicodeWordCharacters() {
        return addToClass(RegexSyntax.UNICODE_WORD_CHARACTERS);
    }

    @Override
    public ConnectorStep nonUnicodeWordCharacters() {
        return addToClass(RegexSyntax.NON_UNICODE_WORD_CHARACTERS);
    }

    @Override
    public ConnectorStep whitespace() {
        return addToClass(RegexSyntax.WHITESPACE);
    }

    @Override
    public ConnectorStep nonWhitespace() {
        return addToClass(RegexSyntax.NON_WHITESPACE);
    }

    @Override
    public ConnectorStep unicodeWhitespace() {
        return addToClass(RegexSyntax.UNICODE_WHITESPACE);
    }

    @Override
    public ConnectorStep nonUnicodeWhitespace() {
        return addToClass(RegexSyntax.NON_UNICODE_WHITESPACE);
    }

    @Override
    public ConnectorStep followedBy(char c) {
        return this.then().exactly(1).character(c);
    }

    @Override
    public ConnectorStep followedBy(SiftPattern pattern, SiftPattern... additionalPatterns) {
        ConnectorStep current = this.then().exactly(1).pattern(pattern);

        for (SiftPattern p : additionalPatterns) {
            current = current.then().exactly(1).pattern(p);
        }

        return current;
    }

    @Override
    public QuantifierStep then() {
        flush();
        return this;
    }

    @Override
    public ConnectorStep including(char extra, char... additionalExtras) {
        if (isBuildingClass) {
            RegexEscaper.escapeInsideBrackets(extra, pendingClass);

            for (char c : additionalExtras) {
                RegexEscaper.escapeInsideBrackets(c, pendingClass);
            }
        }
        return this;
    }

    @Override
    public ConnectorStep excluding(char excluded, char... additionalExcluded) {
        if (isBuildingClass) {
            pendingClass.append(RegexSyntax.CLASS_INTERSECTION_NEGATION);
            RegexEscaper.escapeInsideBrackets(excluded, pendingClass);

            for (char c : additionalExcluded) {
                RegexEscaper.escapeInsideBrackets(c, pendingClass);
            }

            pendingClass.append(RegexSyntax.CLASS_CLOSE);
        }
        return this;
    }

    @Override
    public ConnectorStep wordBoundary() {
        flush();
        mainPattern.append(RegexSyntax.WORD_BOUNDARY);
        currentQuantifier = RegexSyntax.EMPTY;
        return this;
    }

    @Override
    public ConnectorStep withoutBacktracking() {
        if (isBuildingClass) {
            if (!currentQuantifier.isEmpty()) {
                if (currentQuantifier.equals(RegexSyntax.ONE_OR_MORE) || !currentQuantifier.endsWith(RegexSyntax.POSSESSIVE)) {
                    currentQuantifier += RegexSyntax.POSSESSIVE;
                }
            }
        } else if (canMakePossessiveToMain) {
            mainPattern.append(RegexSyntax.POSSESSIVE);
            canMakePossessiveToMain = false;
        }
        return this;
    }

    @Override
    public SiftPattern andNothingElse() {
        flush();
        mainPattern.append(RegexSyntax.END_OF_LINE);
        return this;
    }

    @Override
    public String shake() {
        flush();
        return mainPattern.toString();
    }

    private ConnectorStep addToClass(String range) {
        isBuildingClass = true;
        pendingClass.append(range);
        return this;
    }

    private void flush() {
        if (isBuildingClass) {
            mainPattern.append(RegexSyntax.CLASS_OPEN)
                    .append(pendingClass)
                    .append(RegexSyntax.CLASS_CLOSE)
                    .append(currentQuantifier);
            canMakePossessiveToMain = !currentQuantifier.isEmpty();
            pendingClass.setLength(0);
            isBuildingClass = false;
            currentQuantifier = RegexSyntax.EMPTY;
        } else {
            canMakePossessiveToMain = false;
        }
    }
}