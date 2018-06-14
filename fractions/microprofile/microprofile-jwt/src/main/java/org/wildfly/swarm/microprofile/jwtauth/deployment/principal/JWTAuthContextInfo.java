/**
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth.deployment.principal;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

/**
 * The public key and expected issuer needed to validate a token.
 */
public class JWTAuthContextInfo {
    private RSAPublicKey signerKey;
    private String issuedBy;
    private int expGracePeriodSecs = 60;
    private String jwksUri;
    private Integer jwksRefreshInterval;
    private HttpsJwks httpsJwks;

    /**
     * Flag that indicates whether the issuer is required and validated, or ignored, new in MP-JWT 1.1.
     */
    private boolean requireIssuer = true;
    private boolean followMpJwt11Rules;

    public JWTAuthContextInfo() {
    }

    public JWTAuthContextInfo(RSAPublicKey signerKey, String issuedBy) {
        this.signerKey = signerKey;
        this.issuedBy = issuedBy;
    }

    public JWTAuthContextInfo(JWTAuthContextInfo orig) {
        this.signerKey = orig.signerKey;
        this.issuedBy = orig.issuedBy;
        this.expGracePeriodSecs = orig.expGracePeriodSecs;
        this.jwksUri = orig.jwksUri;
        this.jwksRefreshInterval = orig.jwksRefreshInterval;
    }

    public RSAPublicKey getSignerKey() {
        return signerKey;
    }

    public List<JsonWebKey> loadJsonWebKeys() {
        synchronized (this) {
            if (jwksUri == null) {
                return Collections.emptyList();
            }

            if (httpsJwks == null) {
                httpsJwks = new HttpsJwks(jwksUri);
                httpsJwks.setDefaultCacheDuration(jwksRefreshInterval.longValue() * 60L);
            }
        }

        try {
            return httpsJwks.getJsonWebKeys().stream()
                    .filter(jsonWebKey -> "sig".equals(jsonWebKey.getUse())) // only signing keys are relevant
                    .filter(jsonWebKey -> "RS256".equals(jsonWebKey.getAlgorithm())) // MP-JWT dictates RS256 only
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to fetch JWKS from %s.", jwksUri), e);
        } catch (JoseException e) {
            throw new IllegalStateException(String.format("Unable to parse JWKS from %s.", jwksUri), e);
        }
    }

    public void setSignerKey(RSAPublicKey signerKey) {
        this.signerKey = signerKey;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public int getExpGracePeriodSecs() {
        return expGracePeriodSecs;
    }

    public void setExpGracePeriodSecs(int expGracePeriodSecs) {
        this.expGracePeriodSecs = expGracePeriodSecs;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public Integer getJwksRefreshInterval() {
        return jwksRefreshInterval;
    }

    public void setJwksRefreshInterval(Integer jwksRefreshInterval) {
        this.jwksRefreshInterval = jwksRefreshInterval;
    }

    public boolean isRequireIssuer() {
        return requireIssuer;
    }

    public void setRequireIssuer(boolean requireIssuer) {
        this.requireIssuer = requireIssuer;
    }

    /**
     * Is the {@linkplain #jwksUri} a location that follows the MP-JWT 1.1 rules for the mp.jwt.verify.publickey.location
     * property? These rules allow for any URL type to one of PEM, JWK or JWKS contents.
     * @return true if jwksUri was set from the mp.jwt.verify.publickey.location, false otherwise
     */
    public boolean isFollowMpJwt11Rules() {
        return followMpJwt11Rules;
    }

    public void setFollowMpJwt11Rules(boolean followMpJwt11Rules) {
        this.followMpJwt11Rules = followMpJwt11Rules;
    }
}
