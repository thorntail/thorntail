package org.jboss.unimbus.migrate.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * Created by bob on 3/13/18.
 */
public class DependencyReplaceAction extends DependencyAction<DependencyReplaceRule> {
    public DependencyReplaceAction(DependencyReplaceRule rule, Dependency dependency) {
        super(rule, dependency);
    }

    @Override
    public void apply(Model model) {
        System.err.println( "applying " + this + " to " + model.getArtifactId());
        System.err.println( "remove: " + model.getDependencies().remove(this.dependency));
        Dependency dep = new Dependency();
        dep.setGroupId(getRule().replacementGroupId);
        dep.setArtifactId(getRule().replacementArtifactId);
        dep.setVersion("1.0.0-SNAPSHOT");
        model.getDependencies().add(dep);
    }
}
