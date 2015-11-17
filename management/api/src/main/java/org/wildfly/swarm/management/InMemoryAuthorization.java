package org.wildfly.swarm.management;

import org.wildfly.swarm.config.management.security_realm.PlugInAuthorization;

/**
 * @author Bob McWhirter
 */
public class InMemoryAuthorization {

    @FunctionalInterface
    public interface Consumer {
        void accept(InMemoryAuthorization authz);
    }

    private final PlugInAuthorization plugin;

    public InMemoryAuthorization(PlugInAuthorization plugin) {
        this.plugin = plugin;
    }

    public void add(String userName, String...roles) {
        this.plugin.property( userName + ".roles", (prop)->{
            String value = String.join(",", roles);
            prop.value( value );
        });

    }
}
