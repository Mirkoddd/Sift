package com.mirkoddd.sift.core.dsl;

/**
 * Represents a pure, reusable regex building block (Fragment).
 * <p>
 * Fragments do not contain absolute boundaries (like ^ or $) or global flags.
 * They are safe to be embedded into other patterns via {@code of()} or {@code followedBy()}.
 */
public interface Fragment extends SiftContext {
}
