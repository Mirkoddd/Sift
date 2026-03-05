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

import com.mirkoddd.sift.core.dsl.* ;

import java.util.Objects;
import java.util.regex.Pattern;

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
 */
class SiftConnector implements ConnectorStep, CharacterClassConnectorStep {

    protected final PatternAssembler assembler;
    private volatile String cachedRegex = null;
    private volatile Pattern cachedPattern = null;

    SiftConnector(PatternAssembler assembler) {
        this.assembler = assembler;
    }

    @Override
    public QuantifierStep then() {
        PatternAssembler next = assembler.copy();
        next.flush();
        return new SiftQuantifier(next);
    }

    @Override
    public ConnectorStep followedBy(char c) {
        return this.then().exactly(1).character(c);
    }

    @Override
    public ConnectorStep followedBy(SiftPattern pattern, SiftPattern... additionalPatterns) {
        Objects.requireNonNull(pattern, "First SiftPattern cannot be null");
        Objects.requireNonNull(additionalPatterns, "Additional SiftPatterns array cannot be null");

        for (int i = 0; i < additionalPatterns.length; i++) {
            Objects.requireNonNull(additionalPatterns[i], "SiftPattern at index " + i + " cannot be null");
        }

        ConnectorStep current = this.then().exactly(1).pattern(pattern);
        for (SiftPattern p : additionalPatterns) {
            current = current.then().exactly(1).pattern(p);
        }
        return current;
    }

    @Override
    public CharacterClassConnectorStep including(char extra, char... additionalExtras) {
        PatternAssembler next = assembler.copy();
        next.addClassInclusion(extra, additionalExtras);
        return new SiftConnector(next);
    }

    @Override
    public CharacterClassConnectorStep excluding(char excluded, char... additionalExcluded) {
        PatternAssembler next = assembler.copy();
        next.addClassExclusion(excluded, additionalExcluded);
        return new SiftConnector(next);
    }

    @Override
    public ConnectorStep wordBoundary() {
        PatternAssembler next = assembler.copy();
        next.addWordBoundary();
        return new SiftConnector(next);
    }

    @Override
    public SiftPattern andNothingElse() {
        PatternAssembler next = assembler.copy();
        next.addAnchor(RegexSyntax.END_OF_LINE);
        return new SiftConnector(next);
    }

    @Override
    public String shake() {
        if (cachedRegex == null) {
            synchronized (this) {
                if (cachedRegex == null) {
                    PatternAssembler tempAssembler = assembler.copy();
                    tempAssembler.validateFinalAssembly();
                    String generatedRegex = tempAssembler.build();
                    try {
                        cachedPattern = Pattern.compile(generatedRegex);
                        cachedRegex = generatedRegex;
                    } catch (java.util.regex.PatternSyntaxException e) {
                        throw new IllegalStateException("Sift generated an invalid regex pattern...", e);
                    }
                }
            }
        }
        return cachedRegex;
    }

    @Override
    public Pattern sieve() {
        shake();
        return cachedPattern;
    }

    @Override
    public void preventExternalImplementation(InternalToken token) {
        if (token == null) {
            throw new SecurityException("External implementation of SiftPattern is not allowed.");
        }
    }

    @Override
    public String toString() {
        return shake();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiftConnector that = (SiftConnector) o;
        return this.shake().equals(that.shake());
    }

    @Override
    public int hashCode() {
        return Objects.hash(shake());
    }
}