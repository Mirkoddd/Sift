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

/**
 * Visitor interface defining the contract for traversing the Sift Lazy AST.
 * <p>
 * By separating the structural representation of a pattern (the nodes) from the logic
 * acting upon it, Sift can effortlessly support multiple outputs. Implementations of this
 * interface can generate the raw cross-engine regex string, build human-readable English
 * explanations, or even translate the pattern into other languages without modifying
 * the core DSL structure.
 */
interface PatternVisitor {
    void visitQuantifier(String quantifier);
    void visitAnchor(String anchor);
    void visitClassRange(String range);
    void visitClassInclusion(char c, char... additionalExtras);
    void visitClassExclusion(char excluded, char... additionalExcluded);
    void visitCustomRange(char start, char end);
    void visitBackreference(NamedCapture group);
    void visitAnyChar();
    void visitLinebreak();
    void visitClassIntersection(String intersectionClass);
    void visitCharacter(char literal);
    void visitPattern(SiftPattern<? extends Composable> pattern);
    void visitPrependPattern(SiftPattern<? extends Composable> pattern);
    void visitWordBoundary();
    void visitPossessiveModifier();
    void visitLazyModifier();
    void visitAtomicGroup(SiftPattern<?> pattern);
    void visitAnyOf(java.util.List<? extends SiftPattern<?>> options);
    void visitCaptureGroup(SiftPattern<?> pattern);
    void visitNonCapturingGroup(java.util.List<? extends SiftPattern<?>> patterns);
    void visitLookaround(SiftPattern<?> pattern, boolean positive, boolean lookahead);
    void visitNamedCapture(String name, SiftPattern<?> pattern);
    void visitLocalFlags(SiftPattern<?> pattern, SiftGlobalFlag... flags);
    void visitConditional(SiftPattern<?> trueCond, SiftPattern<?> thenPat, SiftPattern<?> falseCond, SiftPattern<?> falsePat);
    void visitFeature(RegexFeature feature);
}