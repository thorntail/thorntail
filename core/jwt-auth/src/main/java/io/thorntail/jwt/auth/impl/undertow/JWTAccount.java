package io.thorntail.jwt.auth.impl.undertow;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.undertow.security.idm.Account;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Representation of the caller account using the JWTCallerPrincipal as an Undertow Account object.
 */
public class JWTAccount implements Account {
    private JsonWebToken principal;
    private Map<String, String> mappedRoles;

    private Account delegate;

    public JWTAccount(JsonWebToken principal, Account delegate) {
        this.principal = principal;
        this.delegate = delegate;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * TODO: this does not handle the EE role mapping requirements
     * @return
     */
    @Override
    public Set<String> getRoles() {
        //System.err.println( "--> " + this.principal.getGroups() );
        //System.err.println( "groups: " + this.principal.getGroups());
        Set<String> roles = this.principal.getGroups();
        if(mappedRoles != null && mappedRoles.size() > 0) {
            roles = applyRoleMapping(roles, mappedRoles);
        } else if(delegate instanceof JWTAccount) {
            JWTAccount jwtAccount = (JWTAccount) delegate;
            Map<String, String> delegateMappedRoles = jwtAccount.getMappedRoles();
            roles = applyRoleMapping(roles, delegateMappedRoles);
        }
        return roles;
    }

    public Map<String, String> getMappedRoles() {
        return mappedRoles;
    }

    public void setMappedRoles(Map<String, String> mappedRoles) {
        this.mappedRoles = mappedRoles;
    }

    protected Set<String> applyRoleMapping(Set<String> roles, Map<String, String> mappedRoles) {
        Set<String> allRoles = new HashSet<>(roles);
        for (String role : roles) {
            // Check for a mapping from the token role
            String mapping = mappedRoles.get(role);
            if(mapping != null) {
                String[] asRoles = mapping.split(",");
                allRoles.addAll(Arrays.asList(asRoles));
            }
        }
        // Add any user to role mapping
        String userRoles = mappedRoles.get(principal.getName());
        if(userRoles != null) {
            String[] asRoles = userRoles.split(",");
            allRoles.addAll(Arrays.asList(asRoles));
        }
        return allRoles;
    }
}
