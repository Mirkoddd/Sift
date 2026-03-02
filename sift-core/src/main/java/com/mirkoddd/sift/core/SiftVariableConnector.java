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

import com.mirkoddd.sift.core.dsl.VariableCharacterClassConnectorStep;
import com.mirkoddd.sift.core.dsl.VariableConnectorStep;

/**
 * Specialized connector for variable-length steps.
 */
class SiftVariableConnector extends SiftConnector implements VariableConnectorStep, VariableCharacterClassConnectorStep {

    SiftVariableConnector(PatternAssembler assembler) {
        super(assembler);
    }

    @Override
    public VariableCharacterClassConnectorStep including(char extra, char... additionalExtras) {
        PatternAssembler next = assembler.copy();
        next.addClassInclusion(extra, additionalExtras);
        return new SiftVariableConnector(next);
    }

    @Override
    public VariableCharacterClassConnectorStep excluding(char excluded, char... additionalExcluded) {
        PatternAssembler next = assembler.copy();
        next.addClassExclusion(excluded, additionalExcluded);
        return new SiftVariableConnector(next);
    }

    @Override
    public VariableConnectorStep withoutBacktracking() {
        PatternAssembler next = assembler.copy();
        next.applyPossessiveModifier();
        return new SiftVariableConnector(next);
    }

    @Override
    public VariableConnectorStep asFewAsPossible() {
        PatternAssembler next = assembler.copy();
        next.applyLazyModifier();
        return new SiftVariableConnector(next);
    }
}