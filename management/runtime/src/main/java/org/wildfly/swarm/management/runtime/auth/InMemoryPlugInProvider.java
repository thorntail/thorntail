package org.wildfly.swarm.management.runtime.auth;

import org.jboss.as.domain.management.plugin.AuthenticationPlugIn;
import org.jboss.as.domain.management.plugin.AuthorizationPlugIn;
import org.jboss.as.domain.management.plugin.Credential;
import org.jboss.as.domain.management.plugin.PlugInProvider;
import org.wildfly.swarm.management.EnhancedSecurityRealm;

/**
 * @author Bob McWhirter
 */
public class InMemoryPlugInProvider implements PlugInProvider {


    @Override
    public AuthenticationPlugIn<Credential> loadAuthenticationPlugIn(String name) {
        if ( name.equals(EnhancedSecurityRealm.IN_MEMORY_PLUGIN_NAME ) ) {
            return new InMemoryAuthenticationPlugIn();
        }
        return null;
    }

    @Override
    public AuthorizationPlugIn loadAuthorizationPlugIn(String name) {
        if ( name.equals( EnhancedSecurityRealm.IN_MEMORY_PLUGIN_NAME ) ) {
            return new InMemoryAuthorizationPlugIn();
        }
        return null;
    }
}
