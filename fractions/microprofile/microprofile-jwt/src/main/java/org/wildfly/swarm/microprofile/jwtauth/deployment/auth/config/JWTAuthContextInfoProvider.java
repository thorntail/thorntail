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
 *
 */

package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.config;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.KeyUtils;
import org.wildfly.swarm.microprofile.jwtauth.deployment.principal.JWTAuthContextInfo;

/**
 * A CDI provider for the JWTAuthContextInfo that obtains the necessary information from
 * MP config properties.
 */
@Dependent
public class JWTAuthContextInfoProvider {
    private static final String NONE = "NONE";
    private static final Logger log = Logger.getLogger(JWTAuthContextInfoProvider.class);

    // The MP-JWT spec defined configuration properties
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.publickey", defaultValue = NONE)
    private Optional<String> mpJwtublicKey;
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = NONE)
    private String mpJwtIssuer;
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = NONE)
    private Optional<String> mpJwtLocation;
    // Swarm fraction defined properties
    @Inject
    @ConfigProperty(name = "mpjwt.signerPublicKey", defaultValue = NONE)
    private Optional<String> publicKeyPemEnc;
    @Inject
    @ConfigProperty(name = "mpjwt.issuedBy", defaultValue = NONE)
    private String issuedBy;
    @Inject
    @ConfigProperty(name = "mpjwt.expGracePeriodSecs", defaultValue = "60")
    private Optional<Integer> expGracePeriodSecs;
    @Inject
    @ConfigProperty(name = "mpjwt.jwksUri", defaultValue = NONE)
    private Optional<String> jwksUri;
    @Inject
    @ConfigProperty(name = "mpjwt.jwksRefreshInterval", defaultValue = "60")
    private Optional<Integer> jwksRefreshInterval;

    @Produces
    Optional<JWTAuthContextInfo> getOptionalContextInfo() {
        // Log the config values
        log.debugf("init, mpJwtublicKey=%s, mpJwtIssuer=%s, mpJwtLocation=%s",
                   mpJwtublicKey.orElse("missing"), mpJwtIssuer, mpJwtLocation.orElse("missing"));
        log.debugf("init, publicKeyPemEnc=%s, issuedBy=%s, expGracePeriodSecs=%d, jwksRefreshInterval=%d",
                   publicKeyPemEnc.orElse("missing"), issuedBy, expGracePeriodSecs.get(), jwksRefreshInterval.get());

        /*
        FIXME Due to a bug in MP-Config (https://github.com/wildfly-extras/wildfly-microprofile-config/issues/43) we need to set all
        values to "NONE" as Optional Strings are populated with a ConfigProperty.defaultValue if they are absent. Fix this when MP-Config
        is repaired.
         */
        if (NONE.equals(publicKeyPemEnc.get()) && NONE.equals(jwksUri.get()) && NONE.equals(mpJwtublicKey.get()) && NONE.equals(mpJwtLocation.get())) {
            return Optional.empty();
        }
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();
        // Look to MP-JWT values first
        if (mpJwtublicKey.isPresent() && !NONE.equals(mpJwtublicKey.get())) {
            // Need to decode what this is...
            try {
                RSAPublicKey pk = (RSAPublicKey) KeyUtils.decodeJWKSPublicKey(mpJwtublicKey.get());
                contextInfo.setSignerKey(pk);
                log.debugf("mpJwtublicKey parsed as JWK(S)");
            } catch (Exception e) {
                // Try as PEM key value
                log.debugf("mpJwtublicKey failed as JWK(S), %s", e.getMessage());
                try {
                    RSAPublicKey pk = (RSAPublicKey) KeyUtils.decodePublicKey(mpJwtublicKey.get());
                    contextInfo.setSignerKey(pk);
                    log.debugf("mpJwtublicKey parsed as PEM");
                } catch (Exception e1) {
                    throw new DeploymentException(e1);
                }
            }
        } else if (publicKeyPemEnc.isPresent() && !NONE.equals(publicKeyPemEnc.get())) {
            try {
                RSAPublicKey pk = (RSAPublicKey) KeyUtils.decodePublicKey(publicKeyPemEnc.get());
                contextInfo.setSignerKey(pk);
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }

        if (mpJwtIssuer != null && !mpJwtIssuer.equals(NONE)) {
            contextInfo.setIssuedBy(mpJwtIssuer);
        } else if (issuedBy != null && !issuedBy.equals(NONE)) {
            contextInfo.setIssuedBy(issuedBy);
        } else {
            // If there is no expected issuer configured, don't validate it; new in MP-JWT 1.1
            contextInfo.setRequireIssuer(false);
        }

        if (expGracePeriodSecs.isPresent()) {
            contextInfo.setExpGracePeriodSecs(expGracePeriodSecs.get());
        }
        // The MP-JWT location can be a PEM, JWK or JWKS
        if (mpJwtLocation.isPresent() && !NONE.equals(mpJwtLocation.get())) {
            contextInfo.setJwksUri(mpJwtLocation.get());
            contextInfo.setFollowMpJwt11Rules(true);
        } else if (jwksUri.isPresent() && !NONE.equals(jwksUri.get())) {
            contextInfo.setJwksUri(jwksUri.get());
        }
        if (jwksRefreshInterval.isPresent()) {
            contextInfo.setJwksRefreshInterval(jwksRefreshInterval.get());
        }

        return Optional.of(contextInfo);
    }

    @Produces
    JWTAuthContextInfo getContextInfo() {
        return getOptionalContextInfo().get();
    }
}
