package io.thorntail.security.basic;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.thorntail.security.Security;
import org.eclipse.microprofile.config.Config;

/**
 * Basic username/password security implementation.
 *
 * <p>{@link User}s may be added directly to this component, but usually they are configured
 * using standard configuration properties.</p>
 *
 * <p>Setting {@code security.USERNAME.password} and {@code security.USERNAME.roles} will define
 * a user with the username of {@code USERNAME} and the associated password and roles, where
 * {@code roles} is an array of strings.</p>
 *
 * <p>At startup time, if no users are configured, a user named {@code admin} with a generated password
 * will be created, with the password displayed on the boot console. Configuring any specific users will
 * cause the auto-creation of the {@code admin} user to be inhibited.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
@ApplicationScoped
public class BasicSecurity implements Security {

    public static final String PREFIX = "security.basic.";

    /**
     * Construct.
     *
     * <p>An application should not usually directly instantiate an instance of {@code BasicSecurity}.</p>
     */
    public BasicSecurity() {

    }

    @PostConstruct
    void init() {
        for (String name : this.config.getPropertyNames()) {
            if (name.startsWith(PREFIX)) {
                String[] parts = name.split("\\.");
                User user = null;
                if (parts.length >= 3) {
                    user = getUser(parts[2]);
                    if (user == null) {
                        user = addUser(parts[2], null);
                    }
                    if (parts.length == 4) {
                        if (parts[3].equals("password")) {
                            user.setCredentials(this.config.getValue(name, String.class));
                        } else if (parts[3].equals("roles")) {
                            String[] roles = this.config.getValue(name, String[].class);
                            for (String role : roles) {
                                user.addRole(role);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add a user.
     *
     * @param identifier  The username or identifier.
     * @param credentials The password for the user.
     * @return The newly-created user.
     */
    public User addUser(String identifier, String credentials) {
        User user = new User(identifier, credentials);
        this.users.put(identifier, user);
        return user;
    }

    /**
     * Retrieve a user by username or identifier.
     *
     * @param identifier The identifier.
     * @return The found {@link User} or {@code null} if not found.
     */
    public User getUser(String identifier) {
        return this.users.get(identifier);
    }

    /**
     * Determine if any users have been registered.
     *
     * @return {@code true} if no users are defined, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return this.users.isEmpty();
    }

    private Map<String, User> users = new HashMap<>();

    @Inject
    Config config;
}
