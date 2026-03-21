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

import java.util.Objects;
import java.util.Set;

/**
 * Internal foundation for all Sift patterns.
 * <p>
 * This class serves as the abstract base for all nodes in the Sift Abstract Syntax Tree (AST).
 * It represents an immutable node linked to its parent, establishing a lazy evaluation chain.
 * <p>
 * It also centralizes the <b>Double-Checked Locking</b> logic for thread-safe caching
 * of the generated regex string and its associated structural features, ensuring that
 * tree traversal and compilation happen at most once per pattern instance.
 *
 * @param <Ctx> The specific context type for state machine validation.
 */
abstract class BaseSiftPattern<Ctx extends SiftContext> implements SiftPattern<Ctx>, RegexNode, PatternMetadata {

    // The core of the Lazy AST: an immutable pointer to the previous step in the DSL.
    private final BaseSiftPattern<?> parentNode;

    private volatile String cachedRegex = null;
    private volatile Set<RegexFeature> cachedFeatures = null;
    private volatile Set<String> cachedRegisteredGroups = null;
    private volatile Set<String> cachedRequiredBackreferences = null;

    /**
     * Creates a new node in the AST, linking it to its parent.
     *
     * @param parentNode The preceding node in the DSL chain, or null if this is the root node.
     */
    protected BaseSiftPattern(BaseSiftPattern<?> parentNode) {
        this.parentNode = parentNode;
    }

    @Override
    public final Object ___internal_lock___() {
        return InternalToken.INSTANCE;
    }

    /**
     * Traverses the AST recursively from the root down to this leaf node,
     * allowing the visitor to process instructions in the correct structural order.
     *
     * @param visitor The visitor traversing the tree (e.g., PatternAssembler).
     */
    protected final void traverse(PatternVisitor visitor) {
        if (parentNode != null) {
            parentNode.traverse(visitor);
        }
        this.accept(visitor);
    }

    private void evaluateAst() {
        if (cachedRegex == null) {
            synchronized (this) {
                if (cachedRegex == null) {
                    PatternAssembler compiler = new PatternAssembler();
                    this.traverse(compiler);

                    this.cachedRegex = compiler.build();
                    this.cachedFeatures = compiler.getUsedFeatures();
                    this.cachedRegisteredGroups = compiler.getRegisteredGroups();
                    this.cachedRequiredBackreferences = compiler.getRequiredBackreferences();
                }
            }
        }
    }

    @Override
    public final String shake() {
        evaluateAst();

        for (String req : cachedRequiredBackreferences) {
            if (!cachedRegisteredGroups.contains(req)) {
                throw new IllegalStateException("The group '" + req +
                        "' must be captured with .namedCapture() before it can be referenced.");
            }
        }

        return cachedRegex;
    }

    @Override
    public final String getInternalRawRegex() {
        evaluateAst();
        return cachedRegex;
    }

    @Override
    public final SiftCompiledPattern sieveWith(SiftEngine engine) {
        shake();
        return engine.compile(cachedRegex, cachedFeatures);
    }

    @Override
    public final Set<String> getInternalRegisteredGroups() {
        evaluateAst();
        return cachedRegisteredGroups;
    }

    @Override
    public final Set<String> getInternalRequiredBackreferences() {
        evaluateAst();
        return cachedRequiredBackreferences;
    }

    @Override
    public final Set<RegexFeature> getInternalUsedFeatures() {
        evaluateAst();
        return cachedFeatures;
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