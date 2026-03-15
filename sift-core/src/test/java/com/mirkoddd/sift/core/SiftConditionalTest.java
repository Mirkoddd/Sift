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

import com.mirkoddd.sift.core.dsl.CharacterConnector;
import com.mirkoddd.sift.core.dsl.Connector;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.mirkoddd.sift.core.Sift.exactly;
import static com.mirkoddd.sift.core.SiftPatterns.ifFollowedBy;
import static com.mirkoddd.sift.core.SiftPatterns.ifNotFollowedBy;
import static com.mirkoddd.sift.core.SiftPatterns.ifNotPrecededBy;
import static com.mirkoddd.sift.core.SiftPatterns.ifPrecededBy;
import static com.mirkoddd.sift.core.SiftPatterns.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Sift Conditionals (If-Then-Else) Test Suite")
class SiftConditionalsTest {

    @Test
    @DisplayName("Should correctly generate an If-Without-Else block (otherwiseNothing)")
    void testIfWithoutElse() {
        // Emulates an IF condition where the FALSE branch consumes nothing and simply moves on.
        SiftPattern<Fragment> optionalPrefix = ifFollowedBy(literal("ID-"))
                .thenUse(exactly(3).digits())
                .otherwiseNothing();

        String regex = optionalPrefix.shake();
        // Expectation: Uses a positive lookahead for TRUE, and a negative lookahead for FALSE with an empty branch.
        assertEquals("(?:(?=ID-)[0-9]{3}|(?!ID-))", regex);
    }

    @Test
    @DisplayName("Should correctly generate a standard If-Then-Else block")
    void testIfThenElse() {
        SiftPattern<Fragment> dollar = literal("$");
        SiftPattern<Fragment> euro = literal("€");

        CharacterConnector<Fragment> twoDigits = exactly(2).digits();
        Connector<Fragment> dollarFollowedBy2digits = exactly(1).of(dollar).followedBy(twoDigits);
        Connector<Fragment> euroFollowedBy2digits = exactly(1).of(euro).followedBy(twoDigits);

        // Tests the standard conditional routing logic.
        SiftPattern<Fragment> currencyRule = ifFollowedBy(dollar)
                .thenUse(dollarFollowedBy2digits)
                .otherwiseUse(euroFollowedBy2digits);

        String regex = currencyRule.shake();
        assertEquals("(?:(?=\\$)\\$[0-9]{2}|(?!\\$)€[0-9]{2})", regex);
    }

    @Test
    @DisplayName("BOSS FIGHT: Deeply nested Else-If chain with text extraction")
    void testDeepElseIfChainExecution() {
        // Scenario: Parsing serial numbers based on the first letter.
        // A -> must be followed by 3 digits
        // B -> must be followed by 3 letters
        // Any other letter -> must be followed by 3 hyphens

        SiftPattern<Fragment> a = literal("A");
        SiftPattern<Fragment> b = literal("B");
        SiftPattern<Fragment> hyphens = literal("---");

        Connector<Fragment> aFollowedBy3digits = exactly(1).of(a).followedBy(exactly(3).digits());
        Connector<Fragment> bFollowedBy3letters = exactly(1).of(b).followedBy(exactly(3).upperCaseLetters());

        // We explicitly require a letter before hyphens to ensure "C---" is captured completely.
        Connector<Fragment> letterFollowedByHyphens = exactly(1).upperCaseLetters().followedBy(hyphens);

        // The state machine safely wraps multiple else-if conditions, nesting them Matryoshka-style
        // inside the previous False branch.
        SiftPattern<Fragment> serialRule = ifFollowedBy(a)
                .thenUse(aFollowedBy3digits)
                .otherwiseIfFollowedBy(b)
                .thenUse(bFollowedBy3letters)
                .otherwiseUse(letterFollowedByHyphens);

        // Structural validation: Ensures the ConditionalAssembler resolves functional closures correctly.
        String expectedRegex = "(?:(?=A)A[0-9]{3}|(?!A)(?:(?=B)B[A-Z]{3}|(?!B)[A-Z]---))";
        assertEquals(expectedRegex, serialRule.shake(), "The internal State Machine failed to nest the groups correctly!");

        // --- EXECUTION PHASE ---
        String text = "Valid targets: A123, BXYZ, C---. " +
                "Invalid targets: AXYZ (A needs digits), B123 (B needs letters), C123.";

        // We use the word boundary only at the start.
        // Appending a word boundary at the end would cause the "C---" target to fail,
        // as the hyphen '-' combined with the dot '.' doesn't trigger \b in Java's native engine.
        Connector<Fragment> extractionPattern = Sift.fromWordBoundary()
                .followedBy(serialRule);

        List<String> results = extractionPattern.extractAll(text);

        // Ensures the emulated conditionals actually work at runtime within the Java Regex engine.
        List<String> expectedResults = Arrays.asList("A123", "BXYZ", "C---");
        assertEquals(expectedResults, results, "The Regex Engine failed to execute the conditional logic properly.");
    }

    @Test
    @DisplayName("Should handle Negative Lookahead conditions (Solo-Else scenario)")
    void testNegativeCondition() {
        // Emulates an inverted logic condition (executes code only if the pattern is NOT followed).
        SiftPattern<Fragment> notAdminRule = ifNotFollowedBy(literal("ADMIN_"))
                .thenUse(exactly(4).digits())
                .otherwiseNothing();

        String regex = notAdminRule.shake();
        // Expectation: The Negative Lookahead triggers the TRUE branch, while the Positive Lookahead triggers the empty FALSE branch.
        assertEquals("(?:(?!ADMIN_)[0-9]{4}|(?=ADMIN_))", regex);
    }

    @Test
    @DisplayName("Should handle Positive Lookbehind conditions (ifPrecededBy)")
    void testPositiveLookbehindCondition() {
        // Scenario: If the number is preceded by "USD", it must be exactly 2 digits.
        // Otherwise, it must be exactly 3 digits.
        SiftPattern<Fragment> lookbehindRule = ifPrecededBy(literal("USD"))
                .thenUse(exactly(2).digits())
                .otherwiseUse(exactly(3).digits());

        String regex = lookbehindRule.shake();
        // Expectation: Uses positive lookbehind (?<=...) for TRUE, and negative lookbehind (?<!...) for FALSE.
        assertEquals("(?:(?<=USD)[0-9]{2}|(?<!USD)[0-9]{3})", regex);
    }

    @Test
    @DisplayName("Should handle Negative Lookbehind conditions (ifNotPrecededBy)")
    void testNegativeLookbehindCondition() {
        // Scenario: Extract a 4-digit code ONLY IF it is NOT preceded by the word "FAIL".
        // This generates a Solo-Else structure powered by a negative lookbehind.
        SiftPattern<Fragment> notPrecededRule = ifNotPrecededBy(literal("FAIL"))
                .thenUse(exactly(4).digits())
                .otherwiseNothing();

        String regex = notPrecededRule.shake();
        // Expectation: The Negative Lookbehind triggers the TRUE branch, while the Positive Lookahead triggers the empty FALSE branch.
        assertEquals("(?:(?<!FAIL)[0-9]{4}|(?<=FAIL))", regex);
    }
}