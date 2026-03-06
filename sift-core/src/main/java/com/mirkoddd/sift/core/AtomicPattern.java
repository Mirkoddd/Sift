package com.mirkoddd.sift.core;

import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;

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
        final String atomicOpen = "(?>";
        final String atomicClose = ")";
        return atomicOpen + inner.shake() + atomicClose;
    }
}