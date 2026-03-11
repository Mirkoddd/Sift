package com.mirkoddd.sift.core;

import com.mirkoddd.sift.core.dsl.CharacterConnector;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static com.mirkoddd.sift.core.Sift.exactly;
import static com.mirkoddd.sift.core.SiftPatterns.anyOf;
import static com.mirkoddd.sift.core.SiftPatterns.nesting;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Sift Nesting Builder Test Suite")
class SiftNestingTest {

    @Test
    @DisplayName("Should successfully parse nested parentheses up to the configured depth")
    void testValidNesting() {
        SiftPattern<Fragment> mathExpression = nesting(3)
                .using(Delimiter.PARENTHESES)
                .containing(exactly(1).upperCaseLetters());

        System.out.println("Generated Nested Regex (Depth 3): \n" + mathExpression.shake());

        // --- Happy Paths ---
        assertTrue(mathExpression.sieve().matcher("(A)").matches(), "Failed on Level 1 (A)");
        assertTrue(mathExpression.sieve().matcher("(A(B)C)").matches(), "Failed on Level 2 (A(B)C)");
        assertTrue(mathExpression.sieve().matcher("(A(B(C))D)").matches(), "Failed on Level 3 (A(B(C))D)");

        // --- Extraction ---
        String textToExtract = "Some prefix text (A(B)C) and some suffix";
        Matcher extractionMatcher = mathExpression.sieve().matcher(textToExtract);

        assertTrue(extractionMatcher.find());
        assertEquals("(A(B)C)", extractionMatcher.group(), "Partial match error! It stopped at the first closing parenthesis.");
    }

    @Test
    @DisplayName("Should parse nested structures using custom multicharacter delimiters")
    void testCustomDelimiters() {
        // Example: Parsing nested multi-line comments /* ... */
        Delimiter customBlock = Delimiter.custom("/*", "*/");

        SiftPattern<Fragment> commentParser = nesting(3)
                .using(customBlock)
                .containing(exactly(1).upperCaseLetters());

        assertTrue(commentParser.sieve().matcher("/*A*/").matches(), "Failed on Level 1 Custom Delimiter");
        assertTrue(commentParser.sieve().matcher("/*A/*B*/C*/").matches(), "Failed on Level 2 Custom Delimiter");

        // Fails correctly if custom delimiters are mismatched
        assertFalse(commentParser.sieve().matcher("/*A/*B*/").matches(), "Should fail on missing custom closing delimiter");
    }

    @Test
    @DisplayName("Should cleanly fail when delimiters are unbalanced or mixed")
    void testEdgeCasesUnbalancedAndMixed() {
        SiftPattern<Fragment> mathExpression = nesting(3)
                .using(Delimiter.PARENTHESES)
                .containing(exactly(1).upperCaseLetters());

        // Edge case 1: Missing the final closing parenthesis
        assertFalse(mathExpression.sieve().matcher("(A(B)").matches(), "Should fail on missing closing parenthesis");

        // Edge case 2: Mixed or incorrect delimiter
        assertFalse(mathExpression.sieve().matcher("(A(B)]").matches(), "Should fail on mixed delimiters");
    }

    @Test
    @DisplayName("Should safely fail matching when exceeding maximum configured depth")
    void testEdgeCaseMaxDepthExceeded() {
        // We intentionally set a shallow depth (2)
        SiftPattern<Fragment> shallowExpression = nesting(2)
                .using(Delimiter.PARENTHESES)
                .containing(exactly(1).upperCaseLetters());

        assertTrue(shallowExpression.sieve().matcher("(A(B)C)").matches(), "Level 2 should pass");

        // Edge case 3: The text goes down to depth 3.
        // Our Base Case (?!) must cause the match to fail cleanly without throwing runtime errors.
        assertFalse(shallowExpression.sieve().matcher("(A(B(C))D)").matches(), "Should safely fail when exceeding max depth");
    }

    @Test
    @DisplayName("Should throw expected Exceptions on invalid states and parameters")
    void testBuilderSafetyAndExceptions() {
        CharacterConnector<Fragment> oneLetter = exactly(1).letters();
        // 1. Missing Delimiter state check
        IllegalStateException stateException = assertThrows(IllegalStateException.class, () ->
                nesting(3).containing(oneLetter));
        assertEquals("A Delimiter pair must be specified using .using() before defining content.", stateException.getMessage());

        // 2. Depth out of bounds (too low)
        IllegalArgumentException lowDepthEx = assertThrows(IllegalArgumentException.class, () ->
                nesting(1).using(Delimiter.PARENTHESES).containing(oneLetter));
        assertTrue(lowDepthEx.getMessage().contains("between 2 and 10"), "Should warn about minimum depth");

        // 3. Depth out of bounds (too high - JVM protection)
        IllegalArgumentException highDepthEx = assertThrows(IllegalArgumentException.class, () ->
                nesting(11).using(Delimiter.PARENTHESES).containing(oneLetter));
        assertTrue(highDepthEx.getMessage().contains("between 2 and 10"), "Should warn about maximum depth");

        // 4. Null checks for Custom Delimiters
        assertThrows(NullPointerException.class, () -> Delimiter.custom(null, "*/"));
        assertThrows(NullPointerException.class, () -> Delimiter.custom("/*", null));

        // 5. Null checks for Builder methods
        assertThrows(NullPointerException.class, () -> nesting(3).using(null));
        assertThrows(NullPointerException.class, () -> nesting(3).using(Delimiter.PARENTHESES).containing(null));
    }

    @Test
    @DisplayName("Should successfully parse deeply nested JSON-like objects")
    void testJsonObjectNesting() {
        SiftPattern<Fragment> jsonPrimitives = anyOf(
                exactly(1).letters(),
                exactly(1).digits(),
                exactly(1).whitespace(),
                exactly(1).character('"'),
                exactly(1).character(':'),
                exactly(1).character(','),
                exactly(1).character('['),
                exactly(1).character(']')
        );

        SiftPattern<Fragment> jsonParser = nesting(4)
                .using(Delimiter.BRACES)
                .containing(jsonPrimitives);

        System.out.println("\nGenerated JSON Regex (Depth 4): \n" + jsonParser.shake());

        String flatJson = "{\n  \"name\": \"Mirko\",\n  \"age\": 39\n}";
        assertTrue(jsonParser.sieve().matcher(flatJson).matches(), "Failed on Flat JSON");

        String nestedJson = "{\"user\": {\"name\": \"Mirko\", \"role\": \"admin\"}, \"status\": 200}";
        assertTrue(jsonParser.sieve().matcher(nestedJson).matches(), "Failed on Nested JSON (Depth 2)");

        String deepJson = "{\"data\": {\"users\": {\"first\": {\"id\": 1}}}}";
        assertTrue(jsonParser.sieve().matcher(deepJson).matches(), "Failed on Deep JSON (Depth 4)");

        String invalidJson = "{\"user\": {\"name\": \"Mirko\"}";
        assertFalse(jsonParser.sieve().matcher(invalidJson).matches(), "Should correctly fail on missing closing brace");
    }

    @Test
    @DisplayName("Should parse complex real-world JSON by composing public Nesting builders")
    void testComplexMixedJsonParsing() {
        // 1. Define the basic characters allowed in a JSON payload
        // Alphanumerics covers [a-zA-Z0-9], while we keep the structural JSON punctuation explicit.
        SiftPattern<Fragment> jsonPrimitives = anyOf(
                exactly(1).alphanumeric(),
                exactly(1).whitespace(),
                exactly(1).character('"'),
                exactly(1).character(':'),
                exactly(1).character(','),
                exactly(1).character('.')
        );

        // 2. Bottom-up Composition: Instead of raw recursion, we compose public builders.
        // Step A: A simple JSON Object (depth 2 minimum required by our engine)
        SiftPattern<Fragment> simpleJsonObject = nesting(2)
                .using(Delimiter.BRACES)
                .containing(jsonPrimitives);

        // Step B: A JSON Array (depth 2) that can contain primitives AND simple JSON Objects.
        SiftPattern<Fragment> jsonArray = nesting(2)
                .using(Delimiter.BRACKETS)
                .containing(anyOf(jsonPrimitives, simpleJsonObject));

        // Step C: The Root JSON Object (depth 2) that can contain primitives AND the JSON Array.
        SiftPattern<Fragment> complexJsonParser = nesting(2)
                .using(Delimiter.BRACES)
                .containing(anyOf(jsonPrimitives, jsonArray));

        // 3. A realistic payload: Root Object -> Array -> Simple Objects
        String realWorldPayload = "{\n" +
                "  \"company\": \"Sift Inc.\",\n" +
                "  \"employees\": [\n" +
                "    { \"name\": \"Mirko\", \"role\": \"Android Lead\" },\n" +
                "    { \"name\": \"Bob\", \"role\": \"Clean Coder\" }\n" +
                "  ]\n" +
                "}";

        // 4. Verify that the regex engine parses the composed structure
        assertTrue(complexJsonParser.sieve().matcher(realWorldPayload).matches(),
                "Failed to parse the composed JSON structure");

        // 5. Real-world extraction scenario: Fishing the JSON array out of a dirty log string.
        String dirtyLogText = "Server response: 200 OK - Payload: [ { \"id\": 1 }, { \"id\": 2 } ] - Connection closed.";
        Matcher extractionMatcher = jsonArray.sieve().matcher(dirtyLogText);

        assertTrue(extractionMatcher.find(), "Failed to extract the JSON array from the dirty log");
        assertEquals("[ { \"id\": 1 }, { \"id\": 2 } ]", extractionMatcher.group(),
                "Did not cleanly extract the nested array structure");

        System.out.println("Cleanly extracted from dirty log: " + extractionMatcher.group());
    }
}