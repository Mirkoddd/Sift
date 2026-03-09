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
package com.mirkoddd.sift.core.dsl;

/**
 * Marker interface hierarchy for Sift <b>Phantom Types</b>.
 * <p>
 * These types do not hold data or behavior at runtime. They are strictly used
 * by the Java compiler to enforce Type-Driven Design, distinguishing between
 * reusable fragments and terminal root expressions to prevent logical composition errors.
 * <p>
 * <b>Compile-Time Safety Example:</b>
 * <pre>
 * SiftPattern&lt;Fragment&gt; frag = Sift.exactly(3).digits();
 * SiftPattern&lt;Root&gt; root = Sift.fromStart().exactly(3).digits().andNothingElse();
 * Sift.fromAnywhere().followedBy(frag); // Compiles perfectly!
 * Sift.fromAnywhere().followedBy(root); // COMPILE ERROR: Root cannot be embedded!
 * </pre>
 */
public interface SiftContext {

    /**
     * Represents a pure, reusable regex building block (Fragment).
     * <p>
     * Fragments do not contain absolute boundaries (like ^ or $) or global flags.
     * They are safe to be embedded into other patterns via {@code of()} or {@code followedBy()}.
     */
    interface Fragment extends SiftContext {}

    /**
     * Represents a terminal regex root.
     * <p>
     * Roots contain absolute boundaries or global inline flags. To prevent logical errors
     * (e.g., nesting an end-of-line anchor in the middle of a string), the compiler
     * strictly prohibits embedding a {@code Root} inside another pattern.
     */
    interface Root extends SiftContext {}
}