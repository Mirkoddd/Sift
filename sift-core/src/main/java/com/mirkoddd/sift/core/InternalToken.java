package com.mirkoddd.sift.core;

/**
 * Package-private token used to seal the SiftPattern hierarchy in Java 8.
 * Prevents external implementations and ensures DSL safety guarantees.
 */
public final class InternalToken {
    private InternalToken() {}
}