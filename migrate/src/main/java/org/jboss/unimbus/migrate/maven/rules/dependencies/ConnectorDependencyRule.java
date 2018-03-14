package org.jboss.unimbus.migrate.maven.rules.dependencies;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.migrate.maven.SimpleDependencyReplaceRule;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class ConnectorDependencyRule extends SimpleDependencyReplaceRule {

    protected ConnectorDependencyRule() {
        super("connector", "jca");
    }
}
