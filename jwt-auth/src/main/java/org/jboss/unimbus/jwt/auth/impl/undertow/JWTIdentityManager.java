package org.jboss.unimbus.jwt.auth.impl.undertow;

import java.security.interfaces.RSAPublicKey;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import org.jboss.unimbus.jwt.auth.impl.DefaultJWTCallerPrincipalFactory;
import org.jboss.unimbus.jwt.auth.impl.JWTAuthContextInfo;
import org.jboss.unimbus.jwt.auth.impl.JWTCallerPrincipal;
import org.jboss.unimbus.jwt.auth.impl.JWTCallerPrincipalFactory;
import org.jboss.unimbus.jwt.auth.impl.ParseException;
import org.jboss.unimbus.jwt.auth.impl.jaas.JWTCredential;

/**
 * Created by bob on 3/27/18.
 */
public class JWTIdentityManager implements IdentityManager {

    public JWTIdentityManager() {
    }

    @Override
    public Account verify(Account account) {
        System.err.println("JWT verify: " + account);
        return null;
    }

    @Override
    public Account verify(String id, Credential credential) {

        System.err.println("JWT verify: " + id + " // " + credential);
        if (!(credential instanceof JWTCredential)) {
            return null;
        }

        JWTCredential jwtCredential = (JWTCredential) credential;

        RSAPublicKey signedBy = jwtCredential.getAuthContextInfo().getSignerKey();
        String issuedBy = jwtCredential.getAuthContextInfo().getIssuedBy();
        JWTAuthContextInfo authContextInfo = new JWTAuthContextInfo((RSAPublicKey) signedBy, issuedBy);
        JWTCallerPrincipalFactory factory = DefaultJWTCallerPrincipalFactory.instance();

        try {
            JWTCallerPrincipal jsonWebToken = factory.parse(jwtCredential.getBearerToken(), authContextInfo);
            return new JWTAccount(jsonWebToken, null);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public Account verify(Credential credential) {
        System.err.println("JWT verify: " + credential);
        return null;
    }
}
