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
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.undertow.WARArchive;
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

        if (deploymentConfigs == null || deploymentConfigs.isEmpty()) {
            return;
        }

        // find a matching archive declaration
        Optional<String> match = deploymentConfigs.keySet()
                .stream()
                .filter(c -> archive.getName().equals(c))
                .findFirst();
        if (!match.isPresent()) {
            return; // no matching archive
        }

        Map<String, Object> matchingConfig = (Map<String, Object>) deploymentConfigs.get(match.get());
        if (!matchingConfig.containsKey("web")) {
            return; // missing web configuration
        }

        Map<String, Object> deploymentConfig = (Map<String, Object>) matchingConfig.get("web");

        // unsupported auth method
        Map<String, Object> loginConfig = (Map<String, Object>) deploymentConfig.get("login-config");
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

        WARArchive war = archive.as(WARArchive.class);
        WebXmlAsset webXml = war.findWebXmlAsset();
        JBossWebAsset jbossWeb = war.findJbossWebAsset();

        // Setup web.xml
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
                (List<Map<String, Object>>) deploymentConfig.getOrDefault("security-constraints", Collections.EMPTY_LIST);

        for (Map<String, Object> sc : securityConstraints) {
            SecurityConstraint securityConstraint = webXml
                    .protect((String) sc.getOrDefault("url-pattern", "/*"));

            ((List<String>) sc.getOrDefault("methods", Collections.emptyList()))
                    .forEach(securityConstraint::withMethod);

            ((List<String>) sc.getOrDefault("roles", Collections.emptyList()))
                    .forEach(securityConstraint::withRole);
        }
    }

    @Configurable("swarm.deployment")
    Map<String, Object> deploymentConfigs;

}
