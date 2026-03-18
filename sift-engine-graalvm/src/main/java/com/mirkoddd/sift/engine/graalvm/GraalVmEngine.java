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
package com.mirkoddd.sift.engine.graalvm;

import com.mirkoddd.sift.core.engine.AbstractSiftEngine;
import com.mirkoddd.sift.core.engine.RegexFeature;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * GraalVM TRegex execution engine for Sift.
 * <p>
 * This engine provides extreme performance through native AOT-ready compilation
 * and Polyglot evaluation using GraalVM's internal TRegex engine. It bridges
 * the gap using a lightweight JavaScript environment.
 * <p>
 * <b>Thread Safety:</b> This engine is fully thread-safe. It utilizes a shared
 * {@link Engine} to cache native DFA/AST bytecodes across all threads, combined
 * with a {@link ThreadLocal} Polyglot {@link Context} to ensure isolated and
 * lock-free execution of stateful JavaScript RegEx objects.
 *
 * @since 6.2.0
 */
public final class GraalVmEngine extends AbstractSiftEngine {

    /**
     * The stateless singleton instance of the GraalVM Engine.
     */
    public static final GraalVmEngine INSTANCE = new GraalVmEngine();

    /**
     * The shared native engine. It caches the TRegex compilations and ASTs,
     * allowing multiple contexts to execute the same regex without recompiling.
     */
    private static final Engine SHARED_ENGINE = Engine.newBuilder().build();

    private static final Map<RegexFeature, String> UNSUPPORTED_FEATURES = new EnumMap<>(RegexFeature.class);

    static {
        UNSUPPORTED_FEATURES.put(RegexFeature.RECURSION,
                "GraalVM TRegex (via JS) does not support this specific feature: Recursive patterns.");
        UNSUPPORTED_FEATURES.put(RegexFeature.INLINE_FLAGS,
                "GraalVM TRegex (via JS) does not support inline/local modifiers like (?i) or (?m).");
        UNSUPPORTED_FEATURES.put(RegexFeature.PREVIOUS_MATCH_ANCHOR,
                "GraalVM TRegex (via JS) does not support the contiguous match anchor \\G.");
    }

    /**
     * Thread-isolated Polyglot context.
     * Uses Lazy Initialization to avoid spinning up the GraalVM JS engine
     * on threads that open a scope but never compile/execute a regex.
     */
    static final ThreadLocal<Context> THREAD_CONTEXT = new ThreadLocal<>();

    private static final Logger LOGGER = Logger.getLogger(GraalVmEngine.class.getName());

    private final ContextCloser contextCloser;

    /**
     * Private constructor to prevent instantiation. Use {@link #INSTANCE}.
     * Initializes the default production behavior for closing contexts.
     */
    private GraalVmEngine() {
        this.contextCloser = Context::close;
    }

    /**
     * Package-private constructor for testing purposes only.
     * Allows injecting a custom ContextCloser to test branch coverage (e.g., exceptions during close)
     * without polluting global state with mutable static fields.
     */
    GraalVmEngine(ContextCloser closer) {
        this.contextCloser = closer;
    }

    /**
     * Safely executes a block of code within a managed GraalVM thread scope.
     * <p>
     * This is the recommended API for evaluating patterns with this engine, as it
     * automatically manages the thread-local {@link Context} lifecycle and prevents
     * memory leaks in environments with long-lived thread pools.
     * <p>
     * If the action throws a {@link RuntimeException}, it is propagated as-is.
     * Any exception thrown during scope cleanup is suppressed and does not mask the original.
     * <p>
     * Usage example:
     * <pre>{@code
     * boolean isValid = GraalVmEngine.INSTANCE.execute(() -> {
     *     try (SiftCompiledPattern pattern = myRegex.sieveWith(GraalVmEngine.INSTANCE)) {
     *         return pattern.matchesEntire(input);
     *     }
     * });
     * }</pre>
     *
     * @param action The operation to perform within the managed scope.
     * @param <T>    The return type of the operation.
     * @return The result of the operation.
     */
    @SuppressWarnings("ConstantConditions")
    public <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            // JUSTIFICATION for @SuppressWarnings("ConstantConditions"):
            // The Java compiler assumes Supplier.get() only throws RuntimeExceptions.
            // However, cross-language interoperability or Java's "sneaky throws"
            // can bypass compiler checks and throw checked exceptions at runtime.
            // This explicit type check prevents a catastrophic ClassCastException.
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failed to execute action within GraalVM scope", e);
        } finally {
            openThreadScope().close();
        }
    }

    /**
     * Represents a managed execution scope for the GraalVM engine.
     * Extends AutoCloseable but safely overrides close() to not throw checked exceptions.
     */
    public interface EngineScope extends AutoCloseable {
        @Override
        void close();
    }

    /**
     * Opens a new execution scope for the GraalVM engine on the current thread.
     * <p>
     * <b>Important Memory Leak Prevention:</b> When using this engine in environments
     * with long-lived thread pools (such as application servers or Android background workers),
     * you <b>must</b> wrap your Sift operations within this scope using a {@code try-with-resources}
     * block. This ensures that the thread-local Polyglot {@link Context} and its associated
     * native resources are safely destroyed at the end of the execution.
     * <p>
     * For most use cases, prefer {@link #execute(java.util.function.Supplier)} which manages
     * the scope automatically.
     * <p>
     * Usage example:
     * <pre>{@code
     * try (GraalVmEngine.EngineScope scope = GraalVmEngine.INSTANCE.openThreadScope()) {
     *     SiftCompiledPattern pattern = myRegex.sieveWith(GraalVmEngine.INSTANCE);
     *     boolean isValid = pattern.matchesEntire(input);
     *     // ... your business logic ...
     * } // The GraalVM context is safely closed and removed from the thread here
     * }</pre>
     *
     * @return an {@link EngineScope} representing the current thread's GraalVM execution scope
     */
    public EngineScope openThreadScope() {
        return () -> {
            Context ctx = THREAD_CONTEXT.get();
            if (ctx != null) {
                try {
                    contextCloser.close(ctx);
                } catch (Exception ignored) {

                } finally {
                    THREAD_CONTEXT.remove();
                }
            }
        };
    }

    @Override
    protected Map<RegexFeature, String> getUnsupportedFeatures() {
        return UNSUPPORTED_FEATURES;
    }

    @Override
    protected SiftCompiledPattern doCompile(String rawRegex, Set<RegexFeature> usedFeatures) {
        if (THREAD_CONTEXT.get() == null) {
            LOGGER.warning(
                    "GraalVmEngine: compiling without an explicit thread scope. " +
                            "In thread-pool environments this may cause memory leaks. " +
                            "Wrap your call in GraalVmEngine.INSTANCE.execute(...) or manually use openThreadScope()."
            );
        }

        try {
            // Validate the syntax immediately to fail fast
            Context ctx = getContext();
            Value compiler = ctx.eval(GraalVmDictionary.JS_LANGUAGE_ID, GraalVmDictionary.JS_COMPILER_SNIPPET);
            compiler.execute(rawRegex);

            return new GraalVmCompiledPattern(rawRegex);

        } catch (PolyglotException e) {
            throw new IllegalArgumentException("GraalVM TRegex engine rejected the generated syntax: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves or initializes the Polyglot context for the current thread.
     */
    static Context getContext() {
        Context ctx = THREAD_CONTEXT.get();
        if (ctx == null) {
            ctx = Context.newBuilder(GraalVmDictionary.JS_LANGUAGE_ID)
                    .allowAllAccess(false)
                    .engine(SHARED_ENGINE)
                    .build();
            THREAD_CONTEXT.set(ctx);
        }
        return ctx;
    }

    @FunctionalInterface
    interface ContextCloser {
        void close(Context ctx);
    }
}