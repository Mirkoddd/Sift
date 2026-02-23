package com.mirkoddd.sift.annotations;

import java.util.regex.Pattern;

/**
 * Type-safe wrapper for {@link java.util.regex.Pattern} compilation flags.
 */
public enum SiftMatchFlag {
    CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),
    MULTILINE(Pattern.MULTILINE),
    DOTALL(Pattern.DOTALL),
    UNICODE_CASE(Pattern.UNICODE_CASE),
    UNIX_LINES(Pattern.UNIX_LINES),
    LITERAL(Pattern.LITERAL),
    COMMENTS(Pattern.COMMENTS);

    private final int value;

    SiftMatchFlag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}