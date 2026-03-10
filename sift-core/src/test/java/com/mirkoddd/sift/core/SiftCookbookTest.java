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

import static com.mirkoddd.sift.core.Sift.between;
import static com.mirkoddd.sift.core.Sift.exactly;
import static com.mirkoddd.sift.core.Sift.fromStart;
import static com.mirkoddd.sift.core.Sift.fromWordBoundary;
import static com.mirkoddd.sift.core.Sift.oneOrMore;
import static com.mirkoddd.sift.core.Sift.optional;
import static com.mirkoddd.sift.core.SiftPatterns.anyOf;
import static com.mirkoddd.sift.core.SiftPatterns.literal;
import static com.mirkoddd.sift.core.SiftPatterns.negativeLookahead;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mirkoddd.sift.core.dsl.CharacterConnector;
import com.mirkoddd.sift.core.dsl.Connector;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.Root;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.dsl.VariableCharacterConnector;
import com.mirkoddd.sift.core.dsl.VariableConnector;

/**
 * Validates the real-world examples provided in the COOKBOOK.md file.
 * This ensures that the documentation is always in sync with the actual API
 * and that the proposed patterns work correctly.
 * These recipes demonstrate the "LEGO brick" composition style:
 * breaking down complex regular expressions into small, highly readable,
 * and reusable semantic variables.
 * KEY CONCEPT: fromAnywhere() vs fromStart()
 * - Use Sift.fromAnywhere() to build reusable intermediate blocks. It creates
 * unanchored patterns that can be chained anywhere in the final expression.
 * - Use Sift.fromStart() (and .andNothingElse()) when defining the final,
 * strict structure to anchor the match to the beginning (and end) of the string.
 */
@DisplayName("Cookbook Examples Validation")
class SiftCookbookTest {

    private void assertMatches(String regex, String input) {
        assertTrue(Pattern.compile(regex).matcher(input).find(),
                "The cookbook regex [" + regex + "] failed to match valid input: " + input);
    }

    private void assertDoesNotMatch(String regex, String input) {
        assertFalse(Pattern.compile(regex).matcher(input).find(),
                "The cookbook regex [" + regex + "] matched invalid input: " + input);
    }

    @Test
    @DisplayName("Recipe 1: UUID Validator (Using modular blocks)")
    void testUuidValidatorRecipe() {
        // Define the basic building blocks using fromAnywhere() (in this case is the static Fragment shortcut exactly())
        // This ensures these blocks don't carry a '^' anchor, allowing them
        // to be safely placed in the middle or end of our final chain.
        CharacterConnector<Fragment> hex8 = exactly(8).hexDigits();
        CharacterConnector<Fragment> hex4 = exactly(4).hexDigits();
        CharacterConnector<Fragment> hex12 = exactly(12).hexDigits();
        Connector<Fragment> separator = exactly(1).character('-');

        // Compose reusable intermediate blocks
        Connector<Fragment> hex4andSeparator = hex4.followedBy(separator);

        // define the list of steps to follow in the final pattern
        List<Connector<Fragment>> steps = Arrays.asList(
                separator,
                hex4andSeparator,
                hex4andSeparator,
                hex4andSeparator,
                hex12
        );

        // Let's define the actual regex:
        String actualUuidRegex = hex8
                .followedBy(steps)
                .shake();

        System.out.println(actualUuidRegex);
        // Valid UUID
        assertMatches(actualUuidRegex, "123e4567-e89b-12d3-a456-426614174000");
        assertMatches(actualUuidRegex, "550e8400-e29b-41d4-a716-446655440000");

        // Invalid UUIDs
        assertDoesNotMatch(actualUuidRegex, "123e4567-e89b-12d3-a456-42661417400"); // Too short
        assertDoesNotMatch(actualUuidRegex, "123e4567-e89b-12d3-a456-42661417400G"); // Contains non-hex 'G'
    }

    @Test
    @DisplayName("Recipe 2: Parsing Log Files (TSV Format with discrete semantic parts)")
    void testLogParserRecipe() {
        // Define discrete temporal components
        CharacterConnector<Fragment> year = Sift.fromAnywhere().exactly(4).digits();
        CharacterConnector<Fragment> month = Sift.fromAnywhere().exactly(2).digits();
        CharacterConnector<Fragment> day = Sift.fromAnywhere().exactly(2).digits(); // same as month, but more readable
        Connector<Fragment> dash = Sift.fromAnywhere().character('-'); // you could also use literal("-"), less verbose

        Connector<Fragment> dateBlock = year.followedBy(Arrays.asList(dash, month, dash, day));

        // Define structural components
        Connector<Fragment> tab = Sift.fromAnywhere().tab();
        Connector<Fragment> newline = Sift.fromAnywhere().newline();

        // Define payload components
        Connector<Fragment> logLevel = Sift.fromAnywhere().oneOrMore().upperCaseLetters();
        Connector<Fragment> message = Sift.fromAnywhere().oneOrMore().anyCharacter();

        // Assemble the final pattern like a natural language sentence
        String logParserRegex = dateBlock
                .followedBy(tab)
                .followedBy(logLevel)
                .followedBy(tab)
                .followedBy(message)
                .followedBy(newline)
                .shake();

        // you can also be more concise:
        String altLogParserRegex = dateBlock
                .followedBy(Arrays.asList(tab, logLevel, tab, message, newline))
                .shake();

        // or be more verbose
        String verboseLogParserRegex = dateBlock
                .then().exactly(1).tab()
                .then().exactly(1).of(logLevel)
                .then().tab() // you can omit exactly 1, as it's the default
                .then().exactly(1).of(message) // I want to explicitly use exactly(1) here for readability
                .then().newline()
                .shake();

        // verify that they all produce the same regex
        assertEquals(altLogParserRegex, verboseLogParserRegex);
        assertEquals(altLogParserRegex, logParserRegex);

        // Valid TSV Log line
        assertMatches(logParserRegex, "2024-05-12\tERROR\tDatabase connection failed\n");
        assertMatches(logParserRegex, "2023-11-01\tINFO\tUser logged in successfully\n");

        // Invalid lines
        assertDoesNotMatch(logParserRegex, "2024-05-12 ERROR Database connection failed\n"); // Missing tabs
        assertDoesNotMatch(logParserRegex, "2024-05-12\tERROR\tDatabase connection failed"); // Missing newline at the end
    }

    @Test
    @DisplayName("Recipe 3: Strict Token Validator (Composing prefixes and suffixes)")
    void testStrictTokenValidatorRecipe() {
        // Notice the use of fromStart() here.
        // We enforce that the token MUST begin with this exact prefix,
        // anchoring the entire evaluation to the start of the string.
        CharacterConnector<Root> prefix = fromStart().exactly(2).upperCaseLetters();

        // The rest of the blocks use fromAnywhere() for composition
        CharacterConnector<Fragment> body = between(4, 6).alphanumeric();
        Connector<Fragment> suffix = oneOrMore().punctuation();

        Connector<Fragment> underscore = exactly(1).character('_');

        // Create logical compound blocks
        Connector<Root> prefixWithUnderscore = prefix.followedBy(underscore);
        Connector<Fragment> bodyWithUnderscore = body.followedBy(underscore);

        // Final assembly
        String securityTokenRegex = prefixWithUnderscore
                .followedBy(bodyWithUnderscore, suffix)
                .andNothingElse() // We add andNothingElse() to the end to make it fully strict
                .shake();

        // Valid Tokens
        assertMatches(securityTokenRegex, "AK_aB9fE_!#*");
        assertMatches(securityTokenRegex, "US_1234_?");
        assertMatches(securityTokenRegex, "UK_A1B2C3_!@#");

        // Invalid Tokens
        assertDoesNotMatch(securityTokenRegex, "DIRTY_AK_aB9fE_!#*"); // Caught by fromStart()
        assertDoesNotMatch(securityTokenRegex, "AK_aB9fE_!#*_DIRTY"); // Caught by andNothingElse()
        assertDoesNotMatch(securityTokenRegex, "Ak_aB9fE_!#*"); // Starts with lowercase
        assertDoesNotMatch(securityTokenRegex, "AK_aB9_!#*"); // Alphanumeric part too short (3 chars)
        assertDoesNotMatch(securityTokenRegex, "AK_aB9fE_"); // Missing punctuation at the end
    }

    @Test
    @DisplayName("Recipe 4: IP Address Validation (Highly Reusable Blocks)")
    void testIpAddressValidatorRecipe() {
        // built-in pattern for IPv4
        SiftPattern<Fragment> libIPv4 = SiftCatalog.ipv4();

        // Building the IPv4 structure using the built-in pattern, enforcing anchor start and anchor end
        String ipv4Regex = fromStart()
                .of(libIPv4)
                .andNothingElse()
                .shake();

        System.out.println(ipv4Regex);
        // Valid Structural IPv4
        assertMatches(ipv4Regex, "192.168.1.1");
        assertMatches(ipv4Regex, "10.0.0.255");
        assertMatches(ipv4Regex, "8.8.8.8");

        // Invalid IPv4 structurally
        assertDoesNotMatch(ipv4Regex, "256.168.1"); // Out of bound, octet is 0-255
        assertDoesNotMatch(ipv4Regex, "192.168.1"); // Missing last block
        assertDoesNotMatch(ipv4Regex, "1192.168.1"); // First block too long
        assertDoesNotMatch(ipv4Regex, "192.168.1.1234"); // Last block too long
        assertDoesNotMatch(ipv4Regex, "192.168.1.a"); // Contains letter
    }

    @Test
    @DisplayName("Recipe 5: Data Extraction (Named Captures, Backreferences, and Flags)")
    void testAdvancedExtractionRecipe() {
        // Goal: Parse an XML/HTML style tag, ignoring case, and extract its content.
        // This demonstrates data extraction and relational logic over simple validation.

        // Structural components defined as self-documenting variables (no magic characters)
        char openBracket = '<';
        char closeBracket = '>';
        String closingTagPrefix = "</";

        VariableCharacterConnector<Fragment> tagName = Sift.fromAnywhere().oneOrMore().alphanumeric();
        VariableConnector<Fragment> tagContent = Sift.fromAnywhere().oneOrMore().anyCharacter();

        // 1. Defining Named Captures to extract specific data blocks
        NamedCapture groupTag = SiftPatterns.capture("tag", tagName);
        NamedCapture groupContent = SiftPatterns.capture("content", tagContent);

        Connector<Fragment> openTag = Sift.fromAnywhere()
                .character(openBracket)
                .then().namedCapture(groupTag)
                .then().character(closeBracket);

        Connector<Fragment> content = Sift.fromAnywhere().namedCapture(groupContent);

        Connector<Fragment> closeTag = Sift.fromAnywhere()
                .of(literal(closingTagPrefix))
                .then().backreference(groupTag)
                .then().character(closeBracket);

        // 2. Building the pattern. Notice how SiftGlobalFlag is elegantly applied at the root!
        SiftPattern<Root> htmlTagPattern = Sift
                .filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
                .fromStart()
                .of(openTag)
                .followedBy(content, closeTag)
                .andNothingElse();

        // Input with mixed cases to test the CASE_INSENSITIVE flag
        String input = "<TITLE>My Awesome Cookbook</title>";

        // 4. Using Sift's built-in utility for a quick boolean match check
        assertTrue(htmlTagPattern.matchesEntire(input));

        // 5. Using .sieve() to retrieve the fully compiled java.util.regex.Pattern.
        // This automatically applies the SiftGlobalFlag under the hood.
        Pattern pattern = htmlTagPattern.sieve();
        java.util.regex.Matcher matcher = pattern.matcher(input);

        // 6. Data extraction using standard Java Matcher API powered by Sift
        assertTrue(matcher.find(), "The pattern should find a match");
        assertEquals("TITLE", matcher.group("tag"), "Should extract the exact opening tag");
        assertEquals("My Awesome Cookbook", matcher.group("content"), "Should extract the inner payload");

        // Proof that backreference works: a mismatched closing tag will fail
        assertFalse(pattern.matcher("<TITLE>My Awesome Cookbook</H1>").find());
    }

    @Test
    @DisplayName("Recipe 6: Advanced Assertions (Lookaheads and Alternation)")
    void testLookaheadAndAnyOfRecipe() {
        // Goal 1: Validate a complex password using Lookaheads
        // Must contain at least one uppercase, one digit, and be at least 8 chars long.

        Connector<Fragment> requiresUppercase = Sift.fromAnywhere()
                .of(SiftPatterns.positiveLookahead(
                        Sift.fromAnywhere().zeroOrMore().anyCharacter().then().exactly(1).upperCaseLetters()
                ));

        Connector<Fragment> requiresDigit = Sift.fromAnywhere()
                .of(SiftPatterns.positiveLookahead(
                        Sift.fromAnywhere().zeroOrMore().anyCharacter().then().exactly(1).digits()
                ));

        String passwordPattern = fromStart()
                .of(requiresUppercase)
                .then().exactly(1).of(requiresDigit)
                .then().between(8, 64).anyCharacter()
                .andNothingElse()
                .shake();

        assertMatches(passwordPattern, "SecurePass123");
        assertDoesNotMatch(passwordPattern, "securepass123"); // Missing uppercase
        assertDoesNotMatch(passwordPattern, "SecurePassword"); // Missing digit
        assertDoesNotMatch(passwordPattern, "Sec1"); // Too short

        // Goal 2: Match specific keywords using Alternation (anyOf)
        // Extracting valid HTTP methods
        SiftPattern<Fragment> options = SiftPatterns.anyOf(
                literal("GET"),
                literal("POST"),
                literal("PUT"),
                literal("DELETE"),
                literal("PATCH")
        );

        String httpMethodPattern = fromStart()
                .exactly(1).of(options)
                .andNothingElse()
                .shake();

        assertMatches(httpMethodPattern, "POST");
        assertDoesNotMatch(httpMethodPattern, "OPTIONS"); // Not in the allowed list
    }

    @Test
    @DisplayName("Recipe 7: Security and ReDoS Mitigation (Possessive & Lazy Quantifiers)")
    void testReDoSMitigationRecipe() {
        // Goal: Prevent Catastrophic Backtracking on complex inputs.
        // We use the possessive modifier (.withoutBacktracking()) to tell the engine to NEVER give back
        // matched characters. This prevents infinite loop evaluations.

        String safePayloadExtractor = fromStart()
                .character('{')
                // We use wordCharacters() so it stops capturing BEFORE the '}'.
                // Using .withoutBacktracking() prevents the engine from trying alternative
                // match permutations if the pattern fails later, avoiding ReDoS.
                .then().oneOrMore().wordCharacters().withoutBacktracking()
                .then().character('}')
                .shake();

        // Testing the syntax output (expecting "\w++" or similar based on your engine)
        assertTrue(safePayloadExtractor.contains("++") || safePayloadExtractor.contains("*+"),
                "The pattern should contain possessive regex modifiers");

        assertMatches(safePayloadExtractor, "{secured_payload_data}");

        // This will correctly fail without catastrophic backtracking
        assertDoesNotMatch(safePayloadExtractor, "{invalid payload}");

        // Example of LAZY matching (finding the shortest path instead of greedy)
        String lazyTagExtractor = fromStart()
                .character('<')
                .then().oneOrMore().anyCharacter().asFewAsPossible() // Translates to "+?"
                .then().character('>')
                .shake();

        // Standard regex would match from the first '<' to the very last '>'
        // Lazy matching stops at the first closing '>'
        java.util.regex.Matcher lazyMatcher = java.util.regex.Pattern.compile(lazyTagExtractor).matcher("<first>...<second>");
        assertTrue(lazyMatcher.find());
        assertEquals("<first>", lazyMatcher.group(0), "Lazy modifier should stop at the first closing bracket");
    }

    @Test
    @DisplayName("Should extract only valid hydroxides while ignoring false positives")
    void extractHydroxidesFromText() {

        // 1. Metal element: 1 uppercase letter optionally followed by 1 lowercase (e.g., Na, Ca, Fe)
        Connector<Fragment> metal = exactly(1).upperCaseLetters()
                .followedBy(optional().lowerCaseLetters());

        // 2. Unicode subscripts: from ₀ to ₉ (one or more to support double digits like ₁₂)
        Connector<Fragment> subscripts = oneOrMore().range('₀', '₉');

        // 3. Simple OH group: must NOT be followed by a subscript to prevent partial matches like "CaOH₂"
        Connector<Fragment> simpleOH = exactly(1).of(literal("OH"))
                .followedBy(negativeLookahead(subscripts));

        // 4. Complex OH group: requires parentheses and at least one subscript (e.g., "(OH)₂")
        Connector<Fragment> complexOH = exactly(1).of(literal("(OH)"))
                .followedBy(subscripts);

        // 5. Logical OR: accepts either the simple or the complex hydroxide variant
        SiftPattern<Fragment> hydroxideGroup = anyOf(simpleOH, complexOH);

        // 6. Final assembly: anchored to word boundaries to avoid matching inside other random words
        String hydroxideRegex = fromWordBoundary()
                .followedBy(metal)
                .followedBy(hydroxideGroup)
                .shake();

        // 7. The "chemistry book" text to analyze, packed with valid targets and tricky false positives
        String text =
                "In this reaction, sodium reacts with water to form NaOH," +
                "while calcium forms Ca(OH)₂. Not to be confused with exclamations like OH!" +
                "Iron(III) hydroxide is Fe(OH)₃." +
                "Typos like CaOH₂ (missing parentheses) are grammatically invalid and should be ignored." +
                "To stress-test the parser with double digits, a fictional Xy(OH)₁₂ would also match." +
                "Hydrogen H₂ is not a hydroxide. KOH should pass tho";

        // 8. Standard Java Regex execution
        Pattern pattern = Pattern.compile(hydroxideRegex);
        Matcher matcher = pattern.matcher(text);

        List<String> extractedMolecules = new ArrayList<>();
        while (matcher.find()) {
            extractedMolecules.add(matcher.group());
        }

        // 9. Validation: The expected list remains pristine, proving the parser's strict domain logic!
        List<String> expected = Arrays.asList("NaOH", "Ca(OH)₂", "Fe(OH)₃", "Xy(OH)₁₂", "KOH");

        assertEquals(expected, extractedMolecules, "Extraction failed: Regex matched incorrect patterns or missed valid ones");
    }
}