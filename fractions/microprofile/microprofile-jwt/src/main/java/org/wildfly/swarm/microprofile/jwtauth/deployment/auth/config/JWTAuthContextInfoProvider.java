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

import io.smallrye.jwt.KeyUtils;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;

/**
 * An extension of the SmallRey CDI provider for the JWTAuthContextInfo that extends the information from
 * MP-JWT config properties with legacy fraction properties as well as properties for features not
 * covered by MP-JWT settings.
 */
@Dependent
public class JWTAuthContextInfoProvider extends io.smallrye.jwt.config.JWTAuthContextInfoProvider {
    private static final String NONE = "NONE";
    private static final Logger log = Logger.getLogger(JWTAuthContextInfoProvider.class);

    // Legacy fraction properties that have MP-JWT equivalents that take precedence
    @Inject
    @ConfigProperty(name = "mpjwt.signerPublicKey", defaultValue = NONE)
    private Optional<String> publicKeyPemEnc;
    @Inject
    @ConfigProperty(name = "mpjwt.issuedBy", defaultValue = NONE)
    private String issuedBy;
    // Fraction properties that don't have MP-JWT equivalents
    @Inject
    @ConfigProperty(name = "mpjwt.expGracePeriodSecs", defaultValue = "60")
    private Optional<Integer> expGracePeriodSecs;
    @Inject
    @ConfigProperty(name = "mpjwt.jwksUri", defaultValue = NONE)
    private Optional<String> jwksUri;
    @Inject
    @ConfigProperty(name = "mpjwt.jwksRefreshInterval", defaultValue = "60")
    private Optional<Integer> jwksRefreshInterval;

    /**
     * Produce the JWTAuthContextInfo from a combination of the MP-JWT properties and the extended
     * fraction defined properties.
     * @return an Optional wrapper for the configured JWTAuthContextInfo
     */
    @Produces
    Optional<JWTAuthContextInfo> getOptionalContextInfo() {
        // Log the config values
        log.debugf("init, publicKeyPemEnc=%s, issuedBy=%s, expGracePeriodSecs=%d, jwksRefreshInterval=%d",
                   publicKeyPemEnc.orElse("missing"), issuedBy, expGracePeriodSecs.get(), jwksRefreshInterval.get());

        /*
        FIXME Due to a bug in MP-Config (https://github.com/wildfly-extras/wildfly-microprofile-config/issues/43) we need to set all
        values to "NONE" as Optional Strings are populated with a ConfigProperty.defaultValue if they are absent. Fix this when MP-Config
        is repaired.
         */
        if (NONE.equals(publicKeyPemEnc.get()) && NONE.equals(jwksUri.get()) &&
                NONE.equals(super.getMpJwtPublicKey().get()) && NONE.equals(super.getMpJwtLocation().get())) {
            return Optional.empty();
        }
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();
        // Look to MP-JWT values first
        if (super.getMpJwtPublicKey().isPresent() && !NONE.equals(super.getMpJwtPublicKey().get())) {
            super.decodeMpJwtPublicKey(contextInfo);
        } else if (publicKeyPemEnc.isPresent() && !NONE.equals(publicKeyPemEnc.get())) {
            try {
                RSAPublicKey pk = (RSAPublicKey) KeyUtils.decodePublicKey(publicKeyPemEnc.get());
                contextInfo.setSignerKey(pk);
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }

        String mpJwtIssuer = super.getMpJwtIssuer();
        if (mpJwtIssuer != null && !mpJwtIssuer.equals(NONE)) {
            contextInfo.setIssuedBy(mpJwtIssuer);
        } else if (issuedBy != null && !issuedBy.equals(NONE)) {
            contextInfo.setIssuedBy(issuedBy);
        }

        Optional<Boolean> mpJwtRequireIss = super.getMpJwtRequireIss();
        if (mpJwtRequireIss != null && mpJwtRequireIss.isPresent()) {
            contextInfo.setRequireIssuer(mpJwtRequireIss.get());
        } else {
            // Default is to require iss claim
            contextInfo.setRequireIssuer(true);
        }

        if (expGracePeriodSecs.isPresent()) {
            contextInfo.setExpGracePeriodSecs(expGracePeriodSecs.get());
        }
        // The MP-JWT location can be a PEM, JWK or JWKS
        Optional<String> mpJwtLocation = super.getMpJwtLocation();
        if (mpJwtLocation.isPresent() && !NONE.equals(mpJwtLocation.get())) {
            super.setMpJwtLocation(contextInfo);
        } else if (jwksUri.isPresent() && !NONE.equals(jwksUri.get())) {
            contextInfo.setJwksUri(jwksUri.get());
        }
        if (jwksRefreshInterval.isPresent()) {
            contextInfo.setJwksRefreshInterval(jwksRefreshInterval.get());
        }

        super.setTokenHeadersAndGroups(contextInfo);
        return Optional.of(contextInfo);
    }

    @Produces
    JWTAuthContextInfo getJWTAuthContextInfo() {
        return getOptionalContextInfo().get();
    }
}
