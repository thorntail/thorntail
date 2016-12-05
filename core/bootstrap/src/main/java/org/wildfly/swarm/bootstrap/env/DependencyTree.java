package org.wildfly.swarm.bootstrap.env;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Each direct dependency (parent) keeps a bucket of transient dependencies (children)
 * so we can infer the origin (parent) for each transient dependency.
 *
 * @author Heiko Braun
 * @since 05/12/2016
 */
public class DependencyTree<T> {

    /**
     * A transient dependencies linked ot a parent (origin)
     * @param parent
     */
    public void add(T parent, T child) {
        if(!depTree.keySet().contains(parent)) {
            depTree.put(parent, new HashSet<>());
        }
        if(!child.equals(parent))
            depTree.get(parent).add(child);
    }


    /**
     * Direct dep without any transient dependencies
     * @param parent
     */
    public void add(T parent) {
        if(!depTree.keySet().contains(parent)) {
            depTree.put(parent, new HashSet<>());
        }
    }

    public Set<T> getDirectDeps() {
        return depTree.keySet();
    }

    public Set<T> getTransientDeps(T parent) {
        Set<T> deps = depTree.get(parent);
        if(null==deps)
            throw new IllegalArgumentException("Unknown dependency "+parent);
        return deps;
    }

    protected Map<T,Set<T>> depTree = new HashMap<>();

}
