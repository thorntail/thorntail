/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.undertow.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.undertow.descriptors.JBossWebAsset;
import org.wildfly.swarm.undertow.descriptors.SecurityConstraint;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

/**
 * @author Heiko Braun
 */
@ApplicationScoped
public class HttpSecurityPreparer implements ArchivePreparer {

    private static final Logger LOG = Logger.getLogger(HttpSecurityPreparer.class);

    private final String[] SUPPORTED_AUTH_METHODS = new String[] {"BASIC", "DIGEST", "FORM", "KEYCLOAK"};

    @Override
    public void prepareArchive(Archive<?> archive) {

        if (httpConfig == null || httpConfig.isEmpty()) {
            return;
        }

        if (!httpConfig.containsKey("login-config")) {
            throw new RuntimeException("Syntax error: HTTP security constraints requires a login-config declaration");
        }

        // unsupported auth method
        Map<String, Object> loginConfig = (Map<String, Object>)httpConfig.get("login-config");
        String authMethod = (String)loginConfig.getOrDefault("auth-method", "NONE");
        boolean isSupported = false;
        for (String supported : SUPPORTED_AUTH_METHODS) {
            if (authMethod.equals(supported)) {
                isSupported = true;
                break;
            }
        }

        if (!isSupported) {
            LOG.warn("Ignoring unsupported auth-method: " + authMethod);
            return;
        }

        WebXmlAsset webXml = findWebXml(archive);
        JBossWebAsset jbossWeb = findJBossWeb(archive);

        // Setup web.xml
        webXml.setContextParam("resteasy.scan", "true");
        webXml.setLoginConfig(authMethod, "ignored");

        // security domain
        if (loginConfig.containsKey("security-domain")) {
            jbossWeb.setSecurityDomain((String)loginConfig.get("security-domain"));
        }

        // form login
        if (loginConfig.containsKey("form-login-config")) {
            Map<String, Object> formLoginConfig = (Map<String, Object>) loginConfig.get("form-login-config");
            webXml.setFormLoginConfig(
                    "Security Realm",
                    (String)formLoginConfig.get("form-login-page"),
                    (String)formLoginConfig.get("form-error-page")
                    );
        }

        // security constraints
        List<Map<String, Object>> securityConstraints =
                (List<Map<String, Object>>) httpConfig.getOrDefault("security-constraints", Collections.EMPTY_LIST);

        for (Map<String, Object> sc : securityConstraints) {
            SecurityConstraint securityConstraint = webXml
                    .protect((String) sc.getOrDefault("url-pattern", ""));

            ((List<String>) sc.getOrDefault("methods", Collections.emptyList()))
                    .forEach(securityConstraint::withMethod);

            ((List<String>) sc.getOrDefault("roles", Collections.emptyList()))
                    .forEach(securityConstraint::withRole);
        }
    }

    private WebXmlAsset findWebXml(Archive<?> archive) {
        WebXmlAsset webXml;
        Node node = archive.as(JARArchive.class).get("WEB-INF/web.xml");
        if (node == null) {
            webXml = new WebXmlAsset();
            archive.as(JARArchive.class).add(webXml);
        } else {
            Asset asset = node.getAsset();
            if (!(asset instanceof WebXmlAsset)) {
                webXml = new WebXmlAsset(asset.openStream());
                archive.as(JARArchive.class).add(webXml);
            } else {
                webXml = (WebXmlAsset) asset;
            }
        }
        return webXml;
    }

    private JBossWebAsset findJBossWeb(Archive<?> archive) {
        JBossWebAsset jbossWeb;
        Node node = archive.as(JARArchive.class).get("WEB-INF/jboss-web.xml");
        if (node == null) {
            jbossWeb = new JBossWebAsset();
            archive.as(JARArchive.class).add(jbossWeb, "WEB-INF/jboss-web.xml");
        } else {
            Asset asset = node.getAsset();
            if (!(asset instanceof WebXmlAsset)) {
                jbossWeb = new JBossWebAsset(asset.openStream());
                archive.as(JARArchive.class).add(jbossWeb, "WEB-INF/jboss-web.xml");
            } else {
                jbossWeb = (JBossWebAsset) asset;
            }
        }
        return jbossWeb;
    }

    @Configurable("swarm.http")
    Map<String, Object> httpConfig;

}
