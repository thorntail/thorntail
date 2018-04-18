package io.thorntail.migrate.maven.rules.dependencies;

import javax.enterprise.context.ApplicationScoped;

import io.thorntail.migrate.maven.DependencyRemoveRule;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class MicroProfileConfigDependencyRule extends DependencyRemoveRule {

    protected MicroProfileConfigDependencyRule(String groupId, String artifactId) {
        super("org.wildfly.swarm", "microprofile-config");
    }
}
