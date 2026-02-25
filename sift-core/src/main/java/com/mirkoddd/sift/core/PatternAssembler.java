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

import com.mirkoddd.sift.core.dsl.SiftPattern;

import java.util.HashSet;
import java.util.Set;

/**
 * Internal Buffer and String manipulator for Regex generation.
 * Handles the low-level string concatenation, character escaping, and class flushing.
 */
class PatternAssembler {

    private final StringBuilder mainPattern = new StringBuilder();
    private final StringBuilder pendingClass = new StringBuilder();
    private String currentQuantifier = RegexSyntax.EMPTY;
    private boolean isBuildingClass = false;
    private boolean canMakePossessiveToMain = false;
    private final Set<String> registeredGroups = new HashSet<>();

    PatternAssembler() {
    }

    PatternAssembler(SiftGlobalFlag... flags) {
        mainPattern.append(RegexSyntax.INLINE_FLAG_OPEN);
        for (SiftGlobalFlag flag : flags) {
            mainPattern.append(flag.getSymbol());
        }
        mainPattern.append(RegexSyntax.GROUP_CLOSE);
    }

    void setQuantifier(String quantifier) {
        this.currentQuantifier = quantifier;
    }

    void addAnchor(String anchor) {
        flush();
        mainPattern.append(anchor);
    }

    void addClassRange(String range) {
        isBuildingClass = true;
        pendingClass.append(range);
    }

    void addClassInclusion(char c, char... additionalExtras) {
        if (isBuildingClass) {
            RegexEscaper.escapeInsideBrackets(c, pendingClass);
            for (char extra : additionalExtras) {
                RegexEscaper.escapeInsideBrackets(extra, pendingClass);
            }
        }
    }

    void addClassExclusion(char excluded, char... additionalExcluded) {
        if (isBuildingClass) {
            pendingClass.append(RegexSyntax.CLASS_INTERSECTION_NEGATION);
            RegexEscaper.escapeInsideBrackets(excluded, pendingClass);
            for (char c : additionalExcluded) {
                RegexEscaper.escapeInsideBrackets(c, pendingClass);
            }
            pendingClass.append(RegexSyntax.CLASS_CLOSE);
        }
    }

    void addNamedCapture(NamedCapture group) {
        registeredGroups.add(group.getName());
        mainPattern.append(RegexSyntax.NAMED_GROUP_OPEN)
                .append(group.getName())
                .append(RegexSyntax.NAMED_GROUP_NAME_CLOSE)
                .append(group.getPattern().shake())
                .append(RegexSyntax.GROUP_CLOSE);
    }

    void addBackreference(NamedCapture group) {
        if (!registeredGroups.contains(group.getName())) {
            throw new IllegalStateException("The group '" + group.getName() +
                    "' must be captured with .namedCapture() before it can be referenced.");
        }
        mainPattern.append(RegexSyntax.NAMED_BACKREFERENCE_OPEN)
                .append(group.getName())
                .append(RegexSyntax.NAMED_BACKREFERENCE_CLOSE);
    }

    void addAnyChar() {
        flush();
        mainPattern.append(RegexSyntax.ANY_CHAR).append(currentQuantifier);
        canMakePossessiveToMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
    }

    void addCharacter(char literal) {
        flush();
        StringBuilder esc = new StringBuilder();
        RegexEscaper.escapeString(String.valueOf(literal), esc);
        mainPattern.append(esc).append(currentQuantifier);
        canMakePossessiveToMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
    }

    void addPattern(SiftPattern pattern) {
        flush();
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
    }

    void addWordBoundary() {
        flush();
        mainPattern.append(RegexSyntax.WORD_BOUNDARY);
        currentQuantifier = RegexSyntax.EMPTY;
    }

    void applyPossessiveModifier() {
        if (isBuildingClass) {
            if (!currentQuantifier.isEmpty()) {
                if (currentQuantifier.equals(RegexSyntax.ONE_OR_MORE) ||
                        !currentQuantifier.endsWith(RegexSyntax.POSSESSIVE)) {
                    currentQuantifier += RegexSyntax.POSSESSIVE;
                }
            }
        } else if (canMakePossessiveToMain) {
            mainPattern.append(RegexSyntax.POSSESSIVE);
            canMakePossessiveToMain = false;
        }
    }

    void flush() {
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

    String build() {
        flush();
        return mainPattern.toString();
    }
}