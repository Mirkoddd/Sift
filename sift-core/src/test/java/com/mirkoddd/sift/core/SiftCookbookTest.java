package com.mirkoddd.sift.core;

import static com.mirkoddd.sift.core.SiftPatterns.literal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        // Define the basic building blocks using fromAnywhere().
        // This ensures these blocks don't carry a '^' anchor, allowing them
        // to be safely placed in the middle or end of our final chain.
        var hex8 = Sift.fromAnywhere().exactly(8).hexDigits();
        var hex4 = Sift.fromAnywhere().exactly(4).hexDigits();
        var hex12 = Sift.fromAnywhere().exactly(12).hexDigits();
        var separator = Sift.fromAnywhere().character('-');

        // Compose reusable intermediate blocks
        var hex4andSeparator = hex4.followedBy(separator);

        // Let's define the actual regex:
        String actualUuidRegex = hex8
                .followedBy(
                        separator,
                        hex4andSeparator,
                        hex4andSeparator,
                        hex4andSeparator,
                        hex12)
                .shake();

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
        var year = Sift.fromAnywhere().exactly(4).digits();
        var month = Sift.fromAnywhere().exactly(2).digits();
        var day = Sift.fromAnywhere().exactly(2).digits(); // same as month, but more readable
        var dash = Sift.fromAnywhere().character('-'); // you could also use literal("-"), less verbose

        var dateBlock = year.followedBy(dash, month, dash, day);

        // Define structural components
        var tab = Sift.fromAnywhere().tab();
        var newline = Sift.fromAnywhere().newline();

        // Define payload components
        var logLevel = Sift.fromAnywhere().oneOrMore().uppercaseLetters();
        var message = Sift.fromAnywhere().oneOrMore().anyCharacter();

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
                .followedBy(tab, logLevel, tab, message, newline)
                .shake();

        // or be more verbose
        String verboseLogParserRegex = dateBlock
                .then().exactly(1).tab()
                .then().exactly(1).pattern(logLevel)
                .then().tab() // you can omit exactly 1, as it's the default
                .then().pattern(message)
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
        var prefix = Sift.fromStart().exactly(2).uppercaseLetters();

        // The rest of the blocks use fromAnywhere() for composition
        var body = Sift.fromAnywhere().between(4, 6).alphanumeric();
        var suffix = Sift.fromAnywhere().oneOrMore().punctuation();

        var underscore = Sift.fromAnywhere().character('_');

        // Create logical compound blocks
        var prefixWithUnderscore = prefix.followedBy(underscore);
        var bodyWithUnderscore = body.followedBy(underscore);

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
        var libIPv4 = SiftCatalog.ipv4();

        // Building the IPv4 structure using the built-in pattern, enforcing anchor start and anchor end
        String ipv4Regex = Sift.fromStart()
                .pattern(libIPv4)
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

        var tagName = Sift.fromAnywhere().oneOrMore().alphanumeric();
        var tagContent = Sift.fromAnywhere().oneOrMore().anyCharacter();

        // 1. Defining Named Captures to extract specific data blocks
        var groupTag = SiftPatterns.capture("tag", tagName);
        var groupContent = SiftPatterns.capture("content", tagContent);

        var openTag = Sift.fromAnywhere()
                .character(openBracket)
                .then().namedCapture(groupTag)
                .then().character(closeBracket);

        var content = Sift.fromAnywhere().namedCapture(groupContent);

        var closeTag = Sift.fromAnywhere()
                .pattern(literal(closingTagPrefix))
                .then().backreference(groupTag)
                .then().character(closeBracket);

        // 2. Building the pattern. Notice how SiftGlobalFlag is elegantly applied at the root!
        var htmlTagPattern = Sift
                .filteringWith(SiftGlobalFlag.CASE_INSENSITIVE)
                .fromStart()
                .pattern(openTag)
                .followedBy(content, closeTag)
                .andNothingElse();

        // Input with mixed cases to test the CASE_INSENSITIVE flag
        String input = "<TITLE>My Awesome Cookbook</title>";

        // 4. Using Sift's built-in utility for a quick boolean match check
        assertTrue(htmlTagPattern.matches(input));

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
}