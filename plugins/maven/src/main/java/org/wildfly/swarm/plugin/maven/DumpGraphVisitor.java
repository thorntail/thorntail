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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class DumpGraphVisitor implements GraphVisitor {

    private int indent;

    private Set<Graph.Module> seen = new HashSet<>();

    public DumpGraphVisitor() {
        this.indent = 0;
    }

    @Override
    public void visit(Graph graph) {

    }

    @Override
    public void visit(Graph.Module module) {
        System.err.println(spacer() + module.getID());

        if (this.seen.contains(module)) {
            return;
        }

        this.seen.add(module);

        ++this.indent;

        for (Graph.Module each : module.getDependents()) {
            each.accept(this);
        }

        --this.indent;
    }

    @Override
    public void visit(Graph.Artifact artifact) {
        System.err.println(spacer() + artifact.getGAV());

        ++this.indent;

        for (Graph.Module each : artifact.getDependents()) {
            each.accept(this);
        }

        --this.indent;

    }

    private String spacer() {
        String spacer = "";
        for (int i = 0; i < this.indent; ++i) {
            spacer = spacer + "  ";
        }

        return spacer;
    }
}
