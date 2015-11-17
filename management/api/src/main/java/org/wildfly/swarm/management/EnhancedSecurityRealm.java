package org.wildfly.swarm.management;

import org.wildfly.swarm.config.management.SecurityRealm;
import org.wildfly.swarm.config.management.SecurityRealmConsumer;

/**
 * @author Bob McWhirter
 */
public class EnhancedSecurityRealm extends SecurityRealm<EnhancedSecurityRealm> {

    public static final String IN_MEMORY_PLUGIN_NAME = "swarm-in-memory";

    public static interface Consumer extends SecurityRealmConsumer<EnhancedSecurityRealm> {

    }

    public EnhancedSecurityRealm(String key) {
        super(key);
        plugIn( "org.wildfly.swarm.management:runtime");
    }

    public EnhancedSecurityRealm inMemoryAuthentication(InMemoryAuthentication.Consumer consumer) {
        return plugInAuthentication( (plugin)->{
            plugin.name( IN_MEMORY_PLUGIN_NAME );
            InMemoryAuthentication authn = new InMemoryAuthentication(getKey(), plugin);
            consumer.accept( authn );
        });
    }

    public EnhancedSecurityRealm inMemoryAuthorization() {
        return inMemoryAuthorization( (authz)->{} );
    }

    public EnhancedSecurityRealm inMemoryAuthorization(InMemoryAuthorization.Consumer consumer) {
        return plugInAuthorization( (plugin)->{
            plugin.name( IN_MEMORY_PLUGIN_NAME );
            InMemoryAuthorization authz = new InMemoryAuthorization(plugin);
            consumer.accept( authz );
        });
    }

}
