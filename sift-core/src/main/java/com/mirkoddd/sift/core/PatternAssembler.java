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

import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Internal Buffer and String manipulator for Regex generation.
 * Handles the low-level string concatenation, character escaping, and class flushing.
 */
class PatternAssembler {

    private enum QuantifierModifier {
        /** No modifier applied. The quantifier behaves greedily (default). */
        NONE,
        /** Possessive modifier ({@code ++}, {@code *+}, {@code ?+}, {@code {n,m}+}). Disables backtracking. */
        POSSESSIVE,
        /** Lazy modifier ({@code +?}, {@code *?}, {@code ??}, {@code {n,m}?}). Matches as few characters as possible. */
        LAZY
    }

    private final StringBuilder mainPattern = new StringBuilder();
    private final StringBuilder pendingClass = new StringBuilder();
    private String currentQuantifier = RegexSyntax.EMPTY;
    private boolean isBuildingClass = false;
    private QuantifierModifier quantifierModifier = QuantifierModifier.NONE;
    private boolean canModifyMain = false;
    private final Set<String> registeredGroups = new HashSet<>();
    private final Set<String> requiredBackreferences = new HashSet<>();
    private boolean containsAbsoluteAnchor = false;

    PatternAssembler() {
    }

    PatternAssembler(SiftGlobalFlag... flags) {
        mainPattern.append(RegexSyntax.INLINE_FLAG_OPEN);
        for (SiftGlobalFlag flag : flags) {
            mainPattern.append(flag.getSymbol());
        }
        mainPattern.append(RegexSyntax.GROUP_CLOSE);
    }

    Set<String> getRegisteredGroups() {
        return registeredGroups;
    }

    Set<String> getRequiredBackreferences() {
        return requiredBackreferences;
    }

    public boolean isContainsAbsoluteAnchor() {
        return containsAbsoluteAnchor;
    }

    void setQuantifier(String quantifier) {
        this.currentQuantifier = quantifier;
    }

    void addAnchor(String anchor) {
        flush();

        if (RegexSyntax.START_OF_LINE.equals(anchor) || RegexSyntax.END_OF_LINE.equals(anchor)){
            containsAbsoluteAnchor = true;
        }

        mainPattern.append(anchor);
    }

    void addClassRange(String range) {
        isBuildingClass = true;
        pendingClass.append(range);
    }

    void addClassInclusion(char c, char... additionalExtras) {
        RegexEscaper.escapeInsideBrackets(c, pendingClass);
        for (char extra : additionalExtras) {
            RegexEscaper.escapeInsideBrackets(extra, pendingClass);
        }
    }

    void addClassExclusion(char excluded, char... additionalExcluded) {
        pendingClass.append(RegexSyntax.CLASS_INTERSECTION_NEGATION);
        RegexEscaper.escapeInsideBrackets(excluded, pendingClass);
        for (char c : additionalExcluded) {
            RegexEscaper.escapeInsideBrackets(c, pendingClass);
        }
        pendingClass.append(RegexSyntax.CLASS_CLOSE);
    }

    void addCustomRange(char start, char end) {
        if (start > end) {
            throw new IllegalArgumentException("Invalid range: start character '" + start +
                    "' must be less than or equal to end character '" + end + "'.");
        }
        isBuildingClass = true;
        RegexEscaper.escapeInsideBrackets(start, pendingClass);
        pendingClass.append('-');
        RegexEscaper.escapeInsideBrackets(end, pendingClass);
    }

    void addNamedCapture(NamedCapture group) {
        boolean isAdded = registeredGroups.add(group.getName());
        if (!isAdded) {
            throw new IllegalStateException("A capturing group with the name '" + group.getName() +
                    "' has already been defined. Each group name must be unique.");
        }

        extractAndCheckGroupsAndRequirements(group.getPattern(), group.getName());

        mainPattern.append(RegexSyntax.NAMED_GROUP_OPEN)
                .append(group.getName())
                .append(RegexSyntax.NAMED_GROUP_NAME_CLOSE)
                .append(group.getPattern().shake())
                .append(RegexSyntax.GROUP_CLOSE);
    }

    void addBackreference(NamedCapture group) {
        requiredBackreferences.add(group.getName());

        mainPattern.append(RegexSyntax.NAMED_BACKREFERENCE_OPEN)
                .append(group.getName())
                .append(RegexSyntax.NAMED_BACKREFERENCE_CLOSE);
    }

    void addAnyChar() {
        flush();
        mainPattern.append(RegexSyntax.ANY_CHAR).append(currentQuantifier);
        canModifyMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
    }

    void addCharacter(char literal) {
        flush();
        StringBuilder esc = new StringBuilder();
        RegexEscaper.escapeString(String.valueOf(literal), esc);
        mainPattern.append(esc).append(currentQuantifier);
        canModifyMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
    }

    void addPattern(SiftPattern<SiftContext.Fragment> pattern) {
        flush();
        extractAndCheckGroupsAndRequirements(pattern, null);

        String rawSubPattern = extractRawString(pattern);

        if (!currentQuantifier.isEmpty()) {
            mainPattern.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN)
                    .append(rawSubPattern)
                    .append(RegexSyntax.GROUP_CLOSE)
                    .append(currentQuantifier);
            canModifyMain = true;
        } else {
            mainPattern.append(rawSubPattern);
            canModifyMain = false;
        }
        currentQuantifier = RegexSyntax.EMPTY;
    }

    private void extractAndCheckGroupsAndRequirements(SiftPattern<?> pattern, String wrapperGroupName) {
        Set<String> incomingGroups = getIncomingGroups(pattern);
        for (String incomingGroup : incomingGroups) {
            if (!registeredGroups.add(incomingGroup)) {
                if (wrapperGroupName != null) {
                    throw new IllegalStateException("Collision detected: The pattern inside capturing group '" +
                            wrapperGroupName + "' contains a nested group named '" + incomingGroup +
                            "' which has already been defined in the current builder.");
                } else {
                    throw new IllegalStateException("Collision detected: The sub-pattern contains a capturing group named '" +
                            incomingGroup + "' which has already been defined in the current pattern.");
                }
            }
        }

        requiredBackreferences.addAll(getIncomingBackreferences(pattern));
    }

    private static Set<String> getIncomingGroups(SiftPattern<?> pattern) {
        if (pattern instanceof PatternMetadata) {
            return ((PatternMetadata) pattern).getInternalRegisteredGroups();
        }
        return Collections.emptySet();
    }

    private static Set<String> getIncomingBackreferences(SiftPattern<?> pattern) {
        if (pattern instanceof PatternMetadata) {
            return ((PatternMetadata) pattern).getInternalRequiredBackreferences();
        }
        return Collections.emptySet();
    }

    void addWordBoundary() {
        flush();
        mainPattern.append(RegexSyntax.WORD_BOUNDARY);
        currentQuantifier = RegexSyntax.EMPTY;
    }

    void applyPossessiveModifier() {
        if (isBuildingClass) {
            if (!currentQuantifier.isEmpty()) {
                quantifierModifier = QuantifierModifier.POSSESSIVE;
            }
        } else if (canModifyMain) {
            mainPattern.append(RegexSyntax.POSSESSIVE_MODIFIER);
            canModifyMain = false;
        }
    }

    void applyLazyModifier() {
        if (isBuildingClass) {
            if (!currentQuantifier.isEmpty()) {
                quantifierModifier = QuantifierModifier.LAZY;
            }
        } else if (canModifyMain) {
            mainPattern.append(RegexSyntax.LAZY_MODIFIER);
            canModifyMain = false;
        }
    }

    void flush() {
        if (isBuildingClass) {
            String modifier;
            switch (quantifierModifier) {
                case POSSESSIVE: modifier = RegexSyntax.POSSESSIVE_MODIFIER; break;
                case LAZY:       modifier = RegexSyntax.LAZY_MODIFIER;       break;
                default:         modifier = RegexSyntax.EMPTY;               break;
            }
            String fullQuantifier = currentQuantifier + modifier;

            mainPattern.append(RegexSyntax.CLASS_OPEN)
                    .append(pendingClass)
                    .append(RegexSyntax.CLASS_CLOSE)
                    .append(fullQuantifier);

            canModifyMain = !fullQuantifier.isEmpty();
            pendingClass.setLength(0);
            isBuildingClass = false;
            currentQuantifier = RegexSyntax.EMPTY;
            quantifierModifier = QuantifierModifier.NONE;
        } else {
            canModifyMain = false;
        }
    }

    String build() {
        flush();
        return mainPattern.toString();
    }

    void validateFinalAssembly() {
        for (String req : requiredBackreferences) {
            if (!registeredGroups.contains(req)) {
                throw new IllegalStateException("The group '" + req +
                        "' must be captured with .namedCapture() before it can be referenced.");
            }
        }
    }

    PatternAssembler copy() {
        PatternAssembler clone = new PatternAssembler();
        clone.mainPattern.append(this.mainPattern);
        clone.pendingClass.append(this.pendingClass);
        clone.currentQuantifier = this.currentQuantifier;
        clone.isBuildingClass = this.isBuildingClass;
        clone.quantifierModifier = this.quantifierModifier;
        clone.canModifyMain = this.canModifyMain;
        clone.registeredGroups.addAll(this.registeredGroups);
        clone.requiredBackreferences.addAll(this.requiredBackreferences);
        clone.containsAbsoluteAnchor = this.containsAbsoluteAnchor;
        return clone;
    }

    private static String extractRawString(SiftPattern<?> pattern) {
        if (pattern instanceof SiftConnector) {
            return ((SiftConnector<?>) pattern).assembler.copy().build();
        }
        return pattern.shake();
    }
}