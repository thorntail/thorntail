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
package org.wildfly.swarm.microprofile.jwtauth;

import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.string;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * A fraction that adds support for the MicroProfile 1.0 JWT RBAC authentication and authorization spec.
 */
@Configurable("swarm.microprofile.jwt")
@DeploymentModule(name = "org.wildfly.swarm.microprofile.jwtauth", slot = "deployment", export = true, metaInf = DeploymentModule.MetaInfDisposition.IMPORT)
public class MicroProfileJWTAuthFraction implements Fraction<MicroProfileJWTAuthFraction> {

    @AttributeDocumentation("The URI of the JWT token issuer")
    @Configurable("swarm.microprofile.jwt.token.issued-by")
    @Configurable("swarm.microprofile.jwtauth.token.issuedBy")
    private Defaultable<String> tokenIssuer = string("http://localhost");

    @AttributeDocumentation("The public key of the JWT token signer")
    @Configurable("swarm.microprofile.jwt.token.signer-pub-key")
    @Configurable("swarm.microprofile.jwtauth.token.signerPubKey")
    private String publicKey;

    @AttributeDocumentation("The JWT token expiration grace period in seconds ")
    @Configurable("swarm.microprofile.jwt.token.exp-grace-period")
    @Configurable("swarm.microprofile.jwtauth.token.expGracePeriod")
    private Defaultable<Integer> expGracePeriodSecs = integer(60);

    @AttributeDocumentation("If a JAX-RS resource has no class-level security metadata, then if this property is set to `true` and at least one resource method has security metadata all other resource methods without security metadata have an implicit `@DenyAll`, otherwise resource methods without security metadata are not secured")
    @Configurable("swarm.microprofile.jwt.default-missing-method-permissions-deny-access")
    private Defaultable<Boolean> defaultMissingMethodPermissionsDenyAccess = bool(true);

    public Defaultable<String> getTokenIssuer() {
        return tokenIssuer;
    }

    public void setTokenIssuer(Defaultable<String> tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Defaultable<Integer> getExpGracePeriodSecs() {
        return expGracePeriodSecs;
    }

    public void setExpGracePeriodSecs(Defaultable<Integer> expGracePeriodSecs) {
        this.expGracePeriodSecs = expGracePeriodSecs;
    }

    public boolean isDefaultMissingMethodPermissionsDenyAccess() {
        return defaultMissingMethodPermissionsDenyAccess.get();
    }

}
