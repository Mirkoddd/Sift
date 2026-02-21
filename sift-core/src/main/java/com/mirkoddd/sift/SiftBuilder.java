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

import com.mirkoddd.sift.internal.RegexSyntax;

import com.mirkoddd.sift.dsl.*;

/**
 * Internal implementation of the State Machine.
 */
class SiftBuilder implements QuantifierStep, TypeStep, ConnectorStep {

    private final StringBuilder mainPattern = new StringBuilder();
    private final StringBuilder pendingClass = new StringBuilder();
    private String currentQuantifier = RegexSyntax.EMPTY;
    private boolean isBuildingClass = false;

    public SiftBuilder anchorStart() {
        mainPattern.append(RegexSyntax.START_OF_LINE);
        return this;
    }

    // --- QUANTIFIERS ---

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

    // --- TYPES ---

    @Override
    public ConnectorStep digits() {
        return addToClass(RegexSyntax.RANGE_DIGITS);
    }

    @Override
    public ConnectorStep letters() {
        return addToClass(RegexSyntax.RANGE_LETTERS);
    }

    @Override
    public ConnectorStep lettersLowercaseOnly() {
        return addToClass(RegexSyntax.RANGE_LOWERCASE);
    }

    @Override
    public ConnectorStep lettersUppercaseOnly() {
        return addToClass(RegexSyntax.RANGE_UPPERCASE);
    }

    @Override
    public ConnectorStep alphanumeric() {
        return addToClass(RegexSyntax.RANGE_ALPHANUMERIC);
    }

    @Override
    public ConnectorStep any() {
        flush();
        mainPattern.append(RegexSyntax.ANY_CHAR).append(currentQuantifier);
        currentQuantifier = RegexSyntax.EMPTY;
        return this;
    }

    // --- CONNECTORS ---
    @Override
    public ConnectorStep followedBy(char c) {
        return this.then().exactly(1).character(c);
    }

    @Override
    public ConnectorStep followedBy(SiftPattern... patterns) {
        ConnectorStep current = this;
        for (SiftPattern p : patterns) {
            current = current.then().exactly(1).pattern(p);
        }
        return current;
    }
    @Override
    public ConnectorStep character(char literal) {
        flush();
        StringBuilder esc = new StringBuilder();
        RegexEscaper.escapeString(String.valueOf(literal), esc);
        mainPattern.append(esc).append(currentQuantifier);
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
        } else {
            mainPattern.append(pattern.shake());
        }
        currentQuantifier = RegexSyntax.EMPTY;
        return this;
    }

    @Override
    public QuantifierStep then() {
        flush();
        return this;
    }

    @Override
    public ConnectorStep including(char... extra) {
        if (isBuildingClass) {
            for (char c : extra) RegexEscaper.escapeInsideBrackets(c, pendingClass);
        }
        return this;
    }

    @Override
    public ConnectorStep excluding(char... excluded) {
        if (isBuildingClass) {
            pendingClass.append(RegexSyntax.CLASS_INTERSECTION_NEGATION);
            for (char c : excluded) RegexEscaper.escapeInsideBrackets(c, pendingClass);
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
    public SiftPattern untilEnd() {
        flush();
        mainPattern.append(RegexSyntax.END_OF_LINE);
        return this;
    }

    @Override
    public String shake() {
        flush();
        return mainPattern.toString();
    }

    // --- HELPERS ---

    private ConnectorStep addToClass(String range) {
        if (!isBuildingClass) isBuildingClass = true;
        pendingClass.append(range);
        return this;
    }

    private void flush() {
        if (isBuildingClass) {
            mainPattern.append(RegexSyntax.CLASS_OPEN)
                    .append(pendingClass)
                    .append(RegexSyntax.CLASS_CLOSE)
                    .append(currentQuantifier);
            pendingClass.setLength(0);
            isBuildingClass = false;
            currentQuantifier = RegexSyntax.EMPTY;
        }
    }
}