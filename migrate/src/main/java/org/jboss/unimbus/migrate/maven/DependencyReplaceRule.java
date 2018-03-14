package org.jboss.unimbus.migrate.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.jboss.unimbus.Info;

/**
 * Created by bob on 3/13/18.
 */
public class DependencyReplaceRule extends DependencyRule {

    protected DependencyReplaceRule(String groupId, String artifactId,
                                    String replacementGroupId, String replacementArtifactId) {
        super(groupId, artifactId);
        this.replacementGroupId = replacementGroupId;
        this.replacementArtifactId = replacementArtifactId;
    }

    @Override
    protected List<DependencyAction<?>> createMatches(Dependency dependency) {
        List<DependencyAction<?>> matches = new ArrayList<>();
        matches.add(new DependencyRemoveAction(this, dependency));
        Dependency replacement = new Dependency();
        replacement.setGroupId(this.replacementGroupId);
        replacement.setArtifactId(this.replacementArtifactId);
        replacement.setVersion("${version." + Info.KEY + "}");
        matches.add(new DependencyAddAction(this, replacement));
        return matches;
    }

    @Override
    public String toString() {
        return "replace dependency: " +
                this.groupId + ":" + this.artifactId +
                " with " +
                this.replacementGroupId + ":" + this.replacementArtifactId;
    }

    protected final String replacementGroupId;

    protected final String replacementArtifactId;
}
