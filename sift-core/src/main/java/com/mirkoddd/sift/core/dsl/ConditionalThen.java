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
 * Represents the intermediate state of a conditional regex block immediately after
 * the condition has been declared.
 * <p>
 * As part of Sift's Type-Driven builder, this interface restricts the available
 * methods to enforce the declaration of the mandatory 'Then' branch. This strictly
 * prevents the creation of incomplete or syntactically invalid conditional structures.
 */
public interface ConditionalThen {

    /**
     * Defines the pattern to use and consume if the preceding condition evaluates to TRUE.
     *
     * @param pattern The sequence to consume.
     * @return The next state of the builder, allowing the definition of an 'Else' branch
     * or the finalization of the conditional construct.
     */
    ConditionalElse thenUse(SiftPattern<Fragment> pattern);
}