package org.wildfly.swarm.management.runtime.auth;

import java.io.IOException;

import org.jboss.as.domain.management.plugin.AuthenticationPlugIn;
import org.jboss.as.domain.management.plugin.Credential;
import org.jboss.as.domain.management.plugin.Identity;

/**
 * @author Bob McWhirter
 */
public class SimpleAuthenticationPlugin implements AuthenticationPlugIn<Credential> {

    @Override
    public Identity<Credential> loadIdentity(String userName, String realm) throws IOException {
        System.err.println( "loadIdentity(" + userName + ", " + realm + ")");
        return null;
    }
}
