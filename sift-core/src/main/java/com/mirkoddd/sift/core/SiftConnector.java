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

import com.mirkoddd.sift.core.dsl.*;

import java.util.Objects;

/**
 * Node responsible for connecting steps and terminating the DSL chain.
 * <p>
 * <b>Architectural Note (Interface Segregation & Memory Optimization):</b><br>
 * This single package-private class implements multiple state interfaces (e.g., {@link ConnectorStep}
 * and {@link CharacterClassConnectorStep}). While these represent logically distinct states in the DSL,
 * they are unified into a single concrete implementation to prevent "class explosion" and reduce
 * memory footprint (especially useful for Android environments).
 * <p>
 * Strict type-safety is guaranteed at compile-time because this class is not public.
 * External consumers interact exclusively with the narrowly-scoped public interfaces returned by the engine.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
class SiftConnector<Ctx extends SiftContext> extends BaseSiftPattern<Ctx> implements ConnectorStep<Ctx>, CharacterClassConnectorStep<Ctx> {

    protected final PatternAssembler assembler;

    /**
     * Instantiates the connector with the current state of the pattern assembler.
     *
     * @param assembler The internal state machine builder containing the accumulated pattern.
     */
    SiftConnector(PatternAssembler assembler) {
        this.assembler = assembler;
    }

    /** {@inheritDoc} */
    @Override
    public QuantifierStep<Ctx> then() {
        PatternAssembler next = assembler.copy();
        next.flush();
        return new SiftQuantifier<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> followedBy(char c) {
        return this.then().exactly(1).character(c);
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> followedBy(SiftPattern<SiftContext.Fragment> p1) {
        Objects.requireNonNull(p1, "Pattern cannot be null");
        return this.then().exactly(1).pattern(p1);
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> followedBy(SiftPattern<SiftContext.Fragment> p1, SiftPattern<SiftContext.Fragment> p2) {
        return followedBy(p1).followedBy(p2);
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> followedBy(Iterable<? extends SiftPattern<SiftContext.Fragment>> patterns) {
        Objects.requireNonNull(patterns, "Patterns iterable cannot be null");

        ConnectorStep<Ctx> current = this;
        for (SiftPattern<SiftContext.Fragment> p : patterns) {
            Objects.requireNonNull(p, "SiftPattern in iterable cannot be null");
            current = current.followedBy(p);
        }
        return current;
    }

    /** {@inheritDoc} */
    @Override
    public CharacterClassConnectorStep<Ctx> including(char extra, char... additionalExtras) {
        PatternAssembler next = assembler.copy();
        next.addClassInclusion(extra, additionalExtras);
        return new SiftConnector<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public CharacterClassConnectorStep<Ctx> excluding(char excluded, char... additionalExcluded) {
        PatternAssembler next = assembler.copy();
        next.addClassExclusion(excluded, additionalExcluded);
        return new SiftConnector<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public ConnectorStep<Ctx> wordBoundary() {
        PatternAssembler next = assembler.copy();
        next.addWordBoundary();
        return new SiftConnector<>(next);
    }

    /** {@inheritDoc} */
    @Override
    public SiftPattern<SiftContext.Root> andNothingElse() {
        PatternAssembler next = assembler.copy();
        next.addAnchor(RegexSyntax.END_OF_LINE);
        return new SiftConnector<>(next);
    }

    /** {@inheritDoc} */
    @Override
    protected String buildRegex() {
        PatternAssembler temp = assembler.copy();
        temp.validateFinalAssembly();
        return temp.build();
    }
}