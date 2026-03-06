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
 * Internal sealer interface.
 * <p>
 * <b>Security Mechanism:</b> This interface is package-private. Because public interfaces
 * (like {@link SiftPattern}) extend this, external users are forced to satisfy its contract
 * to implement the DSL. However, since they cannot "see" this interface outside the
 * {@code core.dsl} package, it becomes impossible for them to compile custom implementations.
 * <p>
 * This effectively emulates the modern Java {@code sealed} keyword feature for Java 8 compatibility.
 */
interface SiftInternalSealer {

    /**
     * Internal lock method.
     * <p>
     * Being part of a package-private interface, it cannot be overridden or satisfied
     * by any class outside the 'core.dsl' package. It also destroys the Single Abstract
     * Method (SAM) contract, completely preventing lambda injections.
     * * @return The internal singleton lock token.
     */
    Object ___internal_lock___();
}