package org.jboss.unimbus.migrate.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * Created by bob on 3/13/18.
 */
public class DependencyRemoveAction extends DependencyAction<DependencyRule> {
    public DependencyRemoveAction(DependencyRule rule, Dependency dependency) {
        super(rule, dependency);
    }

    @Override
    public void apply(Model model) {
        model.getDependencies().remove(this.dependency);
        if ( model.getDependencyManagement() != null ) {
            model.getDependencyManagement().getDependencies().remove(this.dependency);
        }
    }
}
