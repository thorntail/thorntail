package org.jboss.unimbus.migrate.maven;

import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Dependency;

/**
 * Created by bob on 3/13/18.
 */
public class DependencyRemoveRule extends DependencyRule {

    protected DependencyRemoveRule(String groupId, String artifactId) {
        super(groupId, artifactId);
    }

    @Override
    protected List<DependencyRemoveAction> createMatches(Dependency dependency) {
        return Collections.singletonList(new DependencyRemoveAction(this, dependency));
    }

    @Override
    public String toString() {
        return "remove dependency: " + this.groupId + ":" + this.artifactId;
    }
}
