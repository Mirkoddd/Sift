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

import com.mirkoddd.sift.core.dsl.Assertion;
import com.mirkoddd.sift.core.dsl.ConditionalElse;
import com.mirkoddd.sift.core.dsl.ConditionalThen;
import com.mirkoddd.sift.core.dsl.Fragment;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import com.mirkoddd.sift.core.engine.RegexFeature;

import java.util.Objects;
import java.util.function.Function;

import static com.mirkoddd.sift.core.SiftPatterns.negativeLookahead;
import static com.mirkoddd.sift.core.SiftPatterns.positiveLookahead;

/**
 * Internal State Machine that safely guides the user through the If-Then-Else lifecycle.
 * <p>
 * This class is intentionally package-private to hide implementation details from the
 * public API, enforcing interaction strictly through the DSL interfaces.
 */
final class ConditionalAssembler implements ConditionalThen, ConditionalElse {

    private final SiftPattern<Assertion> trueCondition;
    private final SiftPattern<Assertion> falseCondition;
    private SiftPattern<Fragment> thenPattern;
    private final Function<SiftPattern<Fragment>, SiftPattern<Fragment>> resolutionWrapper;

    private ConditionalAssembler(
            SiftPattern<Assertion> trueCondition,
            SiftPattern<Assertion> falseCondition,
            Function<SiftPattern<Fragment>, SiftPattern<Fragment>> resolutionWrapper) {
        this.trueCondition = trueCondition;
        this.falseCondition = falseCondition;
        this.resolutionWrapper = resolutionWrapper;
    }

    ConditionalAssembler(SiftPattern<Assertion> trueCondition, SiftPattern<Assertion> falseCondition) {
        this(trueCondition, falseCondition, Function.identity());
    }

    @Override
    public ConditionalElse thenUse(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Then branch pattern cannot be null");
        this.thenPattern = pattern;
        return this;
    }

    @Override
    public SiftPattern<Fragment> otherwiseUse(SiftPattern<Fragment> pattern) {
        Objects.requireNonNull(pattern, "Otherwise branch pattern cannot be null");
        return resolutionWrapper.apply(assembleConditionalPattern(pattern));
    }

    @Override
    public SiftPattern<Fragment> otherwiseNothing() {
        return resolutionWrapper.apply(assembleConditionalPattern(null));
    }

    @Override
    public ConditionalThen otherwiseIfFollowedBy(SiftPattern<Fragment> condition) {
        Objects.requireNonNull(condition, "Else-If condition cannot be null");

        return new ConditionalAssembler(
                positiveLookahead(condition),
                negativeLookahead(condition),
                nestedResolvedPattern -> this.resolutionWrapper.apply(this.assembleConditionalPattern(nestedResolvedPattern))
        );
    }

    private SiftPattern<Fragment> assembleConditionalPattern(SiftPattern<Fragment> falseBranch) {

        SiftPattern<?>[] childNodes = falseBranch != null
                ? new SiftPattern<?>[]{trueCondition, thenPattern, falseCondition, falseBranch}
                : new SiftPattern<?>[]{trueCondition, thenPattern, falseCondition};

        return SiftPatterns.memoize(
                () -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(RegexSyntax.NON_CAPTURING_GROUP_OPEN);

                    sb.append(trueCondition.shake());
                    sb.append(thenPattern.shake());

                    sb.append(RegexSyntax.OR);

                    sb.append(falseCondition.shake());
                    if (falseBranch != null) {
                        sb.append(falseBranch.shake());
                    }

                    sb.append(RegexSyntax.GROUP_CLOSE);
                    return sb.toString();
                },
                RegexFeature.CONDITIONAL,
                childNodes
        );
    }
}