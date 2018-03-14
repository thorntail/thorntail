package org.jboss.unimbus.migrate.maven.rules.dependencies;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.migrate.maven.SimpleDependencyReplaceRule;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class ResourceAdaptersDependencyRule extends SimpleDependencyReplaceRule {

    protected ResourceAdaptersDependencyRule() {
        super("resource-adapters", "jca");
    }
}
