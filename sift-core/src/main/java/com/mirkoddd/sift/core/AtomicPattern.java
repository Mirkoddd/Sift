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

/**
 * Decorator that wraps a pattern in an atomic group (?>...).
 * <p>
 * In the Lazy AST architecture, this acts as a new root node that instructs the
 * visitor to apply the atomic group modifier to the inner evaluated pattern.
 */
class AtomicPattern<Ctx extends SiftContext> extends BaseSiftPattern<Ctx> {

    private final SiftPattern<Ctx> inner;

    AtomicPattern(SiftPattern<Ctx> inner) {
        super(null); // This is a decorator, so it starts a new AST traversal chain
        this.inner = inner;
    }

    @Override
    public void accept(PatternVisitor visitor) {
        visitor.visitAtomicGroup(inner);
    }
}