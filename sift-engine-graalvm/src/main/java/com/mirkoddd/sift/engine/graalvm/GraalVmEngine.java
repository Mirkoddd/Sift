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

import com.mirkoddd.sift.core.engine.RegexFeature;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;
import com.mirkoddd.sift.core.engine.SiftEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.util.Set;

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
public final class GraalVmEngine implements SiftEngine {

    /**
     * The stateless singleton instance of the GraalVM Engine.
     */
    public static final GraalVmEngine INSTANCE = new GraalVmEngine();

    /**
     * The shared native engine. It caches the TRegex compilations and ASTs,
     * allowing multiple contexts to execute the same regex without recompiling.
     */
    private static final Engine SHARED_ENGINE = Engine.newBuilder().build();

    /**
     * Thread-isolated Polyglot context. Since JavaScript execution (and RegExp state)
     * is strictly single-threaded in GraalVM, every thread gets its own secure sandbox
     * backed by the shared AST engine.
     */
    static final ThreadLocal<Context> THREAD_CONTEXT = ThreadLocal.withInitial(() ->
            Context.newBuilder(GraalVmDictionary.JS_LANGUAGE_ID)
                    .allowAllAccess(false)
                    .engine(SHARED_ENGINE)
                    .build()
    );

    /**
     * Private constructor to prevent instantiation. Use {@link #INSTANCE}.
     */
    private GraalVmEngine() {
        // Prevent instantiation
    }

    /**
     * Validates if the requested regex features are supported by GraalVM TRegex.
     * <p>
     * Note: GraalVM TRegex (via JS bridge) does not currently support recursive
     * matching (e.g., {@code (?R)}).
     *
     * @param features the set of regex features required by the compiled pattern
     * @throws UnsupportedOperationException if an unsupported feature is detected
     */
    @Override
    public void checkSupport(Set<RegexFeature> features) {
        if (features.contains(RegexFeature.RECURSION)) {
            throw new UnsupportedOperationException(
                    "GraalVM TRegex (via JS) does not support this specific feature: Recursive patterns"
            );
        }

        if (features.contains(RegexFeature.INLINE_FLAGS)) {
            throw new UnsupportedOperationException(
                    "GraalVM TRegex (via JS) does not support inline/local modifiers like (?i) or (?m)."
            );
        }

        if (features.contains(RegexFeature.PREVIOUS_MATCH_ANCHOR)) {
            throw new UnsupportedOperationException(
                    "GraalVM TRegex (via JS) does not support the contiguous match anchor \\G."
            );
        }
    }

    /**
     * Compiles the raw regular expression string into an executable pattern.
     * <p>
     * This method acts as a fail-fast mechanism. It evaluates the regex syntax
     * immediately on the calling thread to intercept any JavaScript SyntaxErrors
     * before returning the lazy-evaluating compiled pattern wrapper.
     *
     * @param rawRegex     the generated regular expression string
     * @param usedFeatures the features used in the regex (for support validation)
     * @return a {@link SiftCompiledPattern} instance tailored for GraalVM
     * @throws IllegalArgumentException if GraalVM rejects the regex syntax
     */
    @Override
    public SiftCompiledPattern compile(String rawRegex, Set<RegexFeature> usedFeatures) {
        checkSupport(usedFeatures);

        try {
            // Validate the syntax immediately to fail fast
            Context ctx = THREAD_CONTEXT.get();
            Value compiler = ctx.eval(GraalVmDictionary.JS_LANGUAGE_ID, GraalVmDictionary.JS_COMPILER_SNIPPET);
            compiler.execute(rawRegex);

            return new GraalVmCompiledPattern(rawRegex);

        } catch (PolyglotException e) {
            throw new IllegalArgumentException("GraalVM TRegex engine rejected the generated syntax: " + e.getMessage(), e);
        }
    }
}