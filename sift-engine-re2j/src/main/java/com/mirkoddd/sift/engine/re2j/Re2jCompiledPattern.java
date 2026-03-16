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
package com.mirkoddd.sift.engine.re2j;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An execution wrapper that adapts Google's RE2J engine to the Sift extraction API.
 * <p>
 * This class guarantees thread-safe execution (as long as a new {@link Matcher}
 * is spawned for every operation, which is standard behavior).
 */
final class Re2jCompiledPattern implements SiftCompiledPattern {

    private final Pattern pattern;
    private final String rawRegex;

    Re2jCompiledPattern(Pattern pattern, String rawRegex) {
        this.pattern = pattern;
        this.rawRegex = rawRegex;
    }

    @Override
    public boolean containsMatchIn(CharSequence input) {
        if (input == null) return false;
        return pattern.matcher(input).find();
    }

    @Override
    public boolean matchesEntire(CharSequence input) {
        if (input == null) return false;
        return pattern.matcher(input).matches();
    }

    @Override
    public Optional<String> extractFirst(CharSequence input) {
        if (input == null) return Optional.empty();
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
    }

    @Override
    public List<String> extractAll(CharSequence input) {
        if (input == null) return Collections.emptyList();
        Matcher matcher = pattern.matcher(input);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return Collections.unmodifiableList(results);
    }

    @Override
    public String replaceFirst(CharSequence input, String replacement) {
        if (input == null) return null;
        return pattern.matcher(input).replaceFirst(replacement);
    }

    @Override
    public String replaceAll(CharSequence input, String replacement) {
        if (input == null) return null;
        return pattern.matcher(input).replaceAll(replacement);
    }

    @Override
    public List<String> splitBy(CharSequence input) {
        if (input == null) return Collections.emptyList();
        // RE2J's split expects a String, not a CharSequence
        return Arrays.asList(pattern.split(input.toString()));
    }

    @Override
    public Stream<String> streamMatches(CharSequence input) {
        if (input == null) return Stream.empty();
        Matcher matcher = pattern.matcher(input);

        Spliterator<String> spliterator = new Spliterators.AbstractSpliterator<String>(
                Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
            @Override
            public boolean tryAdvance(java.util.function.Consumer<? super String> action) {
                if (matcher.find()) {
                    action.accept(matcher.group());
                    return true;
                }
                return false;
            }
        };
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public String getRawRegex() {
        return rawRegex;
    }

    @Override
    public Map<String, String> extractGroups(CharSequence input) {
        if (input == null) return Collections.emptyMap();

        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return Collections.emptyMap();
        }

        Map<String, String> extractedGroups = new HashMap<>();
        java.util.regex.Matcher nameExtractor = java.util.regex.Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(rawRegex);

        while (nameExtractor.find()) {
            String groupName = nameExtractor.group(1);
            try {
                String matchValue = matcher.group(groupName);
                if (matchValue != null) {
                    extractedGroups.put(groupName, matchValue);
                }
            } catch (IllegalArgumentException e) {
                // Defensive catch: ignores malformed or uncaptured group names
            }
        }
        return Collections.unmodifiableMap(extractedGroups);
    }

    @Override
    public List<Map<String, String>> extractAllGroups(CharSequence input) {
        if (input == null) return Collections.emptyList();

        Matcher matcher = pattern.matcher(input);
        java.util.regex.Matcher nameExtractor = java.util.regex.Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(rawRegex);

        List<String> groupNames = new ArrayList<>();
        while (nameExtractor.find()) {
            groupNames.add(nameExtractor.group(1));
        }

        List<Map<String, String>> results = new ArrayList<>();
        while (matcher.find()) {
            Map<String, String> groups = new HashMap<>();
            for (String name : groupNames) {
                try {
                    String value = matcher.group(name);
                    if (value != null) groups.put(name, value);
                } catch (IllegalArgumentException e) {
                    // Defensive catch
                }
            }
            results.add(Collections.unmodifiableMap(groups));
        }
        return Collections.unmodifiableList(results);
    }
}