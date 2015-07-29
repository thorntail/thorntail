package org.wildfly.swarm.keycloak;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class SecurityConstraint {

    private final String urlPattern;
    private String method;
    private List<String> roles = new ArrayList<>();

    public SecurityConstraint() {
        this( "/*" );
    }

    public SecurityConstraint(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String urlPattern() {
        return this.urlPattern;
    }

    public SecurityConstraint withMethod(String method) {
        this.method = method;
        return this;
    }

    public String method() {
        return this.method;
    }

    public SecurityConstraint withRole(String role) {
        this.roles.add( role );
        return this;
    }

    public List<String> roles() {
        return this.roles;
    }
}
