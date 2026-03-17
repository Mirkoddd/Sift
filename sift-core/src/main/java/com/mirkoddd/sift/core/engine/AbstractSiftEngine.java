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
package com.mirkoddd.sift.core.engine;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Base abstract implementation of {@link SiftEngine} that enforces
 * centralized feature validation using the Template Method pattern.
 * <p>
 * This class serves as the primary extension point for integrating custom regex
 * engines into Sift. It guarantees that any engine extending this class will
 * automatically validate regex features before attempting compilation, preventing
 * unexpected runtime failures or contract violations.
 *
 * @since 6.0.0
 */
public abstract class AbstractSiftEngine implements SiftEngine {

    /**
     * The Template Method. Marked as FINAL to absolutely prevent subclasses
     * from bypassing the centralized validation logic.
     *
     * @param rawRegex     the raw regular expression string
     * @param usedFeatures the set of features required by the pattern
     * @return the compiled pattern
     * @throws UnsupportedOperationException if an unsupported feature is detected
     */
    @Override
    public final SiftCompiledPattern compile(String rawRegex, Set<RegexFeature> usedFeatures) {
        checkSupport(usedFeatures);
        return doCompile(rawRegex, usedFeatures);
    }

    /**
     * Centralized validation. Marked as FINAL to prevent tampering.
     * It relies on the map provided by {@link #getUnsupportedFeatures()}.
     *
     * @param features the set of regex features to validate
     * @throws UnsupportedOperationException if the engine explicitly unsupported a feature
     */
    @Override
    public final void checkSupport(Set<RegexFeature> features) {
        Map<RegexFeature, String> unsupported = getUnsupportedFeatures();
        for (RegexFeature feature : features) {
            String errorMessage = unsupported.get(feature);
            if (errorMessage != null) {
                throw new UnsupportedOperationException(errorMessage);
            }
        }
    }

    /**
     * Subclasses MUST implement this to provide the actual compilation logic.
     * <p>
     * This method is safely invoked only AFTER the requested features have been
     * successfully validated against the engine's capabilities.
     *
     * @param rawRegex     the raw regular expression string
     * @param usedFeatures the set of features required by the pattern
     * @return the compiled pattern specific to the underlying engine
     */
    protected abstract SiftCompiledPattern doCompile(String rawRegex, Set<RegexFeature> usedFeatures);

    /**
     * Subclasses CAN override this to provide an {@link java.util.EnumMap} of unsupported features,
     * mapped to their specific error messages.
     * <p>
     * By default, it returns an empty map, implying that the engine supports
     * all Sift generated features (e.g., the default {@link JdkEngine}).
     *
     * @return a map containing the unsupported features and their error messages
     */
    protected Map<RegexFeature, String> getUnsupportedFeatures() {
        return Collections.emptyMap();
    }
}