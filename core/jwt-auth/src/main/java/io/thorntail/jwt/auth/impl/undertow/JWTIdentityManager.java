package io.thorntail.jwt.auth.impl.undertow;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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
            JWTAccount account = new JWTAccount(jsonWebToken, null);
            loadRoleMapping(account);
            return account;
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public Account verify(Credential credential) {
        log.debug("JWT verify: " + credential);
        return null;
    }

    protected void loadRoleMapping(JWTAccount account) {
        try {
            InputStream is = getClass().getResourceAsStream("/jwt-roles.properties");
            if(is != null) {
                Properties props = new Properties();
                props.load(is);
                is.close();
                HashMap<String, String> mappedRoles = new HashMap<>();
                props.forEach((k, v) -> mappedRoles.put((String)k, (String)v));
                account.setMappedRoles(mappedRoles);
            }
        } catch (IOException e) {
        }
    }
}
