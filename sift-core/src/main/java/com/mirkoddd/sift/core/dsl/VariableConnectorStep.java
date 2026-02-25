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
package com.mirkoddd.sift.core.dsl;

/**
 * A specialized {@link ConnectorStep} that follows variable-length quantifiers.
 * <p>
 * This interface exposes modifiers that are only mathematically or logically valid
 * when the preceding element has a variable width (e.g., {@code +}, {@code *}, or {@code {min,max}}).
 *
 * @author Mirko Dimartino
 * @since 1.5.0
 */
public interface VariableConnectorStep extends ConnectorStep {

    /**
     * Makes the preceding quantifier "possessive" (e.g., {@code *+} or {@code ++}),
     * preventing Catastrophic Backtracking (ReDoS).
     * <p>
     * A possessive quantifier will match as many characters as possible and will <b>never</b>
     * give them back to the engine to try and satisfy the rest of the pattern.
     * <p>
     * <b>Performance Note:</b> Use this when you are certain that the matched sequence
     * should not be re-evaluated, as it significantly reduces the engine's search space.
     *
     * @return A standard {@link ConnectorStep}, as possessive modifiers cannot be stacked.
     */
    ConnectorStep withoutBacktracking();
}