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
package org.wildfly.swarm.plugin.maven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.collection.UnsolvableVersionConflictException;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.visitor.PathRecordingDependencyVisitor;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionConstraint;

/**
 * @author Ken Finnigan
 */
public class NewestVersionSelector extends ConflictResolver.VersionSelector {
    @Override
    public void selectVersion(ConflictResolver.ConflictContext context) throws RepositoryException {
        ConflictGroup group = new ConflictGroup();
        for (ConflictResolver.ConflictItem item : context.getItems()) {
            DependencyNode node = item.getNode();
            VersionConstraint constraint = node.getVersionConstraint();

            boolean backtrack = false;
            boolean hardConstraint = constraint.getRange() != null;

            if (hardConstraint) {
                if (group.constraints.add(constraint)) {
                    if (group.winner != null && !constraint.containsVersion(group.winner.getNode().getVersion())) {
                        backtrack = true;
                    }
                }
            }

            if (isAcceptable(group, node.getVersion())) {
                group.candidates.add(item);

                if (backtrack) {
                    backtrack(group, context);
                } else if (group.winner == null || isNewer(item, group.winner)) {
                    group.winner = item;
                }
            } else if (backtrack) {
                backtrack(group, context);
            }
        }
        context.setWinner(group.winner);
    }

    private boolean isNewer(ConflictResolver.ConflictItem item1, ConflictResolver.ConflictItem item2) {
        return item1.getNode().getVersion().compareTo(item2.getNode().getVersion()) > 0;
    }

    private void backtrack(ConflictGroup group, ConflictResolver.ConflictContext context)
            throws UnsolvableVersionConflictException {
        group.winner = null;

        for (Iterator<ConflictResolver.ConflictItem> it = group.candidates.iterator(); it.hasNext(); ) {
            ConflictResolver.ConflictItem candidate = it.next();

            if (!isAcceptable(group, candidate.getNode().getVersion())) {
                it.remove();
            } else if (group.winner == null || isNewer(candidate, group.winner)) {
                group.winner = candidate;
            }
        }

        if (group.winner == null) {
            throw newFailure(context);
        }
    }

    private UnsolvableVersionConflictException newFailure(final ConflictResolver.ConflictContext context) {
        DependencyFilter filter = new DependencyFilter() {
            public boolean accept(DependencyNode node, List<DependencyNode> parents) {
                return context.isIncluded(node);
            }
        };
        PathRecordingDependencyVisitor visitor = new PathRecordingDependencyVisitor(filter);
        context.getRoot().accept(visitor);
        return new UnsolvableVersionConflictException(visitor.getPaths());
    }

    private boolean isAcceptable(ConflictGroup group, Version version) {
        for (VersionConstraint constraint : group.constraints) {
            if (!constraint.containsVersion(version)) {
                return false;
            }
        }
        return true;
    }

    static final class ConflictGroup {

        public ConflictGroup() {
            constraints = new HashSet<VersionConstraint>();
            candidates = new ArrayList<ConflictResolver.ConflictItem>(64);
        }

        @Override
        public String toString() {
            return String.valueOf(winner);
        }

        final Collection<VersionConstraint> constraints;

        final Collection<ConflictResolver.ConflictItem> candidates;

        ConflictResolver.ConflictItem winner;
    }
}
