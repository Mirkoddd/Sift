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

/**
 * Package-private dictionary containing GraalVM/JS-specific configuration tokens.
 */
final class GraalVmDictionary {

    private GraalVmDictionary() {
        // Prevent instantiation
    }

    static final String JS_LANGUAGE_ID = "js";

    // JavaScript RegExp members & properties
    static final String MEMBER_EXEC = "exec";
    static final String MEMBER_TEST = "test";
    static final String MEMBER_GROUPS = "groups";
    static final String MEMBER_INDICES = "indices";
    static final String MEMBER_LAST_INDEX = "lastIndex";

    // JS Compiler helper
    static final String JS_COMPILER_SNIPPET = "(function(pattern) { return new RegExp(pattern, 'dg'); })";
}