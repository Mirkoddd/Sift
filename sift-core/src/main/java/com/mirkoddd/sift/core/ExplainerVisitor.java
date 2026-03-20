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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

class ExplainerVisitor implements PatternVisitor {
    private static final String RECURSIVE_FALLBACK = "(?!)";

    private static final class Msg {
        static final String ANY_CHAR = "explain.anyChar";
        static final String ANCHOR_START = "explain.anchor.start";
        static final String ANCHOR_END = "explain.anchor.end";
        static final String ANCHOR_PREV = "explain.anchor.previous";
        static final String PATTERN = "explain.pattern";
        static final String GLOBAL_FLAGS = "explain.globalFlags";
        static final String LITERAL = "explain.literal";
        static final String LINEBREAK = "explain.linebreak";
        static final String WORD_BOUNDARY = "explain.wordBoundary";
        static final String CLASS_RANGE = "explain.classRange";
        static final String CLASS_RANGE_KNOWN = "explain.classRange.known";
        static final String CLASS_INCLUSION = "explain.classInclusion";
        static final String CLASS_EXCLUSION = "explain.classExclusion";
        static final String CUSTOM_RANGE = "explain.customRange";
        static final String BACKREFERENCE = "explain.backreference";
        static final String CLASS_INTERSECTION = "explain.classIntersection";
        static final String MODIFIER_POSSESSIVE = "explain.modifier.possessive";
        static final String MODIFIER_LAZY = "explain.modifier.lazy";
        static final String ATOMIC_GROUP = "explain.atomicGroup";
        static final String CAPTURE_GROUP = "explain.captureGroup";
        static final String NON_CAPTURING_GROUP = "explain.nonCapturingGroup";
        static final String ANY_OF = "explain.anyOf";
        static final String ANY_OF_OPTION = "explain.anyOf.option";
        static final String NAMED_CAPTURE = "explain.namedCapture";
        static final String LOCAL_FLAGS = "explain.localFlags";
        static final String CONDITIONAL = "explain.conditional";
        static final String CONDITIONAL_IF = "explain.conditional.if";
        static final String CONDITIONAL_THEN = "explain.conditional.then";
        static final String CONDITIONAL_ELSE = "explain.conditional.else";
        static final String LOOKAHEAD_POS = "explain.lookahead.positive";
        static final String LOOKAHEAD_NEG = "explain.lookahead.negative";
        static final String LOOKBEHIND_POS = "explain.lookbehind.positive";
        static final String LOOKBEHIND_NEG = "explain.lookbehind.negative";
    }

    private final ExplainerTranslator translator;
    private final List<ExplanationNode> nodes = new ArrayList<>();
    private int indentLevel = 0;
    private String pendingQuantifier = "";

    ExplainerVisitor(ResourceBundle bundle) {
        this.translator = new ExplainerTranslator(bundle);
    }

    String getExplanation() {
        return new AsciiTreeRenderer().render(nodes);
    }

    private void addNode(String key, Object... args) {
        String text = translator.translate(key, args);
        if (!pendingQuantifier.isEmpty()) {
            text += " " + translator.translateQuantifier(pendingQuantifier);
            pendingQuantifier = "";
        }
        nodes.add(new ExplanationNode(indentLevel, text));
    }

    private void visitNested(SiftPattern<?> pattern) {
        if (pattern instanceof BaseSiftPattern) {
            indentLevel++;
            ((BaseSiftPattern<?>) pattern).traverse(this);
            indentLevel--;
        }
    }

    @Override
    public void visitAnyChar() {
        addNode(Msg.ANY_CHAR);
    }

    @Override
    public void visitAnchor(String anchor) {
        if (RegexSyntax.START_OF_LINE.equals(anchor)) {
            addNode(Msg.ANCHOR_START);
        } else if (RegexSyntax.END_OF_LINE.equals(anchor)) {
            addNode(Msg.ANCHOR_END);
        } else if (RECURSIVE_FALLBACK.equals(anchor)) {
            addNode(Msg.PATTERN);
        } else if (anchor.startsWith("(?") && anchor.endsWith(")") && !anchor.contains(":")) {
            String flags = anchor.substring(2, anchor.length() - 1);
            addNode(Msg.GLOBAL_FLAGS, translator.formatFlags(flags));
        } else if (RegexSyntax.PREVIOUS_MATCH_END.equals(anchor)) {
            addNode(Msg.ANCHOR_PREV);
        } else {
            addNode(Msg.LITERAL, translator.unescapeAndClean(anchor));
        }
    }

    @Override
    public void visitQuantifier(String quantifier) {
        this.pendingQuantifier = quantifier;
    }

    @Override
    public void visitCharacter(char literal) {
        addNode(Msg.LITERAL, translator.escape(String.valueOf(literal)));
    }

    @Override
    public void visitLinebreak() {
        addNode(Msg.LINEBREAK);
    }

    @Override
    public void visitWordBoundary() {
        addNode(Msg.WORD_BOUNDARY);
    }

    @Override
    public void visitClassRange(String range) {
        String friendly = translator.translateRange(range);
        if (friendly != null) {
            addNode(Msg.CLASS_RANGE_KNOWN, friendly, range);
        } else {
            addNode(Msg.CLASS_RANGE, range);
        }
    }

    @Override
    public void visitClassInclusion(char c, char... extras) {
        StringBuilder sb = new StringBuilder().append(c);
        for (char e : extras) sb.append(e);
        addNode(Msg.CLASS_INCLUSION, translator.escape(sb.toString()));
    }

    @Override
    public void visitClassExclusion(char c, char... extras) {
        StringBuilder sb = new StringBuilder().append(c);
        for (char e : extras) sb.append(e);
        addNode(Msg.CLASS_EXCLUSION, translator.escape(sb.toString()));
    }

    @Override
    public void visitCustomRange(char start, char end) {
        addNode(Msg.CUSTOM_RANGE, String.valueOf(start), String.valueOf(end));
    }

    @Override
    public void visitBackreference(NamedCapture group) {
        addNode(Msg.BACKREFERENCE, group.getName());
    }

    @Override
    public void visitClassIntersection(String intersection) {
        addNode(Msg.CLASS_INTERSECTION, intersection);
    }

    @Override
    public void visitPattern(SiftPattern<? extends Composable> pattern) {
        if (pattern instanceof BaseSiftPattern) {
            ((BaseSiftPattern<?>) pattern).traverse(this);
        }
    }

    @Override
    public void visitPrependPattern(SiftPattern<? extends Composable> pattern) {
        if (pattern instanceof BaseSiftPattern) {
            ((BaseSiftPattern<?>) pattern).traverse(this);
        }
    }

    @Override
    public void visitPossessiveModifier() {
        if (!nodes.isEmpty()) {
            nodes.get(nodes.size() - 1).text += " " + translator.translate(Msg.MODIFIER_POSSESSIVE);
        }
    }

    @Override
    public void visitLazyModifier() {
        if (!nodes.isEmpty()) {
            nodes.get(nodes.size() - 1).text += " " + translator.translate(Msg.MODIFIER_LAZY);
        }
    }

    @Override
    public void visitAtomicGroup(SiftPattern<?> p) {
        addNode(Msg.ATOMIC_GROUP);
        visitNested(p);
    }

    @Override
    public void visitCaptureGroup(SiftPattern<?> p) {
        addNode(Msg.CAPTURE_GROUP);
        visitNested(p);
    }

    @Override
    public void visitNonCapturingGroup(List<? extends SiftPattern<?>> patterns) {
        addNode(Msg.NON_CAPTURING_GROUP);
        for (SiftPattern<?> p : patterns) {
            visitNested(p);
        }
    }

    @Override
    public void visitAnyOf(List<? extends SiftPattern<?>> options) {
        addNode(Msg.ANY_OF);
        indentLevel++;
        for (int i = 0; i < options.size(); i++) {
            addNode(Msg.ANY_OF_OPTION, (i + 1));
            visitNested(options.get(i));
        }
        indentLevel--;
    }

    @Override
    public void visitLookaround(SiftPattern<?> p, boolean pos, boolean head) {
        if (head) addNode(pos ? Msg.LOOKAHEAD_POS : Msg.LOOKAHEAD_NEG);
        else addNode(pos ? Msg.LOOKBEHIND_POS : Msg.LOOKBEHIND_NEG);
        visitNested(p);
    }

    @Override
    public void visitNamedCapture(String name, SiftPattern<?> p) {
        addNode(Msg.NAMED_CAPTURE, name);
        visitNested(p);
    }

    @Override
    public void visitLocalFlags(SiftPattern<?> p, SiftGlobalFlag... flags) {
        List<String> names = new ArrayList<>();
        for (SiftGlobalFlag f : flags) names.add(translator.formatEnum(f.name()));
        addNode(Msg.LOCAL_FLAGS, String.join(", ", names));
        visitNested(p);
    }

    @Override
    public void visitConditional(SiftPattern<?> tc, SiftPattern<?> tp, SiftPattern<?> fc, SiftPattern<?> fp) {
        addNode(Msg.CONDITIONAL);
        indentLevel++;
        addNode(Msg.CONDITIONAL_IF);
        visitNested(tc);
        addNode(Msg.CONDITIONAL_THEN);
        visitNested(tp);
        addNode(Msg.CONDITIONAL_ELSE);
        visitNested(fc);
        if (fp != null) visitNested(fp);
        indentLevel--;
    }

    @Override
    public void visitFeature(RegexFeature f) { /* Ignored */ }
}