package io.thorntail.migrate.maven;

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
            if (isMatch(dependency)) {
                matches.addAll(createMatches(dependency));
            }
        }
        if (context.getDependencyManagement() != null) {
            for (Dependency dependency : context.getDependencyManagement().getDependencies()) {
                if (isMatch((dependency))) {
                    matches.addAll(createMatches(dependency));
                }
            }
        }

        return matches;
    }

    boolean isMatch(Dependency dependency) {
        if (!dependency.getGroupId().equals(this.groupId)) {
            return false;
        }
        if (this.artifactId.endsWith("*")) {
            String simple = this.artifactId.substring(0, this.artifactId.length() - 1);
            return dependency.getArtifactId().startsWith(simple);
        }

        return dependency.getArtifactId().equals(this.artifactId);
    }

    protected abstract List<? extends DependencyAction<?>> createMatches(Dependency context);

    protected final String groupId;

    protected final String artifactId;
}
