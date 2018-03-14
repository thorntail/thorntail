package org.jboss.unimbus.migrate.maven;

import org.jboss.unimbus.Info;
import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.migrate.Migrator;

/**
 * Created by bob on 3/13/18.
 */
public class SimpleDependencyReplaceRule extends DependencyReplaceRule {

    public SimpleDependencyReplaceRule(String simpleName) {
        super("org.wildfly.swarm", simpleName,
              Migrator.GROUP_ID, Info.KEY + "-" + simpleName);
    }

    public SimpleDependencyReplaceRule(String simpleName, String simpleReplacement) {
        super("org.wildfly.swarm", simpleName,
              Migrator.GROUP_ID, Info.KEY + "-" + simpleReplacement);
    }
}
