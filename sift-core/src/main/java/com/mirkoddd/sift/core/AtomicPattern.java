package com.mirkoddd.sift.core;

import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;

import java.util.Set;

/**
 * Decorator that wraps a pattern in an atomic group (?>...).
 */
class AtomicPattern<Ctx extends SiftContext> extends BaseSiftPattern<Ctx> implements PatternMetadata {
    private final SiftPattern<Ctx> inner;

    AtomicPattern(SiftPattern<Ctx> inner) {
        this.inner = inner;
    }

    @Override
    protected String buildRegex() {
        final String atomicOpen = "(?>";
        final String atomicClose = ")";
        return atomicOpen + inner.shake() + atomicClose;
    }

    @Override
    public Set<String> getInternalRegisteredGroups() {
        if (inner instanceof PatternMetadata) {
            return ((PatternMetadata) inner).getInternalRegisteredGroups();
        }
        return java.util.Collections.emptySet();
    }

    @Override
    public Set<String> getInternalRequiredBackreferences() {
        if (inner instanceof PatternMetadata) {
            return ((PatternMetadata) inner).getInternalRequiredBackreferences();
        }
        return java.util.Collections.emptySet();
    }
}