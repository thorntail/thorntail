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

import java.io.File;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.microprofile.jwtauth.MicroProfileJWTAuthFraction;
import org.wildfly.swarm.microprofile.jwtauth.MpJwtFilterRegistrar;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.JBossWebAsset;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

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

    @Override
    public void process() throws Exception {
        WARArchive war = archive.as(WARArchive.class);
        // Check for LoginConfig annotation
        Collection<AnnotationInstance> lcAnnotations = index.getAnnotations(LOGIN_CONFIG);
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
            if (authMethod != null) {
                WebXmlAsset webXml = war.findWebXmlAsset();
                webXml.setLoginConfig(authMethod.asString(), realm);
            }
            if (realm.length() > 0) {
                JBossWebAsset jBossWeb = war.findJbossWebAsset();
                jBossWeb.setSecurityDomain(realm);
            }
        }

        // Handle the verification configuration on the fraction
        if (fraction.getTokenIssuer().isPresent()) {
            log.debugf("Issuer: %s", fraction.getTokenIssuer().get());
            war.addAsManifestResource(new StringAsset(fraction.getTokenIssuer().get()), "MP-JWT-ISSUER");
        }

        String publicKey = fraction.getPublicKey();
        if (publicKey != null) {
            log.debugf("PublicKey: %s", publicKey);

            if (publicKey.startsWith("file:")) {
                File fileRef = new File(publicKey.substring(5, publicKey.length()));
                war.addAsManifestResource(new FileAsset(fileRef), "MP-JWT-SIGNER");
            } else if (publicKey.startsWith("classpath:")) {
                String cpref  = publicKey.substring(10, publicKey.length());
                Node node = archive.get("WEB-INF/classes/" + cpref);
                war.addAsManifestResource(node.getAsset(), "MP-JWT-SIGNER");
            } else {
                war.addAsManifestResource(new StringAsset(publicKey), "MP-JWT-SIGNER");
            }
        }

        war.addAsManifestResource(new StringAsset("" + fraction.getExpGracePeriodSecs().get()), "MP-JWT-EXP-GRACE");

        if (fraction.isDefaultMissingMethodPermissionsDenyAccess()) {
            war.addAsManifestResource(EmptyAsset.INSTANCE, "MP-JWT-DENY-NONANNOTATED-METHODS");
        }

        if (fraction.getJwksUri() != null) {
            log.debugf("JwksUri: %s", fraction.getJwksUri());
            war.addAsManifestResource(new StringAsset(fraction.getJwksUri()), "MP-JWT-JWKS");
            war.addAsManifestResource(new StringAsset(fraction.getJwksRefreshInterval().get().toString()), "MP-JWT-JWKS-REFRESH");

            if (fraction.getPublicKey() != null) { // warn that both JWKS and signing key is present
                log.warn("The 'signer-pub-key' and 'jwks-uri' configuration options are mutually exclusive, the 'jwks-uri' will be ignored.");
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("war: " + war.toString(true));
        }

        addFilterRegistrar();

        if (fraction.getRolesPropertiesMap() != null) {
            createRolePropertiesFileFromMap();
        }
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

        String filterRegistrar = MpJwtFilterRegistrar.class.getName();

        String providers =
                userProviders == null
                        ? filterRegistrar
                        : userProviders + "," + filterRegistrar;

        webXmlAsset.setContextParam(RESTEASY_PROVIDERS, providers);
    }
}
