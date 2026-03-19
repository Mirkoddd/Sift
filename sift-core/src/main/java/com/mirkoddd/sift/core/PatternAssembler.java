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

import com.mirkoddd.sift.core.dsl.Composable;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.RegexFeature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Internal Buffer and String manipulator for Regex generation.
 * Handles the low-level string concatenation, character escaping, class flushing,
 * and tracking of advanced regex features for cross-engine compatibility.
 * <p>
 * This class now implements {@link PatternVisitor} to act as the regex compiler
 * traversing the Lazy AST.
 */
class PatternAssembler implements PatternVisitor {

    private enum QuantifierModifier {
        NONE, POSSESSIVE, LAZY
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
    private final EnumSet<RegexFeature> usedFeatures = EnumSet.noneOf(RegexFeature.class);

    PatternAssembler() {
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

    Set<RegexFeature> getUsedFeatures() {
        return Collections.unmodifiableSet(usedFeatures);
    }

    @Override
    public void visitFeature(RegexFeature feature) {
        this.usedFeatures.add(feature);
    }

    @Override
    public void visitQuantifier(String quantifier) {
        flush();
        this.currentQuantifier = quantifier;
    }

    @Override
    public void visitAnchor(String anchor) {
        flush();
        if (RegexSyntax.START_OF_LINE.equals(anchor) || RegexSyntax.END_OF_LINE.equals(anchor)){
            containsAbsoluteAnchor = true;
        }
        mainPattern.append(anchor);
    }

    @Override
    public void visitClassRange(String range) {
        isBuildingClass = true;
        pendingClass.append(range);
    }

    @Override
    public void visitClassInclusion(char c, char... additionalExtras) {
        RegexEscaper.escapeInsideBrackets(c, pendingClass);
        for (char extra : additionalExtras) {
            RegexEscaper.escapeInsideBrackets(extra, pendingClass);
        }
    }

    @Override
    public void visitClassExclusion(char excluded, char... additionalExcluded) {
        pendingClass.append(RegexSyntax.CLASS_INTERSECTION_NEGATION);
        RegexEscaper.escapeInsideBrackets(excluded, pendingClass);
        for (char c : additionalExcluded) {
            RegexEscaper.escapeInsideBrackets(c, pendingClass);
        }
        pendingClass.append(RegexSyntax.CLASS_CLOSE);
    }

    @Override
    public void visitCustomRange(char start, char end) {
        isBuildingClass = true;
        RegexEscaper.escapeInsideBrackets(start, pendingClass);
        pendingClass.append('-');
        RegexEscaper.escapeInsideBrackets(end, pendingClass);
    }

    @Override
    public void visitBackreference(NamedCapture group) {
        flush();
        requiredBackreferences.add(group.getName());
        visitFeature(RegexFeature.BACKREFERENCE);

        mainPattern.append(RegexSyntax.NAMED_BACKREFERENCE_OPEN)
                .append(group.getName())
                .append(RegexSyntax.NAMED_BACKREFERENCE_CLOSE)
                .append(currentQuantifier);
    }

    @Override
    public void visitAnyChar() {
        flush();
        mainPattern.append(RegexSyntax.ANY_CHAR).append(currentQuantifier);
        canModifyMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
    }

    @Override
    public void visitLinebreak() {
        flush();
        mainPattern.append(RegexSyntax.LINEBREAK).append(currentQuantifier);
        canModifyMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
    }

    @Override
    public void visitClassIntersection(String intersectionClass) {
        isBuildingClass = true;
        pendingClass.append(RegexSyntax.CLASS_INTERSECTION);
        pendingClass.append(intersectionClass);
        pendingClass.append(RegexSyntax.CLASS_CLOSE);
    }

    @Override
    public void visitCharacter(char literal) {
        flush();
        StringBuilder esc = new StringBuilder();
        RegexEscaper.escapeString(String.valueOf(literal), esc);
        mainPattern.append(esc).append(currentQuantifier);
        canModifyMain = !currentQuantifier.isEmpty();
        currentQuantifier = RegexSyntax.EMPTY;
    }

    @Override
    public void visitPattern(SiftPattern<? extends Composable> pattern) {
        flush();
        extractAndCheckGroupsAndRequirements(pattern, null);

        String rawSubPattern = extractRawRegex(pattern);

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

    @Override
    public void visitPrependPattern(SiftPattern<? extends Composable> pattern) {
        flush();
        extractAndCheckGroupsAndRequirements(pattern, null);

        String rawSubPattern = extractRawRegex(pattern);
        mainPattern.insert(0, rawSubPattern);
    }

    @Override
    public void visitWordBoundary() {
        flush();
        mainPattern.append(RegexSyntax.WORD_BOUNDARY);
        currentQuantifier = RegexSyntax.EMPTY;
    }

    @Override
    public void visitPossessiveModifier() {
        if (isBuildingClass) {
            if (!currentQuantifier.isEmpty()) {
                quantifierModifier = QuantifierModifier.POSSESSIVE;
            }
        } else if (canModifyMain) {
            mainPattern.append(RegexSyntax.POSSESSIVE_MODIFIER);
            canModifyMain = false;
        }
    }

    @Override
    public void visitLazyModifier() {
        if (isBuildingClass) {
            if (!currentQuantifier.isEmpty()) {
                quantifierModifier = QuantifierModifier.LAZY;
            }
        } else if (canModifyMain) {
            mainPattern.append(RegexSyntax.LAZY_MODIFIER);
            canModifyMain = false;
        }
    }

    @Override
    public void visitAtomicGroup(SiftPattern<?> pattern) {
        flush();
        extractAndCheckGroupsAndRequirements(pattern, null);
        visitFeature(RegexFeature.ATOMIC_GROUP);

        String rawSubPattern = extractRawRegex(pattern);

        mainPattern.append(RegexSyntax.ATOMIC_GROUP_OPEN)
                .append(rawSubPattern)
                .append(RegexSyntax.GROUP_CLOSE);

        canModifyMain = false;
    }

    @Override
    public void visitAnyOf(List<? extends SiftPattern<?>> options) {
        flush();
        mainPattern.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);
        for (int i = 0; i < options.size(); i++) {
            SiftPattern<?> option = options.get(i);
            extractAndCheckGroupsAndRequirements(option, null);
            mainPattern.append(extractRawRegex(option));
            if (i < options.size() - 1) {
                mainPattern.append(RegexSyntax.OR);
            }
        }
        mainPattern.append(RegexSyntax.GROUP_CLOSE);
        canModifyMain = false;
    }

    @Override
    public void visitCaptureGroup(SiftPattern<?> pattern) {
        flush();
        extractAndCheckGroupsAndRequirements(pattern, null);
        mainPattern.append(RegexSyntax.GROUP_OPEN)
                .append(extractRawRegex(pattern))
                .append(RegexSyntax.GROUP_CLOSE);
        canModifyMain = false;
    }

    @Override
    public void visitNonCapturingGroup(List<? extends SiftPattern<?>> patterns) {
        flush();
        mainPattern.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);
        for (SiftPattern<?> p : patterns) {
            extractAndCheckGroupsAndRequirements(p, null);
            mainPattern.append(extractRawRegex(p));
        }
        mainPattern.append(RegexSyntax.GROUP_CLOSE);
        canModifyMain = false;
    }

    @Override
    public void visitLookaround(SiftPattern<?> pattern, boolean positive, boolean lookahead) {
        flush();
        extractAndCheckGroupsAndRequirements(pattern, null);

        RegexFeature feature = lookahead ? RegexFeature.LOOKAHEAD : RegexFeature.LOOKBEHIND;
        visitFeature(feature);

        String openSyntax;
        if (lookahead) {
            openSyntax = positive ? RegexSyntax.POSITIVE_LOOKAHEAD_OPEN : RegexSyntax.NEGATIVE_LOOKAHEAD_OPEN;
        } else {
            openSyntax = positive ? RegexSyntax.POSITIVE_LOOKBEHIND_OPEN : RegexSyntax.NEGATIVE_LOOKBEHIND_OPEN;
        }

        mainPattern.append(openSyntax)
                .append(extractRawRegex(pattern))
                .append(RegexSyntax.GROUP_CLOSE);

        canModifyMain = false;
    }

    @Override
    public void visitNamedCapture(String name, SiftPattern<?> pattern) {
        flush();
        boolean isAdded = registeredGroups.add(name);
        if (!isAdded) {
            throw new IllegalStateException("A capturing group with the name '" + name +
                    "' has already been defined. Each group name must be unique.");
        }
        extractAndCheckGroupsAndRequirements(pattern, name);
        visitFeature(RegexFeature.NAMED_CAPTURE);

        mainPattern.append(RegexSyntax.NAMED_GROUP_OPEN)
                .append(name)
                .append(RegexSyntax.NAMED_GROUP_NAME_CLOSE)
                .append(extractRawRegex(pattern))
                .append(RegexSyntax.GROUP_CLOSE);

        canModifyMain = false;
    }

    @Override
    public void visitLocalFlags(SiftPattern<?> pattern, SiftGlobalFlag... flags) {
        flush();
        extractAndCheckGroupsAndRequirements(pattern, null);
        visitFeature(RegexFeature.INLINE_FLAGS);

        mainPattern.append(RegexSyntax.INLINE_FLAG_OPEN);
        for (SiftGlobalFlag flag : flags) {
            mainPattern.append(flag.getSymbol());
        }
        mainPattern.append(RegexSyntax.INLINE_FLAG_SEPARATOR)
                .append(extractRawRegex(pattern))
                .append(RegexSyntax.GROUP_CLOSE);

        canModifyMain = false;
    }

    @Override
    public void visitConditional(SiftPattern<?> trueCond, SiftPattern<?> thenPat, SiftPattern<?> falseCond, SiftPattern<?> falsePat) {
        flush();
        extractAndCheckGroupsAndRequirements(trueCond, null);
        extractAndCheckGroupsAndRequirements(thenPat, null);
        extractAndCheckGroupsAndRequirements(falseCond, null);
        if (falsePat != null) {
            extractAndCheckGroupsAndRequirements(falsePat, null);
        }
        visitFeature(RegexFeature.CONDITIONAL);

        mainPattern.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN)
                .append(extractRawRegex(trueCond))
                .append(extractRawRegex(thenPat))
                .append(RegexSyntax.OR)
                .append(extractRawRegex(falseCond));

        if (falsePat != null) {
            mainPattern.append(extractRawRegex(falsePat));
        }

        mainPattern.append(RegexSyntax.GROUP_CLOSE);
        canModifyMain = false;
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
        usedFeatures.addAll(getIncomingFeatures(pattern));
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

    private static Set<RegexFeature> getIncomingFeatures(SiftPattern<?> pattern) {
        if (pattern instanceof PatternMetadata) {
            return ((PatternMetadata) pattern).getInternalUsedFeatures();
        }
        return Collections.emptySet();
    }

    private String extractRawRegex(SiftPattern<?> pattern) {
        if (pattern instanceof PatternMetadata) {
            return ((PatternMetadata) pattern).getInternalRawRegex();
        }
        return pattern.shake();
    }
}