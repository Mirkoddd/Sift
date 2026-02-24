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

import com.mirkoddd.sift.core.dsl.SiftPattern;

/**
 * Represents a <b>Named Capturing Group</b> within the Sift DSL.
 * <p>
 * This class securely encapsulates a validated group name and its associated pattern.
 * Instances of this class cannot be instantiated directly; they must be created
 * using the {@code SiftPatterns.capture(String, SiftPattern)} factory method.
 * <p>
 * Once created, a {@code NamedCapture} can be passed to the builder to define the group
 * and later used to safely generate backreferences without hardcoding string names.
 */
public final class NamedCapture {
    private final GroupName name;
    private final SiftPattern pattern;

    NamedCapture(GroupName name, SiftPattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    /**
     * Retrieves the strictly validated name of this capturing group.
     * @return The alphanumeric group name.
     */
    public String getName() {
        return name.getValue();
    }

    /**
     * Retrieves the underlying pattern encapsulated by this capturing group.
     * @return The pattern to be captured.
     */
    public SiftPattern getPattern() {
        return pattern;
    }

    static NamedCapture create(GroupName name, SiftPattern pattern) {
        return new NamedCapture(name, pattern);
    }
}