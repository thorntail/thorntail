package org.jboss.unimbus.migrate.maven.rules.dependencies;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.migrate.maven.SimpleDependencyReplaceRule;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class MessagingDependencyRule extends SimpleDependencyReplaceRule {

    protected MessagingDependencyRule() {
        super("messaging", "jms");
    }
}
