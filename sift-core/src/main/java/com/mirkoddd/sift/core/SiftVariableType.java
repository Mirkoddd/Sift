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
 * Concrete implementation of the type evaluation step for <b>variable-length</b> quantifiers.
 * <p>
 * <b>Architecture &amp; Type Safety:</b><br>
 * This class is instantiated when the DSL processes a token with a variable repetition
 * (e.g., {@code .oneOrMore()} or {@code .between(min, max)}). By extending {@link BaseType}
 * with variable-bound generics, its factory methods strictly return {@link VariableConnector}
 * and {@link VariableCharacterConnector}.
 * <p>
 * This unlocks advanced modifier operations for the user, ensuring that methods like
 * {@code asFewAsPossible()} (reluctant) or {@code withoutBacktracking()} (possessive)
 * are only exposed when mathematically and logically valid.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
class SiftVariableType<Ctx extends SiftContext> extends BaseType<Ctx, VariableConnector<Ctx>, VariableCharacterConnector<Ctx>> {

    /**
     * Instantiates the variable-type step with the current state of the pattern assembler.
     *
     * @param assembler The internal state machine builder.
     */
    SiftVariableType(PatternAssembler assembler) {
        super(assembler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a specialized connector that exposes variable-length modifiers.
     */
    @Override
    protected VariableConnector<Ctx> getNormalConnector(PatternAssembler nextAssembler) {
        return new SiftVariableConnector<>(nextAssembler);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a specialized character class connector that retains variable-length modifiers.
     */
    @Override
    protected VariableCharacterConnector<Ctx> getCharacterClassConnector(PatternAssembler nextAssembler) {
        return new SiftVariableConnector<>(nextAssembler);
    }
}