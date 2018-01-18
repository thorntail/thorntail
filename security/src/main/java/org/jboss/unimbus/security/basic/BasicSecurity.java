package org.jboss.unimbus.security.basic;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.security.Security;

/**
 * Created by bob on 1/18/18.
 */
@ApplicationScoped
public class BasicSecurity implements Security {

    @PostConstruct
    public void init() {
        addUser("bob", "pw");
    }

    public User addUser(String identifier, String credentials) {
        User user = new User(identifier, credentials);
        this.users.put( identifier, user );
        return user;
    }

    public User getUser(String identifier) {
        return this.users.get(identifier);
    }

    private Map<String,User> users = new HashMap<>();
}
