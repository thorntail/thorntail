package org.jboss.unimbus.security.undertow;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import io.undertow.security.idm.Account;
import org.jboss.unimbus.security.basic.User;

/**
 * Created by bob on 1/18/18.
 */
public class UserAccount implements Account, Principal {

    public UserAccount(User user) {
        this.user = user;
    }

    @Override
    public Principal getPrincipal() {
        return this;
    }

    @Override
    public Set<String> getRoles() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return this.user.getIdentifier();
    }

    private final User user;
}
