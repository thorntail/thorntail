package io.thorntail.security.basic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base user with a password and roles.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class User {

    /**
     * Construct a new user.
     *
     * @param identifier  The username or identifier.
     * @param credentials The password to authenticate.
     */
    User(String identifier, String credentials) {
        this.identifier = identifier;
        this.credentials = credentials;
    }

    /**
     * Retrieve the username or identifier.
     *
     * @return The username or identifier.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Set the password credentials
     *
     * @param credentials The password credentials or {@code null} to unset and disable authentication.
     * @return This user object.
     */
    public User setCredentials(String credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Add a role.
     *
     * @param role The role to add.
     * @return This user object.
     */
    public User addRole(String role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Retrieve the roles of this user.
     *
     * @return The roles of this user.
     */
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(this.roles);
    }

    /**
     * Test credentials for authentication.
     *
     * @param candidate The candidate password to test.
     * @return {@code true} if the password matches, otherwise {@code false}.
     */
    public boolean testCredentials(String candidate) {
        if (this.credentials == null) {
            return false;
        }
        return this.credentials.equals(candidate);
    }

    private final String identifier;

    private String credentials;

    private final Set<String> roles = new HashSet<>();
}
