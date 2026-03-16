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

import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.RegexFeature;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import com.mirkoddd.sift.core.engine.SiftEngine;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Internal foundation for all Sift patterns.
 * <p>
 * This class centralizes the <b>Double-Checked Locking</b> logic for thread-safe caching
 * of the generated regex string and its associated structural features.
 * <p>
 * By completely decoupling the string generation from the runtime execution engine,
 * this core class implements the Dependency Inversion Principle, allowing Sift
 * to act as a framework-agnostic regex builder.
 *
 * @param <Ctx> The specific context type for state machine validation.
 */
abstract class BaseSiftPattern<Ctx extends SiftContext> implements SiftPattern<Ctx>, PatternMetadata {

    private volatile String cachedRegex = null;
    private volatile Set<RegexFeature> cachedFeatures = null;

    @Override
    public final Object ___internal_lock___() {
        return InternalToken.INSTANCE;
    }

    @Override
    public final String shake() {
        if (cachedRegex == null) {
            synchronized (this) {
                if (cachedRegex == null) {
                    String computedRegex = buildRegex();
                    this.cachedFeatures = Collections.unmodifiableSet(buildFeatures());
                    this.cachedRegex = computedRegex;
                }
            }
        }
        return cachedRegex;
    }

    @Override
    public final SiftCompiledPattern sieveWith(SiftEngine engine) {
        shake();
        return engine.compile(cachedRegex, getInternalUsedFeatures());
    }

    @Override
    public Set<String> getInternalRegisteredGroups() {
        // Default implementation: empty.
        // Overridden by complex builders (like SiftConnector) that manage state.
        return Collections.emptySet();
    }

    @Override
    public Set<String> getInternalRequiredBackreferences() {
        // Default implementation: empty.
        // Overridden by complex builders.
        return Collections.emptySet();
    }

    @Override
    public Set<RegexFeature> getInternalUsedFeatures() {
        shake();
        return buildFeatures();
    }

    /**
     * Implemented by subclasses to define the specific regex string generation logic.
     *
     * @return The raw regular expression string.
     */
    protected abstract String buildRegex();

    /**
     * Implemented by subclasses to report advanced features (e.g., lookarounds, backreferences)
     * used during the pattern assembly.
     * <p>
     * The default implementation returns an empty set. Complex pattern builders should
     * override this method to accurately reflect their structural requirements.
     *
     * @return A set of features used by this specific pattern.
     */
    protected Set<RegexFeature> buildFeatures() {
        return Collections.emptySet();
    }

    @Override
    public final SiftPattern<Ctx> preventBacktracking() {
        return new AtomicPattern<>(this);
    }

    @Override
    public final String toString() {
        return shake();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiftPattern)) return false;
        return Objects.equals(this.shake(), ((SiftPattern<?>) o).shake());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(shake());
    }
}