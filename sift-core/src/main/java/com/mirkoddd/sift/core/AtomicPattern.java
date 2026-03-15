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
import com.mirkoddd.sift.core.engine.RegexFeature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Decorator that wraps a pattern in an atomic group (?>...).
 */
class AtomicPattern<Ctx extends SiftContext> extends BaseSiftPattern<Ctx> {

    private final SiftPattern<Ctx> inner;

    AtomicPattern(SiftPattern<Ctx> inner) {
        this.inner = inner;
    }

    @Override
    protected String buildRegex() {
        return "(?>" + inner.shake() + ")";
    }

    @Override
    protected Set<RegexFeature> buildFeatures() {
        Set<RegexFeature> features = EnumSet.of(RegexFeature.ATOMIC_GROUP);

        if (inner instanceof PatternMetadata) {
            features.addAll(((PatternMetadata) inner).getInternalUsedFeatures());
        }

        return Collections.unmodifiableSet(features);
    }

    @Override
    public Set<String> getInternalRegisteredGroups() {
        if (inner instanceof PatternMetadata) {
            return ((PatternMetadata) inner).getInternalRegisteredGroups();
        }
        return Collections.emptySet();
    }

    @Override
    public Set<String> getInternalRequiredBackreferences() {
        if (inner instanceof PatternMetadata) {
            return ((PatternMetadata) inner).getInternalRequiredBackreferences();
        }
        return Collections.emptySet();
    }
}