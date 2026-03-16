package com.mirkoddd.sift.engine.re2j;

/**
 * Package-private dictionary containing RE2J-specific syntax tokens
 */
final class Re2jDictionary {

    private Re2jDictionary() {
        // Prevent instantiation
    }

    static final String NAME = "name";
    static final String JDK_GROUP_PREFIX = "(?<";
    static final char JDK_GROUP_SUFFIX = '>';
    static final String RE2J_GROUP_REPLACEMENT = "(?P<${" + NAME + "}>";
}