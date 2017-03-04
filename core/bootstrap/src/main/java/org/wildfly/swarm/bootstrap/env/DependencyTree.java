package org.wildfly.swarm.bootstrap.env;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Each direct dependency (parent) keeps a bucket of transient dependencies (children)
 * so we can infer the origin (parent) for each transient dependency.
 *
 * @author Heiko Braun
 * @author Ken Finnigan
 * @since 05/12/2016
 */
public class DependencyTree<T> {

    /**
     * A transient dependencies linked ot a parent (origin)
     *
     * @param parent
     */
    public void add(T parent, T child) {
        final Set<T> children = depTree.computeIfAbsent(parent, p -> new HashSet<>());
        if (!child.equals(parent)) {
            children.add(child);
        }
    }

    /**
     * Direct dep without any transient dependencies
     *
     * @param parent
     */
    public void add(T parent) {
        depTree.computeIfAbsent(parent, p -> new HashSet<>());
    }

    public Collection<T> getDirectDeps() {
        return depTree
                .keySet()
                .stream()
                .sorted(this::comparator)
                .collect(Collectors.toList());
    }

    protected int comparator(T first, T second) {
        return 0;
    }

    public Collection<T> getTransientDeps(T parent) {
        Set<T> deps = depTree.get(parent);
        if (null == deps) {
            throw new IllegalArgumentException("Unknown dependency " + parent);
        }
        return deps;
    }

    protected Map<T, Set<T>> depTree = new HashMap<>();

}
