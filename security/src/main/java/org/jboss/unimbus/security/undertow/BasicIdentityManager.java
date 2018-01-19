package org.jboss.unimbus.security.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import org.jboss.unimbus.condition.IfClassPresent;
import org.jboss.unimbus.security.basic.BasicSecurity;
import org.jboss.unimbus.security.basic.User;

/**
 * Created by bob on 1/18/18.
 */
@IfClassPresent("io.undertow.Undertow")
@Dependent
public class BasicIdentityManager implements IdentityManager {

    public BasicIdentityManager() {

    }

    @Override
    public Account verify(Account account) {
        return null;
    }

    @Override
    public Account verify(String id, Credential credential) {
        User user = this.security.getUser(id);
        if ( user == null ) {
            return null;
        }
        if ( user.testCredentials( new String(((PasswordCredential)credential).getPassword() ))) {
            return new UserAccount(user);
        }
        return null;
    }

    @Override
    public Account verify(Credential credential) {
        return null;
    }

    @Inject
    BasicSecurity security;
}
