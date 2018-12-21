package org.wildfly.swarm.microprofile.jwtauth.keycloak.deployment.principal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.jwt.Claims;
import org.keycloak.representations.AccessToken;

import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;

public class KeycloakJWTCallerPrincipal extends JWTCallerPrincipal {
    private String rawToken;
    private Map<String, Object> claims;

    public KeycloakJWTCallerPrincipal(String rawToken, AccessToken at) {
        super(getPrincipalName(at));
        this.rawToken = rawToken;
        claims = getAllClaimsFromToken(at);
    }

    @Override
    public Set<String> getClaimNames() {
        return claims.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getClaim(String claimName) {
        // TODO: this requires more work to do with the conversions to JsonArray etc
        return Claims.raw_token.name().equals(claimName) ? (T)rawToken : (T)claims.get(claimName);
    }

    @Override
    public Set<String> getGroups() {
        //TODO: fix it
        return Collections.singleton("admin");
    }

    @Override
    public String toString(boolean showAll) {
        return claims.toString();
    }

    private static Map<String, Object> getAllClaimsFromToken(AccessToken at) {
        Map<String, Object> map = new HashMap<>();
        map.put(Claims.iss.name(), at.getIssuer());
        map.put(Claims.aud.name(), at.getAudience());
        map.put(Claims.sub.name(), at.getSubject());
        map.put(Claims.exp.name(), at.getExpiration());
        map.put(Claims.iat.name(), at.getIssuedAt());
        map.put(Claims.jti.name(), at.getId());
        // TODO: more work is needed to support other Claims enums which has typed equivalents in KC AccessToken
        map.putAll(at.getOtherClaims());
        return map;
    }

    private static String getPrincipalName(AccessToken token) {
        return token.getPreferredUsername() != null ? token.getPreferredUsername() : token.getSubject();
    }
}
