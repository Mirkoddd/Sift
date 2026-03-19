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
import com.mirkoddd.sift.core.dsl.VariableCharacterConnector;
import com.mirkoddd.sift.core.dsl.VariableConnector;

/**
 * Specialized connector for variable-length steps.
 * <p>
 * <b>Architectural Note (Interface Segregation & Memory Optimization):</b><br>
 * Similar to {@link SiftConnector}, this single package-private class implements multiple
 * variable-length state interfaces (e.g., {@link VariableConnector} and
 * {@link VariableCharacterConnector}). These logically distinct states are unified
 * into a single concrete implementation to prevent "class explosion" and minimize memory
 * overhead, which is particularly beneficial in Android environments.
 * <p>
 * Compile-time type-safety is strictly enforced because this class is not public, and
 * external consumers only interact with the narrowly-scoped public interfaces.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
class SiftVariableConnector<Ctx extends SiftContext> extends SiftConnector<Ctx> implements VariableConnector<Ctx>, VariableCharacterConnector<Ctx> {

    /**
     * Instantiates the variable-length connector, linking it to the AST chain.
     *
     * @param parentNode The preceding node in the DSL chain.
     */
    SiftVariableConnector(BaseSiftPattern<?> parentNode) {
        super(parentNode, null); // Pass null for operation, as the modifier methods will create child nodes
    }

    // Constructor used internally by modifier methods to append themselves to the chain
    private SiftVariableConnector(BaseSiftPattern<?> parentNode, java.util.function.Consumer<PatternVisitor> operation) {
        super(parentNode, operation);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note on Covariant Return Types:</b> This overrides the parent implementation to return a
     * narrower type ({@link VariableCharacterConnector}). This ensures that variable-length
     * modifiers (like possessive or lazy behaviors) are not lost from the fluent chain after
     * modifying the character class.
     */
    @Override
    public VariableCharacterConnector<Ctx> including(char extra, char... additionalExtras) {
        return new SiftVariableConnector<>(this, visitor -> visitor.visitClassInclusion(extra, additionalExtras));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note on Covariant Return Types:</b> Overridden to return a narrower, variable-length specific type.
     */
    @Override
    public VariableCharacterConnector<Ctx> excluding(char excluded, char... additionalExcluded) {
        return new SiftVariableConnector<>(this, visitor -> visitor.visitClassExclusion(excluded, additionalExcluded));
    }

    /** {@inheritDoc} */
    @Override
    public VariableConnector<Ctx> withoutBacktracking() {
        return new SiftVariableConnector<>(this, PatternVisitor::visitPossessiveModifier);
    }

    /** {@inheritDoc} */
    @Override
    public VariableConnector<Ctx> asFewAsPossible() {
        return new SiftVariableConnector<>(this, PatternVisitor::visitLazyModifier);
    }
}