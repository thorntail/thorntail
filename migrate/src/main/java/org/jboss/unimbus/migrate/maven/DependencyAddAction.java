package org.jboss.unimbus.migrate.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * Created by bob on 3/13/18.
 */
public class DependencyAddAction extends DependencyAction<DependencyRule> {
    public DependencyAddAction(DependencyRule rule, Dependency dependency) {
        super(rule, dependency);
    }

    @Override
    public void apply(Model model) {
        model.getDependencies().add(this.dependency);
    }
}
