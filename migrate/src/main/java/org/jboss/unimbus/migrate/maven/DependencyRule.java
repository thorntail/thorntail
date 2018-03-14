package org.jboss.unimbus.migrate.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * Created by bob on 3/13/18.
 */
public abstract class DependencyRule implements ModelRule {

    protected DependencyRule(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    @Override
    public List<DependencyAction<?>> match(Model context) {
        List<DependencyAction<?>> matches = new ArrayList<>();
        for (Dependency dependency : context.getDependencies()) {
            if (dependency.getGroupId().equals(this.groupId) && dependency.getArtifactId().equals(this.artifactId)) {
                matches.addAll(createMatches(dependency));
            }
        }
        if (context.getDependencyManagement() != null) {
            for (Dependency dependency : context.getDependencyManagement().getDependencies()) {
                if (dependency.getGroupId().equals(this.groupId) && dependency.getArtifactId().equals(this.artifactId)) {
                    matches.addAll(createMatches(dependency));
                }
            }
        }

        return matches;
    }

    protected abstract List<? extends DependencyAction<?>> createMatches(Dependency context);

    protected final String groupId;

    protected final String artifactId;
}
