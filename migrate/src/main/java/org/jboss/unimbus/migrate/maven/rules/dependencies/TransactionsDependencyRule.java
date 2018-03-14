package org.jboss.unimbus.migrate.maven.rules.dependencies;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.migrate.maven.SimpleDependencyReplaceRule;

/**
 * Created by bob on 3/13/18.
 */
@ApplicationScoped
public class TransactionsDependencyRule extends SimpleDependencyReplaceRule {

    protected TransactionsDependencyRule() {
        super("jca");
    }
}
