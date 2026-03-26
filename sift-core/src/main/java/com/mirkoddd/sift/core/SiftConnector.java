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
import com.mirkoddd.sift.core.engine.RegexFeature;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Node responsible for connecting steps and terminating the DSL chain.
 * <p>
 * <b>Architectural Note (Immutable AST & Zero Overhead):</b><br>
 * This class represents a single node in the lazy abstract syntax tree. It holds no
 * string building state. When a DSL method is invoked, it simply creates a new, lightweight
 * node linked to itself, recording the semantic operation to perform.
 * This guarantees absolute thread-safety and near-zero memory allocation overhead.
 *
 * @param <Ctx> The structural context (Fragment or Root) preserving the integrity of the chain.
 */
class SiftConnector<Ctx extends SiftContext> extends BaseSiftPattern<Ctx> implements Connector<Ctx>, CharacterConnector<Ctx> {

    // The semantic operation this specific node represents
    private final Consumer<PatternVisitor> operation;

    /**
     * Instantiates a new node in the DSL chain.
     *
     * @param parentNode The preceding node. Can be null if this is the root.
     * @param operation  The specific instruction to apply to the visitor when traversing this node.
     */
    SiftConnector(BaseSiftPattern<?> parentNode, Consumer<PatternVisitor> operation) {
        super(parentNode);
        this.operation = operation;
    }

    @Override
    public void accept(PatternVisitor visitor) {
        if (operation != null) {
            operation.accept(visitor);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Quantifier<Ctx> then() {
        // Quantifier creation might need adjustment depending on how SiftQuantifier is structured.
        // Assuming SiftQuantifier also extends BaseSiftPattern and takes a parent.
        return new SiftQuantifier<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> followedBy(char c) {
        return this.then().exactly(1).character(c);
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> followedBy(SiftPattern<Fragment> p1) {
        Objects.requireNonNull(p1, "Pattern cannot be null");
        return this.then().exactly(1).of(p1);
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> followedByAssertion(SiftPattern<Assertion> assertion) {
        Objects.requireNonNull(assertion, "Assertion cannot be null");
        return new SiftConnector<>(this, visitor -> {
            visitor.visitFeature(RegexFeature.LOOKAHEAD);
            visitor.visitPattern(assertion);
        });
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> followedBy(SiftPattern<Fragment> p1, SiftPattern<Fragment> p2) {
        return followedBy(p1).followedBy(p2);
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> followedBy(Iterable<? extends SiftPattern<Fragment>> patterns) {
        Objects.requireNonNull(patterns, "Patterns iterable cannot be null");
        Connector<Ctx> current = this;
        for (SiftPattern<Fragment> p : patterns) {
            Objects.requireNonNull(p, "SiftPattern in iterable cannot be null");
            current = current.followedBy(p);
        }
        return current;
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> precededBy(SiftPattern<Fragment> p1) {
        Objects.requireNonNull(p1, "Pattern cannot be null");
        return new SiftConnector<>(this, visitor -> visitor.visitPrependPattern(p1));
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> precededByAssertion(SiftPattern<Assertion> assertion) {
        Objects.requireNonNull(assertion, "Assertion cannot be null");
        return new SiftConnector<>(this, visitor -> {
            visitor.visitFeature(RegexFeature.LOOKBEHIND);
            visitor.visitPrependPattern(assertion);
        });
    }

    /** {@inheritDoc} */
    @Override
    public CharacterConnector<Ctx> including(char extra, char... additionalExtras) {
        return new SiftConnector<>(this, visitor -> visitor.visitClassInclusion(extra, additionalExtras));
    }

    /** {@inheritDoc} */
    @Override
    public CharacterConnector<Ctx> excluding(char excluded, char... additionalExcluded) {
        return new SiftConnector<>(this, visitor -> visitor.visitClassExclusion(excluded, additionalExcluded));
    }

    /** {@inheritDoc} */
    @Override
    public CharacterConnector<Ctx> intersecting(CharacterSubset subset) {
        Objects.requireNonNull(subset, "CharacterSubset cannot be null");
        return new SiftConnector<>(this, visitor -> visitor.visitClassIntersection(subset.getPattern()));
    }

    /** {@inheritDoc} */
    @Override
    public Connector<Ctx> wordBoundary() {
        return new SiftConnector<>(this, PatternVisitor::visitWordBoundary);
    }

    /** {@inheritDoc} */
    @Override
    public SiftPattern<Root> andNothingElse() {
        return new SiftConnector<>(this, visitor -> visitor.visitAnchor(RegexSyntax.END_OF_LINE));
    }

    /** {@inheritDoc} */
    @Override
    public SiftPattern<Root> absoluteEnd() {
        return new SiftConnector<>(this, visitor -> visitor.visitAnchor(RegexSyntax.END_OF_STRING_ABSOLUTE));
    }

    /** {@inheritDoc} */
    @Override
    public SiftPattern<Root> endBeforeOptionalNewline() {
        return new SiftConnector<>(this, visitor -> {
            visitor.visitFeature(RegexFeature.END_BEFORE_NEWLINE_ANCHOR);
            visitor.visitAnchor(RegexSyntax.END_OF_STRING_BEFORE_NEWLINE);
        });
    }
}