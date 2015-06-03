package org.wildfly.swarm.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class ModuleNode {

    private final String name;

    private List<ModuleNode> children = new ArrayList<>();

    public ModuleNode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addChild(ModuleNode child) {
        this.children.add(child);
    }

    public void dump() {
        Set<String> seen = new HashSet<>();
        dump("", seen, true);
    }

    private void dump(String prefix, Set<String> seen, boolean selfIsLast) {

        StringBuffer buf = new StringBuffer();
        boolean isRoot = this.name.equals("");

        if (isRoot) {
            System.err.print("application");
        } else {
            String selfPrefix = "+-";
            if (selfIsLast) {
                selfPrefix = "\\-";
            }

            System.err.print(prefix + selfPrefix + this.name);
        }

        if (seen.contains(this.name)) {
            System.err.println(" [abridged]");
            return;
        } else {
            System.err.println("");
        }

        String additionalPrefix;

        if (isRoot) {
            additionalPrefix = "";
        } else {
            if (selfIsLast) {
                additionalPrefix = "  ";
            } else {
                additionalPrefix = "| ";
            }
        }

        seen.add(this.name);
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; ++i) {
            boolean lastChild = (i + 1 == numChildren);
            this.children.get(i).dump(prefix + additionalPrefix, seen, lastChild);
        }
    }

}
