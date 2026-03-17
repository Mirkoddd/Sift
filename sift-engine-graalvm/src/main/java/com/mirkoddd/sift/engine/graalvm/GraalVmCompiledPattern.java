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

import static com.mirkoddd.sift.engine.graalvm.GraalVmDictionary.*;

import com.mirkoddd.sift.core.engine.SiftCompiledPattern;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

final class GraalVmCompiledPattern implements SiftCompiledPattern {

    private final String rawRegex;
    private final ThreadLocal<Value> threadLocalRegex;

    GraalVmCompiledPattern(String rawRegex) {
        this.rawRegex = rawRegex;
        this.threadLocalRegex = ThreadLocal.withInitial(() -> {
            Context ctx = GraalVmEngine.getContext();
            Value compiler = ctx.eval(JS_LANGUAGE_ID, JS_COMPILER_SNIPPET);
            return compiler.execute(rawRegex);
        });
    }

    @Override
    public String getRawRegex() {
        return rawRegex;
    }

    @Override
    public boolean containsMatchIn(CharSequence input) {
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);
        return jsRegex.invokeMember(MEMBER_TEST, input.toString()).asBoolean();
    }

    @Override
    public boolean matchesEntire(CharSequence input) {
        String inputStr = input.toString();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);
        Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);

        if (!result.isNull()) {
            Value indices = result.getMember(MEMBER_INDICES).getArrayElement(0);
            int start = indices.getArrayElement(0).asInt();
            int end = indices.getArrayElement(1).asInt();
            return start == 0 && end == inputStr.length();
        }
        return false;
    }

    @Override
    public Optional<String> extractFirst(CharSequence input) {
        String inputStr = input.toString();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);
        Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);

        if (!result.isNull()) {
            return Optional.of(result.getArrayElement(0).asString());
        }
        return Optional.empty();
    }

    @Override
    public List<String> extractAll(CharSequence input) {
        List<String> matches = new ArrayList<>();
        String inputStr = input.toString();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);

        while (true) {
            Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);
            if (result.isNull()) break;

            matches.add(result.getArrayElement(0).asString());
            updateLastIndexIfEmptyMatch(jsRegex, result);
        }
        return matches;
    }

    @Override
    public String replaceFirst(CharSequence input, String replacement) {
        String inputStr = input.toString();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);
        Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);

        if (result.isNull()) {
            return inputStr;
        }

        Value indices = result.getMember(MEMBER_INDICES).getArrayElement(0);
        int start = indices.getArrayElement(0).asInt();
        int end = indices.getArrayElement(1).asInt();

        return inputStr.substring(0, start) + replacement + inputStr.substring(end);
    }

    @Override
    public String replaceAll(CharSequence input, String replacement) {
        String inputStr = input.toString();
        StringBuilder sb = new StringBuilder();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);
        int lastAppendPosition = 0;

        while (true) {
            Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);
            if (result.isNull()) break;

            Value indices = result.getMember(MEMBER_INDICES).getArrayElement(0);
            int start = indices.getArrayElement(0).asInt();
            int end = indices.getArrayElement(1).asInt();

            sb.append(inputStr, lastAppendPosition, start);
            sb.append(replacement);
            lastAppendPosition = end;

            updateLastIndexIfEmptyMatch(jsRegex, result);
        }

        sb.append(inputStr.substring(lastAppendPosition));
        return sb.toString();
    }

    @Override
    public Map<String, String> extractGroups(CharSequence input) {
        String inputStr = input.toString();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);
        Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);
        return extractGroupsFromResult(result);
    }

    @Override
    public List<Map<String, String>> extractAllGroups(CharSequence input) {
        List<Map<String, String>> allGroups = new ArrayList<>();
        String inputStr = input.toString();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);

        while (true) {
            Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);
            if (result.isNull()) break;

            allGroups.add(extractGroupsFromResult(result));
            updateLastIndexIfEmptyMatch(jsRegex, result);
        }
        return allGroups;
    }

    @Override
    public List<String> splitBy(CharSequence input) {
        List<String> chunks = new ArrayList<>();
        String inputStr = input.toString();
        Value jsRegex = threadLocalRegex.get();
        jsRegex.putMember(MEMBER_LAST_INDEX, 0);
        int lastAppendPosition = 0;

        while (true) {
            Value result = jsRegex.invokeMember(MEMBER_EXEC, inputStr);
            if (result.isNull()) break;

            Value indices = result.getMember(MEMBER_INDICES).getArrayElement(0);
            int start = indices.getArrayElement(0).asInt();
            int end = indices.getArrayElement(1).asInt();

            chunks.add(inputStr.substring(lastAppendPosition, start));
            lastAppendPosition = end;

            updateLastIndexIfEmptyMatch(jsRegex, result);
        }
        chunks.add(inputStr.substring(lastAppendPosition));
        return chunks;
    }

    @Override
    public Stream<String> streamMatches(CharSequence input) {
        return extractAll(input).stream();
    }

    @Override
    public String toString() {
        return rawRegex;
    }

    private Map<String, String> extractGroupsFromResult(Value result) {
        Map<String, String> capturedGroups = new HashMap<>();

        if (result.isNull()) {
            return capturedGroups;
        }

        Value groups = result.getMember(MEMBER_GROUPS);

        if (!groups.isNull()) {
            for (String name : groups.getMemberKeys()) {
                Value val = groups.getMember(name);
                if (!val.isNull()) {
                    capturedGroups.put(name, val.asString());
                }
            }
        }
        return capturedGroups;
    }

    private void updateLastIndexIfEmptyMatch(Value jsRegex, Value result) {
        Value indices = result.getMember(MEMBER_INDICES).getArrayElement(0);
        int start = indices.getArrayElement(0).asInt();
        int end = indices.getArrayElement(1).asInt();
        if (start == end) {
            jsRegex.putMember(MEMBER_LAST_INDEX, end + 1);
        }
    }

    @Override
    public void close() {
        threadLocalRegex.remove();
    }
}