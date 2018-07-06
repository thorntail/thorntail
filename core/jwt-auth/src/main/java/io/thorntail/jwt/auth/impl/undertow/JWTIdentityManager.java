package io.thorntail.jwt.auth.impl.undertow;

import java.security.interfaces.RSAPublicKey;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.ParseException;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.thorntail.jwt.auth.impl.jaas.JWTCredential;
import org.jboss.logging.Logger;

/**
 * Created by bob on 3/27/18.
 */
public class JWTIdentityManager implements IdentityManager {
    private static Logger log = Logger.getLogger(JWTIdentityManager.class);

    public JWTIdentityManager() {
    }

    @Override
    public Account verify(Account account) {
        log.debug("JWT verify: " + account);
        return null;
    }

    @Override
    public Account verify(String id, Credential credential) {
        log.debug("JWT verify: " + id + " // " + credential);
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
        log.debug("JWT verify: " + credential);
        return null;
    }
}
