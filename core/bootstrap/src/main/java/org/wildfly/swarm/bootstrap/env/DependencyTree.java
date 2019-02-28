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
package org.wildfly.swarm.bootstrap.env;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Each direct dependency (parent) keeps a bucket of transient dependencies (children)
 * so we can infer the origin (parent) for each transient dependency.
 *
 * Insertion order of dependencies is preserved.
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
        final Set<T> children = depTree.computeIfAbsent(parent, p -> new LinkedHashSet<>());
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
        depTree.computeIfAbsent(parent, p -> new LinkedHashSet<>());
    }

    public Collection<T> getDirectDeps() {
        return depTree.keySet();
    }

    /**
     * Is the given parent identified as a direct dependency in this tree?
     *
     * @param parent the dependency to look up in this tree.
     * @return true if the parent is a direct dependency, false otherwise.
     */
    public boolean isDirectDep(T parent) {
        return depTree.containsKey(parent);
    }

    public Collection<T> getTransientDeps(T parent) {
        Set<T> deps = depTree.get(parent);
        if (null == deps) {
            throw new IllegalArgumentException("Unknown dependency " + parent);
        }
        return deps;
    }

    private Map<T, Set<T>> depTree = new LinkedHashMap<>();

}
