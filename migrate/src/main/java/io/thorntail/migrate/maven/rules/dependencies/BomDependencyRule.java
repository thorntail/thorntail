package io.thorntail.migrate.maven.rules.dependencies;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.migrate.maven.DependencyRemoveRule;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class BomDependencyRule extends DependencyRemoveRule {

    protected BomDependencyRule() {
        super("org.wildfly.swarm", "bom*");
    }
}
