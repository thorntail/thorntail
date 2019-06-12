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

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.string;

import java.util.Map;

/**
 * A fraction that adds support for the MicroProfile 1.0 JWT RBAC authentication and authorization spec.
 */
@Configurable("thorntail.microprofile.jwt")
@DeploymentModule(name = "org.wildfly.swarm.microprofile.jwtauth", slot = "deployment", export = true, metaInf = DeploymentModule.MetaInfDisposition.IMPORT)
public class MicroProfileJWTAuthFraction implements Fraction<MicroProfileJWTAuthFraction> {

    @AttributeDocumentation("The URI of the JWT token issuer")
    @Configurable("thorntail.microprofile.jwt.token.issued-by")
    @Configurable("thorntail.microprofile.jwtauth.token.issuedBy")
    private Defaultable<String> tokenIssuer = string("http://localhost");

    @AttributeDocumentation("The public key of the JWT token signer. Can be prefixed 'file:' or 'classpath:' for key assets, otherwise the key contents are expected")
    @Configurable("thorntail.microprofile.jwt.token.signer-pub-key")
    @Configurable("thorntail.microprofile.jwtauth.token.signerPubKey")
    private String publicKey;

    @AttributeDocumentation("The JWT token expiration grace period in seconds ")
    @Configurable("thorntail.microprofile.jwt.token.exp-grace-period")
    @Configurable("thorntail.microprofile.jwtauth.token.expGracePeriod")
    private Defaultable<Integer> expGracePeriodSecs = integer(60);

    @AttributeDocumentation("The JWKS URI from which to load public keys (if 'signer-pub-key' is set, this setting is ignored).")
    @Configurable("thorntail.microprofile.jwt.token.jwks-uri")
    private String jwksUri;

    @AttributeDocumentation("The interval at which the JWKS URI should be queried for keys (in minutes).")
    @Configurable("thorntail.microprofile.jwt.token.jwks-refresh-interval")
    private Defaultable<Integer> jwksRefreshInterval = integer(60);

    @AttributeDocumentation("If a JAX-RS resource has no class-level security metadata, then if this property is set to `true` and at least one resource method has security metadata all other resource methods without security metadata have an implicit `@DenyAll`, otherwise resource methods without security metadata are not secured")
    @Configurable("thorntail.microprofile.jwt.default-missing-method-permissions-deny-access")
    private Defaultable<Boolean> defaultMissingMethodPermissionsDenyAccess = bool(true);

    @AttributeDocumentation("HTTP header which is expected to contain a JWT token, default value is 'Authorization'")
    @Configurable("thorntail.microprofile.jwt.token.header")
    private Defaultable<String> tokenHeader = string("Authorization");
    @AttributeDocumentation("Cookie name containing a JWT token. This property is ignored unless the 'thorntail.microprofile.jwt.token.header' is set to 'Cookie'")
    @Configurable("thorntail.microprofile.jwt.token.cookie")
    private String tokenCookie;
    @AttributeDocumentation("Default group name. This property can be used to support the JWT tokens without a 'groups' claim.")
    @Configurable("thorntail.microprofile.jwt.claims.groups")
    private String defaultGroupsClaim;

    /**
     * Realm name
     */
    @Configurable("thorntail.microprofile.jwt.realm")
    @Configurable("thorntail.microprofile.jwtauth.realm")
    @AttributeDocumentation("If set, a security domain with this name that supports MicroProfile JWT is automatically created in the security subsystem."
                            + " The realmName parameter of the @LoginConfig annotation must be set to the same value.")
    private Defaultable<String> jwtRealm = string("");

    /**
     * Roles properties file path
     */
    @Configurable("thorntail.microprofile.jwt.roles.file")
    @Configurable("thorntail.microprofile.jwtauth.roles.file")
    @AttributeDocumentation("Roles properties file path, ignored if the roles.map property is set")
    private Defaultable<String> rolesPropertiesFile = string("");

    /**
     * The role properties which are configured directly in the project configuration file
     */
    @Configurable("thorntail.microprofile.jwt.roles.map")
    @Configurable("thorntail.microprofile.jwtauth.roles.map")
    @AttributeDocumentation("Roles properties map")
    private Map<String, String> rolesPropertiesMap;

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

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public Defaultable<Integer> getJwksRefreshInterval() {
        return jwksRefreshInterval;
    }

    public void setJwksRefreshInterval(Defaultable<Integer> jwksRefreshInterval) {
        this.jwksRefreshInterval = jwksRefreshInterval;
    }

    public boolean isDefaultMissingMethodPermissionsDenyAccess() {
        return defaultMissingMethodPermissionsDenyAccess.get();
    }

    public Defaultable<String> getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(Defaultable<String> tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public String getTokenCookie() {
        return tokenCookie;
    }

    public void setTokenCookie(String tokenCookie) {
        this.tokenCookie = tokenCookie;
    }

    public String getDefaultGroupsClaim() {
        return defaultGroupsClaim;
    }

    public void setDefaultGroupsClaim(String defaultGroupsClaim) {
        this.defaultGroupsClaim = defaultGroupsClaim;
    }

    public Defaultable<String> getJwtRealm() {
        return jwtRealm;
    }

    public void setJwtRealm(Defaultable<String> jwtRealm) {
        this.jwtRealm = jwtRealm;
    }

    public Defaultable<String> getRolesPropertiesFile() {
        return rolesPropertiesFile;
    }

    public void setRolesPropertiesFile(Defaultable<String> rolesPropertiesFile) {
        this.rolesPropertiesFile = rolesPropertiesFile;
    }

    public Map<String, String> getRolesPropertiesMap() {
        return rolesPropertiesMap;
    }

    public void setRolesPropertiesMap(Map<String, String> rolesPropertiesMap) {
        this.rolesPropertiesMap = rolesPropertiesMap;
    }

}
