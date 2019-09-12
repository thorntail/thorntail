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
package org.wildfly.swarm.microprofile.jwtauth.runtime;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.microprofile.jwtauth.MicroProfileJWTAuthFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.JBossWebAsset;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

import io.smallrye.jwt.auth.jaxrs.JWTAuthorizationFilterRegistrar;

/**
 * A DeploymentProcessor implementation for the MP-JWT custom authentication mechanism that adds support
 * for that mechanism to any war the declares a login-config/auth-method = MP-JWT.
 *
 * This registers a dynamic feature that provides support for javax.annotation security annotations
 *
 */
@DeploymentScoped
public class MPJWTAuthExtensionArchivePreparer implements DeploymentProcessor {

    public static final String RESTEASY_PROVIDERS = "resteasy.providers";
    private static Logger log = Logger.getLogger(MPJWTAuthExtensionArchivePreparer.class);
    private static final String MP_JWT_AUTH_METHOD = "MP-JWT";
    private static final DotName LOGIN_CONFIG = DotName.createSimple("org.eclipse.microprofile.auth.LoginConfig");

    private final Archive archive;

    private final IndexView index;

    @Inject
    private MicroProfileJWTAuthFraction fraction;

    @Inject
    public MPJWTAuthExtensionArchivePreparer(Archive archive, IndexView index) {
        this.archive = archive;
        this.index = index;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void process() throws Exception {
        if (!fraction.isJwtEnabled().get()) {
            return;
        }
        WARArchive war = archive.as(WARArchive.class);
        // Check for LoginConfig annotation
        Collection<AnnotationInstance> lcAnnotations = index.getAnnotations(LOGIN_CONFIG);

        boolean loginConfigMpJwtAvailable = false;
        for (AnnotationInstance lc : lcAnnotations) {
            AnnotationValue authMethod = lc.value("authMethod");
            AnnotationValue realmNameProp = lc.value("realmName");
            String realm = null;
            if (realmNameProp == null) {
                realm = fraction.getJwtRealm().get();
            } else if (!fraction.getJwtRealm().get().isEmpty()
                && !fraction.getJwtRealm().get().equals(realmNameProp.asString())) {
                log.errorf("LoginConfig realmName %s and 'thorntail.microprofile.jwt.realm' %s values must be equal",
                           fraction.getJwtRealm().get(), realmNameProp.asString());
                return;
            } else {
                realm = realmNameProp.asString();
            }
            // Set the web.xml login-config auth-method and jboss-web.xml security domain
            if (authMethod != null && MP_JWT_AUTH_METHOD.equals(authMethod.asString()) && realm.length() > 0) {
                selectSecurityDomain(war, realm);
                loginConfigMpJwtAvailable = true;
            }
        }

        if (!loginConfigMpJwtAvailable) {
            if (!fraction.getJwtRealm().get().isEmpty()) {
                selectSecurityDomain(war, fraction.getJwtRealm().get());
            } else {
                return;
            }
        }
        // Handle the verification configuration on the fraction
        if (fraction.getTokenIssuer().isPresent()) {
            log.debugf("Issuer: %s", fraction.getTokenIssuer().get());
            war.addAsManifestResource(new StringAsset(fraction.getTokenIssuer().get()), "MP-JWT-ISSUER");
        }

        if (fraction.getPublicKey() != null) {
            String publicKey = fraction.getPublicKey();
            log.debugf("PublicKey: %s", publicKey);
            if (publicKey.startsWith("file:") || publicKey.startsWith("classpath:")) {
                log.warn("Using 'thorntail.microprofile.jwt.token.signer-pub-key' for the 'file:' or 'classpath:' key "
                        + "assets is deprecated, use the 'thorntail.microprofile.jwt.token.signer-pub-key-location' "
                        + "property instead");
                war.addAsManifestResource(new StringAsset(publicKey), "MP-JWT-SIGNER-KEY-LOCATION");
            } else {
                war.addAsManifestResource(new StringAsset(publicKey), "MP-JWT-SIGNER");
            }
        }

        if (fraction.getPublicKeyLocation() != null) {
            if (fraction.getPublicKey() != null) {
                log.warn("'thorntail.microprofile.jwt.token.signer-pub-key' property has already been set,"
                        + " 'thorntail.microprofile.jwt.token.signer-pub-key-location' property will be ignored");
            } else {
                log.debugf("PublicKey location: %s", fraction.getPublicKeyLocation());
                war.addAsManifestResource(new StringAsset(fraction.getPublicKeyLocation()), "MP-JWT-SIGNER-KEY-LOCATION");
            }
        }

        war.addAsManifestResource(new StringAsset("" + fraction.getExpGracePeriodSecs().get()), "MP-JWT-EXP-GRACE");

        if (fraction.isDefaultMissingMethodPermissionsDenyAccess()) {
            war.addAsManifestResource(EmptyAsset.INSTANCE, "MP-JWT-DENY-NONANNOTATED-METHODS");
        }

        if (fraction.getJwksUri() != null) {
            log.warn("Using 'thorntail.microprofile.jwt.token.jwks-uri' for the HTTPS based JWK sets is deprecated, "
                    + "use the 'thorntail.microprofile.jwt.token.signer-pub-key-location' "
                    + "property instead");
            if (fraction.getPublicKeyLocation() != null || fraction.getPublicKey() != null) {
                log.warn("One of 'thorntail.microprofile.jwt.token.signer-pub-key' or 'thorntail.microprofile.jwt.token.signer-pub-key-location'"
                        + " properties has already been set. 'thorntail.microprofile.jwt.token.jwks-uri' propery will be ignored");
            } else {
                log.debugf("JwksUri: %s", fraction.getJwksUri());
                war.addAsManifestResource(new StringAsset(fraction.getJwksUri()), "MP-JWT-SIGNER-KEY-LOCATION");
            }
        }
        if (fraction.getPublicKeyLocation() != null && fraction.getPublicKeyLocation().startsWith("https:")
            || fraction.getJwksUri() != null) {
            war.addAsManifestResource(new StringAsset(fraction.getJwksRefreshInterval().get().toString()), "MP-JWT-JWKS-REFRESH");
        }
        if (fraction.getTokenHeader() != null) {
            log.debugf("tokenHeader: %s", fraction.getTokenHeader());
            war.addAsManifestResource(new StringAsset(fraction.getTokenHeader().get()), "MP-JWT-TOKEN-HEADER");
        }
        if (fraction.getTokenCookie() != null) {
            log.debugf("tokenCookie: %s", fraction.getTokenCookie());
            war.addAsManifestResource(new StringAsset(fraction.getTokenCookie()), "MP-JWT-TOKEN-COOKIE");
        }
        if (fraction.getDefaultGroupsClaim() != null) {
            log.debugf("defaultGroupsClaim: %s", fraction.getDefaultGroupsClaim());
            war.addAsManifestResource(new StringAsset(fraction.getDefaultGroupsClaim()), "MP-JWT-DEFAULT-GROUPS-CLAIM");
        }
        if (fraction.getGroupsPath() != null) {
            log.debugf("groupsPath: %s", fraction.getGroupsPath());
            war.addAsManifestResource(new StringAsset(fraction.getGroupsPath()), "MP-JWT-GROUPS-PATH");
        }

        if (log.isTraceEnabled()) {
            log.trace("war: " + war.toString(true));
        }

        addFilterRegistrar();

        if (fraction.getRolesPropertiesMap() != null) {
            createRolePropertiesFileFromMap();
        }
    }

    private void selectSecurityDomain(WARArchive war, String realm) {
        WebXmlAsset webXml = war.findWebXmlAsset();
        webXml.setLoginConfig(MP_JWT_AUTH_METHOD, realm);
        JBossWebAsset jBossWeb = war.findJbossWebAsset();
        jBossWeb.setSecurityDomain(realm);
    }

    private void createRolePropertiesFileFromMap() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : fraction.getRolesPropertiesMap().entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        archive.add(new ByteArrayAsset(sb.toString().getBytes()), "WEB-INF/classes/autogenerated-roles.properties");
    }

    private void addFilterRegistrar() {
        JAXRSArchive jaxrsArchive = archive.as(JAXRSArchive.class);
        WebXmlAsset webXmlAsset = jaxrsArchive.findWebXmlAsset();
        String userProviders = webXmlAsset.getContextParam(RESTEASY_PROVIDERS);

        String filterRegistrar = JWTAuthorizationFilterRegistrar.class.getName();

        String providers =
                userProviders == null
                        ? filterRegistrar
                        : userProviders + "," + filterRegistrar;

        webXmlAsset.setContextParam(RESTEASY_PROVIDERS, providers);
    }
}
