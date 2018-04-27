package io.thorntail.migrate.maven;

import io.thorntail.migrate.Migrator;
import io.thorntail.Info;

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
