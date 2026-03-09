package com.mirkoddd.sift.core.dsl;

/**
 * Represents a terminal regex root.
 * <p>
 * Roots contain absolute boundaries or global inline flags. To prevent logical errors
 * (e.g., nesting an end-of-line anchor in the middle of a string), the compiler
 * strictly prohibits embedding a {@code Root} inside another pattern.
 */
public interface Root extends SiftContext {
}
