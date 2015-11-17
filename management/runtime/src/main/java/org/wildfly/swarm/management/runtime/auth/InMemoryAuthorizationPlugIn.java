package org.wildfly.swarm.management.runtime.auth;

import java.io.IOException;

import org.jboss.as.domain.management.plugin.AuthorizationPlugIn;

/**
 * @author Bob McWhirter
 */
public class InMemoryAuthorizationPlugIn implements AuthorizationPlugIn {
    @Override
    public String[] loadRoles(String userName, String realm) throws IOException {
        return new String[0];
    }
}
