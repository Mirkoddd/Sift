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
package com.mirkoddd.sift.core;

/**
 * Internal cryptographic "Seal" used to lock the SiftPattern hierarchy.
 * <p>
 * <b>Design Pattern:</b> Using a single-element {@code enum} is the absolute most
 * robust way to implement the Singleton pattern in Java (as recommended by Joshua Bloch).
 * It natively prevents any instantiation warnings, provides ironclad thread-safety,
 * and strictly forbids reflexive instantiation attempts by throwing an exception.
 * <p>
 * Because this enum is package-private, external classes cannot acquire this token,
 * making it impossible for them to satisfy the internal DSL contracts.
 */
enum InternalToken {

    /**
     * The unique, globally accessible internal lock instance.
     */
    INSTANCE
}