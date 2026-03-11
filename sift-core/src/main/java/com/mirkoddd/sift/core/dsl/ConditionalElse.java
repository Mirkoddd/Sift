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
 * Represents the intermediate state of a conditional regex block after the 'Then' branch
 * has been defined.
 * <p>
 * From this state, you can finalize the construct by providing a fallback pattern,
 * chain another condition (Else-If), or close the conditional without an 'Else' branch.
 */
public interface ConditionalElse {

    /**
     * Defines the pattern to use and consume if the preceding condition evaluates to FALSE,
     * and completely closes the conditional block.
     *
     * @param pattern The sequence to consume if the condition fails.
     * @return A compiled {@link SiftPattern} of type {@link Fragment}, ready to be
     * safely injected into any Sift builder chain.
     */
    SiftPattern<Fragment> otherwiseUse(SiftPattern<Fragment> pattern);

    /**
     * Chains an alternative condition (Else-If) to the current conditional block.
     * <p>
     * This loops the builder state back to {@link ConditionalThen}, enforcing the
     * declaration of a corresponding 'Then' branch for this new condition.
     *
     * @param condition The pattern to evaluate (as a positive lookahead) if the first condition fails.
     * @return The {@link ConditionalThen} state to define the next branch.
     */
    ConditionalThen otherwiseIfFollowedBy(SiftPattern<Fragment> condition);

    /**
     * Closes the conditional block without an 'Else' branch.
     * <p>
     * In terms of Regex execution, this generates an empty false branch. If the initial
     * condition is FALSE, the engine will consume no characters and simply move forward
     * with the rest of the expression.
     *
     * @return A compiled {@link SiftPattern} of type {@link Fragment}, ready to be
     * safely injected into any Sift builder chain.
     */
    SiftPattern<Fragment> otherwiseNothing();
}