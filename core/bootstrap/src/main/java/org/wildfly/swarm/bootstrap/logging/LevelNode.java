/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class LevelNode {

    public LevelNode(String name, BootstrapLogger.Level level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return this.name;
    }

    public BootstrapLogger.Level getLevel() {
        return this.level;
    }

    public List<LevelNode> getChildren() {
        return this.children;
    }

    public void add(String category, BootstrapLogger.Level level) {
        boolean handled = false;
        for (LevelNode child : this.children) {
            if (category.startsWith(child.name)) {
                handled = true;
                child.add(category, level);
            }
        }

        if (!handled) {
            this.children.add(new LevelNode(category, level));
        }
    }

    public BootstrapLogger.Level getLevel(String category) {
        for (LevelNode child : this.children) {
            if (category.startsWith(child.name)) {
                return child.getLevel(category);
            }
        }

        return this.level;
    }

    private final String name;

    private final BootstrapLogger.Level level;

    private final List<LevelNode> children = new ArrayList<>();
}
