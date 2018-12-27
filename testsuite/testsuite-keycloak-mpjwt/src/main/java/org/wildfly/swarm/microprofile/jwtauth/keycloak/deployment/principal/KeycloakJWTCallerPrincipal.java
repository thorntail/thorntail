package org.wildfly.swarm.microprofile.jwtauth.keycloak.deployment.principal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.jwt.Claims;
import org.keycloak.representations.AccessToken;

import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;

public class KeycloakJWTCallerPrincipal extends JWTCallerPrincipal {
    private Map<String, Object> claims;

    public KeycloakJWTCallerPrincipal(String rawToken, AccessToken at) {
        super(rawToken, "JWT");
        claims = getAllClaimsFromToken(at);
    }

    @Override
    public Collection<String> doGetClaimNames() {
        return claims.keySet();
    }

    @Override
    public Object getClaimValue(String claimName) {
        return claims.get(claimName);
    }

    private static Map<String, Object> getAllClaimsFromToken(AccessToken at) {
        Map<String, Object> map = new HashMap<>();
        map.put(Claims.jti.name(), at.getId());
        map.put(Claims.iat.name(), Long.valueOf(at.getIssuedAt()));
        map.put(Claims.exp.name(), Long.valueOf(at.getExpiration()));
        map.put(Claims.nbf.name(), Long.valueOf(at.getNotBefore()));
        map.put(Claims.auth_time.name(), Long.valueOf(at.getAuthTime()));
        map.put(Claims.updated_at.name(), at.getUpdatedAt());
        map.put(Claims.iss.name(), at.getIssuer());
        map.put(Claims.azp.name(), at.getIssuedFor());
        map.put(Claims.acr.name(), at.getAcr());
        map.put(Claims.aud.name(), at.getAudience());
        map.put(Claims.sub.name(), at.getSubject());
        map.put(Claims.groups.name(), at.getRealmAccess().getRoles());
        map.put(Claims.preferred_username.name(), at.getPreferredUsername());
        map.put(Claims.family_name.name(), at.getFamilyName());
        map.put(Claims.nickname.name(), at.getNickName());
        map.putAll(at.getOtherClaims());
        return map;
    }
}
