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

import static com.mirkoddd.sift.core.SiftPatterns.literal;

import com.mirkoddd.sift.core.dsl.Composable;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import com.mirkoddd.sift.core.engine.SiftEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExplainerVisitorTest {

    private ExplainerVisitor visitor;

    private final SiftPattern<Fragment> dummyNode = literal("X");


    @BeforeEach
    void setUp() {
        ResourceBundle.Control control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
        ResourceBundle bundle = ResourceBundle.getBundle("sift_messages", Locale.ENGLISH, control);
        visitor = new ExplainerVisitor(bundle);
    }

    @Test
    void testBasicElementsAndControlCharsEscaping() {
        visitor.visitAnyChar();
        visitor.visitLinebreak();
        visitor.visitWordBoundary();
        visitor.visitCharacter('\n');
        visitor.visitCharacter('\t');

        String[] lines = visitor.getExplanation().split("\n");
        assertEquals("┌─ Matches any character", lines[0]);
        assertEquals("├─ Matches a line break", lines[1]);
        assertEquals("├─ Matches a word boundary", lines[2]);
        assertEquals("├─ Matches the literal character '\\n'", lines[3]);
        assertEquals("└─ Matches the literal character '\\t'", lines[4]);
    }

    @Test
    void testAnchorsAndGlobalFlags() {
        visitor.visitAnchor(RegexSyntax.START_OF_LINE);
        visitor.visitAnchor(RegexSyntax.END_OF_LINE);
        visitor.visitAnchor(RegexSyntax.PREVIOUS_MATCH_END);
        visitor.visitAnchor("(?!)");
        visitor.visitAnchor("(?im)");
        visitor.visitAnchor("\\<\\/");

        String[] lines = visitor.getExplanation().split("\n");
        assertEquals("┌─ Starts at the beginning of the line", lines[0]);
        assertEquals("├─ Ends at the end of the line", lines[1]);
        assertEquals("├─ Starts at the end of the previous match", lines[2]);
        assertEquals("├─ Matches the following pattern:", lines[3]);
        assertEquals("├─ Applies inline global flags: Case Insensitive, Multiline", lines[4]);
        assertEquals("└─ Matches the literal character '</'", lines[5]);
    }

    @Test
    void testQuantifiersAndModifiers() {
        visitor.visitQuantifier("?");
        visitor.visitCharacter('A');

        visitor.visitQuantifier("*");
        visitor.visitCharacter('B');
        visitor.visitLazyModifier();

        visitor.visitQuantifier("+");
        visitor.visitCharacter('C');
        visitor.visitPossessiveModifier();

        visitor.visitQuantifier("{3}");
        visitor.visitCharacter('D');

        visitor.visitQuantifier("{3,}");
        visitor.visitCharacter('E');

        visitor.visitQuantifier("{3,5}");
        visitor.visitCharacter('F');

        visitor.visitQuantifier("weird");
        visitor.visitCharacter('G');

        String[] lines = visitor.getExplanation().split("\n");
        assertEquals("┌─ Matches the literal character 'A' optionally (0 or 1 time)", lines[0]);
        assertEquals("├─ Matches the literal character 'B' zero or more times (lazy: matches as few times as possible)", lines[1]);
        assertEquals("├─ Matches the literal character 'C' one or more times (possessive: matches as many times as possible, no backtracking)", lines[2]);
        assertEquals("├─ Matches the literal character 'D' exactly 3 times", lines[3]);
        assertEquals("├─ Matches the literal character 'E' at least 3 times", lines[4]);
        assertEquals("├─ Matches the literal character 'F' between 3 and 5 times", lines[5]);
        assertEquals("└─ Matches the literal character 'G'  (quantifier: weird)", lines[6]);
    }

    @Test
    void testClasses() {
        visitor.visitClassInclusion('a', 'b', 'c');
        visitor.visitClassExclusion('x', 'y', 'z');
        visitor.visitCustomRange('A', 'Z');
        visitor.visitClassIntersection("a-z");

        String[] lines = visitor.getExplanation().split("\n");
        assertEquals("┌─ Matches any of the included characters (abc)", lines[0]);
        assertEquals("├─ Matches any character EXCEPT the excluded ones (xyz)", lines[1]);
        assertEquals("├─ Matches a character in the range from 'A' to 'Z'", lines[2]);
        assertEquals("└─ Intersected with class (a-z)", lines[3]);
    }

    @Test
    void testGroupsAndReferences() {
        visitor.visitCaptureGroup(dummyNode);
        visitor.visitNonCapturingGroup(Arrays.asList(dummyNode, dummyNode));
        visitor.visitAtomicGroup(dummyNode);
        visitor.visitNamedCapture("myGroup", dummyNode);

        NamedCapture mockCapture = SiftPatterns.capture("myGroup", dummyNode);
        visitor.visitBackreference(mockCapture);

        String result = visitor.getExplanation();
        assertEquals(10, result.split("\n").length);
    }

    @Test
    void testLookarounds() {
        visitor.visitLookaround(dummyNode, true, true);
        visitor.visitLookaround(dummyNode, false, true);
        visitor.visitLookaround(dummyNode, true, false);
        visitor.visitLookaround(dummyNode, false, false);

        String result = visitor.getExplanation();
        assertEquals(8, result.split("\n").length);
    }

    @Test
    void testConditionalsAndAlternations() {
        visitor.visitAnyOf(Arrays.asList(dummyNode, dummyNode));
        visitor.visitConditional(dummyNode, dummyNode, dummyNode, dummyNode);

        String result = visitor.getExplanation();
        assertEquals(13, result.split("\n").length);
    }

    @Test
    void testLocalFlagsAndFeatures() {
        visitor.visitLocalFlags(dummyNode, SiftGlobalFlag.CASE_INSENSITIVE, SiftGlobalFlag.MULTILINE);
        visitor.visitFeature(null); // Should do nothing

        String result = visitor.getExplanation();
        assertEquals("└─ With local flags Case Insensitive, Multiline:\n  " +
                                 " └─ Matches the literal character 'X'", result);
    }

    @Test
    void testPatternTraversals() {
        visitor.visitPattern(dummyNode);
        visitor.visitPrependPattern(dummyNode);

        String result = visitor.getExplanation();
        System.out.println(result);
        assertEquals("┌─ Matches the literal character 'X'\n└─ Matches the literal character 'X'", result);
    }

    @Test
    void testEmptyExplanation() {
        assertEquals("", visitor.getExplanation());
    }

    @Test
    void testVisitNestedWithNonBaseSiftPattern() {
        SiftPattern<Composable> foreignPattern = new SiftPattern<Composable>() {
            @Override
            public Object ___internal_lock___() {
                return null;
            }

            @Override
            public String shake() {
                return "";
            }

            @Override
            public SiftCompiledPattern sieveWith(SiftEngine engine) {
                return null;
            }

            @Override
            public SiftPattern<Composable> preventBacktracking() {
                return null;
            }
        };

        visitor.visitCaptureGroup(foreignPattern);

        String result = visitor.getExplanation();

        assertEquals("└─ Capturing group:", result);
    }

    @Test
    void testMalformedQuantifiersForCoverage() {
        visitor.visitQuantifier("{3,5");
        visitor.visitCharacter('X');

        visitor.visitQuantifier("{3,,5}");
        visitor.visitCharacter('Y');

        String[] lines = visitor.getExplanation().split("\n");

        assertEquals("┌─ Matches the literal character 'X'  (quantifier: {3,5)", lines[0]);

        assertEquals("└─ Matches the literal character 'Y' at least 3 times", lines[1]);
    }

    @Test
    void testAnchorWithColonForCoverage() {
        visitor.visitAnchor("(?:)");

        String result = visitor.getExplanation();

        assertEquals("└─ Matches the literal character '(?:)'", result);
    }

    @Test
    void testPatternAndModifiersAllBranches() {
        visitor.visitPossessiveModifier();
        visitor.visitLazyModifier();

        SiftPattern<Composable> foreignPattern = new SiftPattern<Composable>() {
            @Override
            public Object ___internal_lock___() {
                return null;
            }

            @Override
            public String shake() {
                return "";
            }

            @Override
            public SiftCompiledPattern sieveWith(SiftEngine engine) {
                return null;
            }

            @Override
            public SiftPattern<Composable> preventBacktracking() {
                return null;
            }
        };
        visitor.visitPattern(foreignPattern);
        visitor.visitPrependPattern(foreignPattern);

        assertEquals("", visitor.getExplanation());



        SiftPattern<Fragment> validPattern = SiftPatterns.literal("X");

        visitor.visitPattern(validPattern);

        visitor.visitPossessiveModifier();

        visitor.visitPrependPattern(validPattern);

        visitor.visitLazyModifier();

        String[] outputLines = visitor.getExplanation().split("\n");
        assertEquals(2, outputLines.length);
    }

    @Test
    void testNonCapturingGroupAllBranches() {
        SiftPattern<?> validPattern = SiftPatterns.literal("X");

        SiftPattern<Composable> alienPattern = new SiftPattern<Composable>() {
            @Override
            public Object ___internal_lock___() {
                return null;
            }

            @Override
            public String shake() {
                return "";
            }

            @Override
            public SiftCompiledPattern sieveWith(SiftEngine engine) {
                return null;
            }

            @Override
            public SiftPattern<Composable> preventBacktracking() {
                return null;
            }
        };

        visitor.visitNonCapturingGroup(Arrays.asList(validPattern, alienPattern));

        String result = visitor.getExplanation();

        assertEquals("└─ Non-capturing group:\n   └─ Matches the literal character 'X'", result);
    }

    @Test
    void testConditionalWithAndWithoutFalsePattern() {
        SiftPattern<?> validNode = SiftPatterns.literal("X");

        visitor.visitConditional(validNode, validNode, validNode, null);

        visitor.visitConditional(validNode, validNode, validNode, validNode);

        String result = visitor.getExplanation();

        assertFalse(result.isEmpty());
    }

    @Test
    void testTranslateFlagsAllBranches() {
        SiftGlobalFlag realFlag = SiftGlobalFlag.values()[0];
        String realSymbol = String.valueOf(realFlag.getSymbol());

        String mixedAnchor = "(?" + realSymbol + "@)";

        visitor.visitAnchor(mixedAnchor);

        String result = visitor.getExplanation();

        assertTrue(result.contains("@"));
        assertFalse(result.isEmpty());
    }

    @Test
    void testAnchorMissingClosingParenthesisForCoverage() {
        visitor.visitAnchor("(?im");

        String result = visitor.getExplanation();

        assertEquals("└─ Matches the literal character '(?im'", result);
    }
}