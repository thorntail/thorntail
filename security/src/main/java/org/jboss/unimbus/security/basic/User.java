package org.jboss.unimbus.security.basic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bob on 1/18/18.
 */
public class User {

    User(String identifier, String credentials) {
        this.identifier = identifier;
        this.credentials = credentials;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public User addRole(String role) {
        this.roles.add( role );
        return this;
    }

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(this.roles);
    }

    public boolean testCredentials(String candidate) {
        return this.credentials.equals(candidate);
    }

    private final String identifier;
    private final String credentials;

    private final Set<String> roles = new HashSet<>();
}
