package org.wildfly.swarm.management;

import org.wildfly.swarm.config.management.SecurityRealmConsumer;

/**
 * @author Bob McWhirter
 */
@FunctionalInterface
public interface EnhancedSecurityRealmConsumer extends SecurityRealmConsumer<EnhancedSecurityRealm> {

}
