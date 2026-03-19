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
 * Represents a fundamental element within the Sift Abstract Syntax Tree (AST).
 * <p>
 * This interface is the entry point for the <b>Visitor Pattern</b>, allowing external
 * components to traverse and process the regex structural nodes without altering
 * the nodes themselves. This heavily enforces the Open/Closed Principle (SOLID).
 */
interface RegexNode {

    /**
     * Accepts a visitor and dispatches the execution to the corresponding
     * visit method based on the node's specific concrete type.
     *
     * @param visitor The visitor traversing the AST (e.g., for regex string compilation
     * or natural language explanation).
     */
    void accept(PatternVisitor visitor);
}