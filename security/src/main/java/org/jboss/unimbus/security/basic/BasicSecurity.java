package org.jboss.unimbus.security.basic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.jboss.unimbus.security.Security;

/**
 * Created by bob on 1/18/18.
 */
@ApplicationScoped
public class BasicSecurity implements Security {

    public static final String PREFIX = "security.basic.";

    @PostConstruct
    public void init() {
        for (String name : this.config.getPropertyNames()) {
            if (name.startsWith(PREFIX)) {
                String[] parts = name.split("\\.");
                User user = null;
                if (parts.length >= 3) {
                    user = getUser(parts[2]);
                    if (user == null) {
                        user = addUser(parts[2], null);
                    }
                }
                if (parts.length == 3) {
                    user.setCredentials(this.config.getValue(name, String.class));
                } else if (parts.length == 4) {
                    String[] roles = this.config.getValue(name, String[].class);
                    for (String role : roles) {
                        user.addRole(role);
                    }
                }
            }
        }
    }

    public User addUser(String identifier, String credentials) {
        User user = new User(identifier, credentials);
        this.users.put(identifier, user);
        return user;
    }

    public User getUser(String identifier) {
        return this.users.get(identifier);
    }

    public boolean isEmpty() {
        return this.users.isEmpty();
    }

    private Map<String, User> users = new HashMap<>();

    @Inject
    Config config;
}
