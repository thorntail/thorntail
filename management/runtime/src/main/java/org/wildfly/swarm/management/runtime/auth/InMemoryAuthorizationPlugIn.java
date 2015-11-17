package org.wildfly.swarm.management.runtime.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.domain.management.plugin.AuthorizationPlugIn;
import org.jboss.as.domain.management.plugin.PlugInConfigurationSupport;

/**
 * @author Bob McWhirter
 */
public class InMemoryAuthorizationPlugIn implements AuthorizationPlugIn, PlugInConfigurationSupport  {

    private Map<String,String[]> roles = new HashMap<>();

    @Override
    public void init(Map<String, String> configuration, Map<String, Object> sharedState) throws IOException {

        for (String key : configuration.keySet()) {
            if ( key.endsWith( ".roles")) {
                String userName = key.substring( 0, key.length() - ".roles".length());
                String value = configuration.get( key );

                String[] roles = value.split(",");

                for ( int i = 0 ; i < roles.length ; ++i ) {
                    roles[i] = roles[i].trim();
                }

                this.roles.put( userName, roles );
            }
        }
    }

    @Override
    public String[] loadRoles(String userName, String realm) throws IOException {
        if ( ! this.roles.containsKey(userName) ) {
            return new String[0];
        }

        return this.roles.get( userName );
    }

}
