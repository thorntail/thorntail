package org.jboss.unimbus.jwt.auth;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.jwt.tck.util.ITokenParser;
import org.jboss.unimbus.jwt.auth.impl.DefaultJWTCallerPrincipalFactory;
import org.jboss.unimbus.jwt.auth.impl.JWTAuthContextInfo;
import org.jboss.unimbus.jwt.auth.impl.JWTCallerPrincipalFactory;

/**
 * Created by bob on 3/27/18.
 */
public class TokenParser implements ITokenParser {
    @Override
    public JsonWebToken parse(String bearerToken, String issuer, PublicKey signedBy) throws Exception {
        System.err.println( "parsing: " + bearerToken  + " // " + issuer + " // " + signedBy);
        JWTAuthContextInfo authContextInfo = new JWTAuthContextInfo((RSAPublicKey) signedBy, issuer);
        JWTCallerPrincipalFactory factory = DefaultJWTCallerPrincipalFactory.instance();
        return factory.parse(bearerToken, authContextInfo);
    }
}
