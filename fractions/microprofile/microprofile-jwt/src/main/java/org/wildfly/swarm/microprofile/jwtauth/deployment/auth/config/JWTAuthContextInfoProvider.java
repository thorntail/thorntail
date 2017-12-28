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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;

import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.KeyUtils;
import org.wildfly.swarm.microprofile.jwtauth.deployment.principal.JWTAuthContextInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 */
@Dependent
public class JWTAuthContextInfoProvider {
    @Inject
    @ConfigProperty(name = "mpjwt.signerPublicKey")
    private Optional<String> publicKeyPemEnc;
    @Inject
    @ConfigProperty(name = "mpjwt.issuedBy", defaultValue = "NONE")
    private String issuedBy;
    @Inject
    @ConfigProperty(name = "mpjwt.expGracePeriodSecs", defaultValue = "60")
    private Optional<Integer> expGracePeriodSecs;

    @PostConstruct
    void init() {
    }

    @Produces
    Optional<JWTAuthContextInfo> getOptionalContextInfo() {
        if (!publicKeyPemEnc.isPresent()) {
            return Optional.empty();
        }
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();
        try {
            RSAPublicKey pk = (RSAPublicKey) KeyUtils.decodePublicKey(publicKeyPemEnc.get());
            contextInfo.setSignerKey(pk);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        if (issuedBy != null && !issuedBy.equals("NONE")) {
            contextInfo.setIssuedBy(issuedBy);
        }
        if (expGracePeriodSecs.isPresent()) {
            contextInfo.setExpGracePeriodSecs(expGracePeriodSecs.get());
        }
        return Optional.of(contextInfo);
    }
    @Produces
    JWTAuthContextInfo getContextInfo() {
        return getOptionalContextInfo().get();
    }
}
