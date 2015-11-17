package org.wildfly.swarm.management.runtime.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.domain.management.plugin.AuthenticationPlugIn;
import org.jboss.as.domain.management.plugin.Credential;
import org.jboss.as.domain.management.plugin.DigestCredential;
import org.jboss.as.domain.management.plugin.Identity;
import org.jboss.as.domain.management.plugin.PlugInConfigurationSupport;

/**
 * @author Bob McWhirter
 */
public class InMemoryAuthenticationPlugIn implements AuthenticationPlugIn<Credential>, PlugInConfigurationSupport {

    private Map<String,String> credentials = new HashMap<>();

    @Override
    public void init(Map<String, String> configuration, Map<String, Object> sharedState) throws IOException {
        for (String key : configuration.keySet()) {
            if ( key.endsWith( ".hash" ) ) {
                String userName = key.substring(0, key.length() - ".hash".length() );
                String hash = configuration.get( key );
                this.credentials.put( userName, hash );
            }
        }
    }

    @Override
    public Identity<Credential> loadIdentity(String userName, String realm) throws IOException {

        if (!this.credentials.containsKey(userName ) ) {
            return null;
        }

        String hash = this.credentials.get( userName );
        return new Identity<Credential>() {
            @Override
            public String getUserName() {
                return userName;
            }

            @Override
            public Credential getCredential() {
                return new DigestCredential(hash);
            }
        };
    }

}
