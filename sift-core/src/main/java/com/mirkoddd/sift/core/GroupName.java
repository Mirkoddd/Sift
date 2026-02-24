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

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object that represents a validated group name for regex named captures.
 * <p>
 * This class cures the Primitive Obsession of using plain Strings for group names,
 * ensuring that any instantiated GroupName perfectly complies with regex syntax rules.
 */
final class GroupName {
    private static final String VALID_NAME_PATTERN = Sift.fromStart()
            .exactly(1).letters()
            .then()
            .zeroOrMore().alphanumeric()
            .andNothingElse()
            .shake();

    private static final Pattern VALIDATOR = Pattern.compile(VALID_NAME_PATTERN);

    private final String value;

    private GroupName(String value) {
        if (value == null || !VALIDATOR.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid group name: " + value +
                    ". Names must start with a letter and be alphanumeric.");
        }
        this.value = value;
    }

    static GroupName of(String value) {
        return new GroupName(value);
    }

    String getValue() { return value; }

    @Override
    public String toString() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupName groupName = (GroupName) o;
        return value.equals(groupName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}