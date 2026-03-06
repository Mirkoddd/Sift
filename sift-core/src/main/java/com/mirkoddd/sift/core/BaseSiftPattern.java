package com.mirkoddd.sift.core;

import com.mirkoddd.sift.core.dsl.SiftContext;
import com.mirkoddd.sift.core.dsl.SiftPattern;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Internal foundation for all Sift patterns.
 * <p>
 * This class centralizes the <b>Double-Checked Locking</b> logic for thread-safe caching
 * of both the regex string and the compiled Pattern. It also implements the
 * com.mirkoddd.sift.core.dsl.SiftInternalSealer interface to prevent unauthorized
 * external implementations.
 */
abstract class BaseSiftPattern<Ctx extends SiftContext> implements SiftPattern<Ctx> {

    private volatile String cachedRegex = null;
    private volatile Pattern cachedPattern = null;

    @Override
    public final Object ___internal_lock___() {
        return InternalToken.INSTANCE;
    }

    @Override
    public final String shake() {
        if (cachedRegex == null) {
            synchronized (this) {
                if (cachedRegex == null) {
                    String raw = buildRegex();
                    try {
                        cachedPattern = Pattern.compile(raw);
                        cachedRegex = raw;
                    } catch (java.util.regex.PatternSyntaxException e) {
                        throw new IllegalStateException("Sift generated an invalid regex pattern: " + raw, e);
                    }
                }
            }
        }
        return cachedRegex;
    }

    @Override
    public final Pattern sieve() {
        shake();
        return cachedPattern;
    }

    /**
     * Implemented by subclasses to define the specific regex logic.
     */
    protected abstract String buildRegex();

    @Override
    public final SiftPattern<Ctx> preventBacktracking() {
        return new AtomicPattern<>(this);
    }

    @Override
    public final String toString() {
        return shake();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiftPattern)) return false;
        return Objects.equals(this.shake(), ((SiftPattern<?>) o).shake());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(shake());
    }
}