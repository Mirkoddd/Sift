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
import com.mirkoddd.sift.core.dsl.VariableCharacterClassConnectorStep;
import com.mirkoddd.sift.core.dsl.VariableConnectorStep;

/**
 * Specialized connector for variable-length steps.
 * <p>
 * <b>Architectural Note (Interface Segregation & Memory Optimization):</b><br>
 * Similar to {@link SiftConnector}, this single package-private class implements multiple
 * variable-length state interfaces (e.g., {@link VariableConnectorStep} and
 * {@link VariableCharacterClassConnectorStep}). These logically distinct states are unified
 * into a single concrete implementation to prevent "class explosion" and minimize memory
 * overhead, which is particularly beneficial in Android environments.
 * <p>
 * Compile-time type-safety is strictly enforced because this class is not public, and
 * external consumers only interact with the narrowly-scoped public interfaces.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
class SiftVariableConnector<Ctx extends SiftContext> extends SiftConnector<Ctx> implements VariableConnectorStep<Ctx>, VariableCharacterClassConnectorStep<Ctx> {

    /**
     * Instantiates the variable-length connector with the current state of the pattern assembler.
     *
     * @param assembler The internal state machine builder.
     */
    SiftVariableConnector(PatternAssembler assembler) {
        super(assembler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note on Covariant Return Types:</b> This overrides the parent implementation to return a
     * narrower type ({@link VariableCharacterClassConnectorStep}). This ensures that variable-length
     * modifiers (like possessive or lazy behaviors) are not lost from the fluent chain after
     * modifying the character class.
     */
    @Override
    public VariableCharacterClassConnectorStep<Ctx> including(char extra, char... additionalExtras) {
        PatternAssembler next = assembler.copy();
        next.addClassInclusion(extra, additionalExtras);
        return new SiftVariableConnector<>(next);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note on Covariant Return Types:</b> Overridden to return a narrower, variable-length specific type.
     */
    @Override
    public VariableCharacterClassConnectorStep<Ctx> excluding(char excluded, char... additionalExcluded) {
        PatternAssembler next = assembler.copy();
        next.addClassExclusion(excluded, additionalExcluded);
        return new SiftVariableConnector<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public VariableConnectorStep<Ctx> withoutBacktracking() {
        PatternAssembler next = assembler.copy();
        next.applyPossessiveModifier();
        return new SiftVariableConnector<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public VariableConnectorStep<Ctx> asFewAsPossible() {
        PatternAssembler next = assembler.copy();
        next.applyLazyModifier();
        return new SiftVariableConnector<>(next);
    }
}