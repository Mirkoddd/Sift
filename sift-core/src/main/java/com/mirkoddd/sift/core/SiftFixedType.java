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

import com.mirkoddd.sift.core.dsl.CharacterConnector;
import com.mirkoddd.sift.core.dsl.Connector;
import com.mirkoddd.sift.core.dsl.SiftContext;

/**
 * Concrete implementation of the type evaluation step for <b>fixed-length</b> quantifiers.
 * <p>
 * <b>Architecture &amp; Type Safety:</b><br>
 * This class is instantiated when the DSL processes a token with an exact repetition
 * (e.g., {@code .exactly(3)} or implicitly exactly one). By extending {@link BaseType}
 * with standard connector bounds, its factory methods strictly return a generic {@link Connector}
 * and {@link CharacterConnector}.
 * <p>
 * This architectural choice physically prevents the compiler from exposing variable-length
 * modifiers (like {@code asFewAsPossible()} or {@code withoutBacktracking()}) on fixed-length tokens,
 * where they would be mathematically meaningless and potentially confusing.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
class SiftFixedType<Ctx extends SiftContext> extends BaseType<Ctx, Connector<Ctx>, CharacterConnector<Ctx>> {

    /**
     * Instantiates the fixed-type step with the current state of the pattern assembler.
     *
     * @param assembler The internal state machine builder.
     */
    SiftFixedType(PatternAssembler assembler) {
        super(assembler);
    }

    /** {@inheritDoc} */
    @Override
    protected Connector<Ctx> getNormalConnector(PatternAssembler nextAssembler) {
        return new SiftConnector<>(nextAssembler);
    }

    /** {@inheritDoc} */
    @Override
    protected CharacterConnector<Ctx> getCharacterClassConnector(PatternAssembler nextAssembler) {
        return new SiftConnector<>(nextAssembler);
    }
}