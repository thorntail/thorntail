package org.jboss.unimbus.migrate.maven.rules.dependencies;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.migrate.maven.DependencyRemoveRule;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class CdiDependencyRule extends DependencyRemoveRule {

    protected CdiDependencyRule() {
        super("org.wildfly.swarm", "cdi");
    }
}
