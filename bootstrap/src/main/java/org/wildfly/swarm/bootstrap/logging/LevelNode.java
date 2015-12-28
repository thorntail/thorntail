package org.wildfly.swarm.bootstrap.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class LevelNode {

    private final String name;

    private final BootstrapLogger.Level level;

    private final List<LevelNode> children = new ArrayList<>();

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
}
