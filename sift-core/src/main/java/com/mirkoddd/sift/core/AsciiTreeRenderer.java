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

import java.util.List;

class AsciiTreeRenderer {
    // Tree Drawing Constants
    private static final String LINE_BREAK = "\n";
    private static final String BRANCH_CONTINUE = "│  ";
    private static final String BRANCH_SPACE = "   ";
    private static final String NODE_FIRST = "┌─ ";
    private static final String NODE_MIDDLE = "├─ ";
    private static final String NODE_LAST = "└─ ";

    String render(List<ExplanationNode> nodes) {
        if (nodes.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            ExplanationNode current = nodes.get(i);
            sb.append(getPrefix(nodes, i)).append(current.text).append(LINE_BREAK);
        }
        return sb.toString().trim();
    }

    private String getPrefix(List<ExplanationNode> nodes, int index) {
        ExplanationNode current = nodes.get(index);
        StringBuilder prefix = new StringBuilder();

        // Ancestors' vertical lines
        for (int depth = 0; depth < current.level; depth++) {
            prefix.append(hasMoreChildrenAtDepth(nodes, index, depth) ? BRANCH_CONTINUE : BRANCH_SPACE);
        }

        // Current node's terminal character
        boolean last = isLastSibling(nodes, index);
        if (index == 0 && !last) {
            prefix.append(NODE_FIRST);
        } else {
            prefix.append(last ? NODE_LAST : NODE_MIDDLE);
        }

        return prefix.toString();
    }

    private boolean hasMoreChildrenAtDepth(List<ExplanationNode> nodes, int start, int depth) {
        for (int i = start + 1; i < nodes.size(); i++) {
            if (nodes.get(i).level < depth) return false;
            if (nodes.get(i).level == depth) return true;
        }
        return false;
    }

    private boolean isLastSibling(List<ExplanationNode> nodes, int start) {
        int currentLevel = nodes.get(start).level;
        for (int i = start + 1; i < nodes.size(); i++) {
            if (nodes.get(i).level < currentLevel) return true;
            if (nodes.get(i).level == currentLevel) return false;
        }
        return true;
    }
}