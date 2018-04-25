package org.wildfly.swarm.microprofile.jwtauth;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

final class JwtTool {
    private final KeyTool keyTool;
    private final String issuer;

    JwtTool(final KeyTool keyTool, final String issuer) {
        this.keyTool = keyTool;
        this.issuer = issuer;
    }

    String generateSignedJwt() {
        return generateSignedJwt("FAKE_USER");
    }

    /**
     * Generates a base64-encoded signed JWT that expires after one hour and has the claims "sub" and
     * "preferred_username" set to the provided subject string.
     *
     * @param subject string to use for "sub" and "preferred_username".
     * @return a base64-encoded signed JWT token.
     */
    String generateSignedJwt(final String subject) {
        final Instant now = Instant.now();
        final Instant later = now.plus(1, ChronoUnit.HOURS);
        final JsonObject joseHeader = Json.createObjectBuilder()
                .add("kid", keyTool.getJwkKeyId())
                .add("typ", "JWT")
                .add("alg", "RS256")
                .build();
        final JsonObject jwtClaims = Json.createObjectBuilder()
                .add("jti", UUID.randomUUID().toString())
                .add("sub", subject)
                .add("preferred_username", subject)
                .add("groups", Json.createArrayBuilder().add("group1").add("group2"))
                .add("aud", "microprofile-jwt-testsuite")
                .add("iss", issuer)
                .add("iat", now.getEpochSecond())
                .add("exp", later.getEpochSecond())
                .build();

        try {
            final byte[] joseBytes = joseHeader.toString().getBytes("UTF-8");
            final byte[] claimBytes = jwtClaims.toString().getBytes("UTF-8");

            final String joseAndClaims = Base64.getUrlEncoder().encodeToString(joseBytes) + "." +
                    Base64.getUrlEncoder().encodeToString(claimBytes);

            final Signature sha256withRSA = Signature.getInstance("SHA256withRSA");
            sha256withRSA.initSign(keyTool.getPrivateKey());
            sha256withRSA.update(joseAndClaims.getBytes("UTF-8"));

            return joseAndClaims + "." + Base64.getUrlEncoder().encodeToString(sha256withRSA.sign());
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Could not sign JWT using SHA256withRSA.", e);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("JVM does not support UTF-8, by spec this can not happen.", e);
        }
    }
}
