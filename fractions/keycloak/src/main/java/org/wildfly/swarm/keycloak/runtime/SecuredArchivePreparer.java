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
package org.wildfly.swarm.keycloak.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

@DeploymentScoped
public class SecuredArchivePreparer implements DeploymentProcessor {

    private static final Logger LOG = Logger.getLogger(SecuredArchivePreparer.class);

    @Inject
    public SecuredArchivePreparer(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws IOException {
        // Prepare the client adapter configuration.
        prepareKeycloakJsonAsset();
        // Prepare the multitenancy configuration.
        prepareKeycloakMultitenancy();
    }

    private void prepareKeycloakJsonAsset() throws IOException {
        InputStream keycloakJson = null;
        // Load the configuration from the disk or archive if the custom path value is set.
        // Otherwise try to load a default "keycloak.json" resource from the archive.
        if (keycloakJsonPath != null) {
            keycloakJson = getKeycloakJsonStream(keycloakJsonPath);
        } else {
            keycloakJson = getKeycloakJsonFromClasspath("keycloak.json");
        }

        if (keycloakJson != null) {
            archive.add(createKeycloakJsonAsset(keycloakJson), "WEB-INF/keycloak.json");
        }
    }

    private static InputStream getKeycloakJsonStream(String keycloakJsonPathValue) {
        InputStream keycloakJson = null;
        URI keycloakJsonUri = URI.create(keycloakJsonPathValue);
        if ("classpath".equals(keycloakJsonUri.getScheme())) {
            keycloakJson = getKeycloakJsonFromClasspath(keycloakJsonUri.getSchemeSpecificPart());
        } else {
            keycloakJson = getKeycloakJsonFromCustomPath(keycloakJsonUri);
            // Try to get from the classpath if the scheme is null.
            if (keycloakJson == null && keycloakJsonUri.getScheme() == null) {
                keycloakJson = getKeycloakJsonFromClasspath(keycloakJsonUri.toString());
            }
        }
        if (keycloakJson == null) {
            LOG.warn(String.format(
                "Unable to get Keycloak configuration from '%s'", keycloakJsonUri.toString()
            ));
        }
        return keycloakJson;
    }

    private static InputStream getKeycloakJsonFromCustomPath(URI keycloakJsonUri) {
        try {
            Path path = keycloakJsonUri.getScheme() != null
                ? Paths.get(keycloakJsonUri) : Paths.get(keycloakJsonUri.toString());
            return Files.newInputStream(path);
        } catch (IOException e) {
            // retry from the classpath if the scheme is null
        }

        return null;
    }

    private static InputStream getKeycloakJsonFromClasspath(String resourceName) {
        InputStream keycloakJson = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (keycloakJson == null) {

            String appArtifact = System.getProperty(BootstrapProperties.APP_ARTIFACT);

            if (appArtifact != null) {
                try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
                    Archive<?> tmpArchive = ShrinkWrap.create(JARArchive.class);
                    tmpArchive.as(ZipImporter.class).importFrom(in);
                    Node jsonNode = tmpArchive.get(resourceName);
                    if (jsonNode == null) {
                        jsonNode = getKeycloakJsonNodeFromWebInf(tmpArchive, resourceName, true);
                    }
                    if (jsonNode == null) {
                        jsonNode = getKeycloakJsonNodeFromWebInf(tmpArchive, resourceName, false);
                    }
                    if (jsonNode != null && jsonNode.getAsset() != null) {
                        keycloakJson = jsonNode.getAsset().openStream();
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return keycloakJson;
    }

    private static Node getKeycloakJsonNodeFromWebInf(Archive<?> tmpArchive, String resourceName, boolean useForwardSlash) {
        String webInfPath = useForwardSlash ? "/WEB-INF" : "WEB-INF";
        if (!resourceName.startsWith("/")) {
            resourceName = "/" + resourceName;
        }
        Node jsonNode = tmpArchive.get(webInfPath + resourceName);
        if (jsonNode == null) {
            jsonNode = tmpArchive.get(webInfPath + "/classes" + resourceName);
        }
        return jsonNode;
    }

    private Asset createKeycloakJsonAsset(InputStream in) throws IOException {
        StringBuilder str = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line = null;

            while ((line = reader.readLine()) != null) {
                str.append(line).append("\n");
            }
        }
        return new ByteArrayAsset(str.toString().getBytes());
    }

    private void prepareKeycloakMultitenancy() throws IOException {
        if (keycloakMultitenancyPaths != null && !keycloakMultitenancyPaths.isEmpty()) {
            // Prepare a relative paths to KC path deployments map.
            Map<String, KeycloakDeployment> pathDeployments = new HashMap<>();
            for (Map.Entry<String, String> entry : keycloakMultitenancyPaths.entrySet()) {
                InputStream is = getKeycloakJsonStream(entry.getValue());
                if (is == null) {
                    LOG.warn(String.format(
                        "Unable to support the multitenancy due to the '%s' being not available", entry.getValue()
                    ));
                } else {
                    pathDeployments.put(entry.getKey(), KeycloakDeploymentBuilder.build(is));
                }
            }
            try {
                // Statically initialize KeycloakAdapterConfigResolver given that
                // KeyCloak can only load it as opposed to accepting the prepared instance.
                Module module = Module.getBootModuleLoader().loadModule("org.wildfly.swarm.keycloak:deployment");
                final String resolverClassName = "org.wildfly.swarm.keycloak.deployment.KeycloakAdapterConfigResolver";
                Class<?> resolverClass = module.getClassLoader().loadClass(resolverClassName);
                Method setPathDeployments = resolverClass.getDeclaredMethod("setPathDeployments", Map.class);
                setPathDeployments.invoke(null, pathDeployments);

                // Set "keycloak.config.resolver" context parameter for KC be able to load it
                WebXmlAsset webXmlAsset = null;
                Node node = archive.as(JARArchive.class).get("WEB-INF/web.xml");
                if (node == null) {
                    webXmlAsset = new WebXmlAsset();
                    archive.as(JARArchive.class).add(webXmlAsset);
                } else {
                    Asset asset = node.getAsset();
                    if (!(asset instanceof WebXmlAsset)) {
                        webXmlAsset = new WebXmlAsset(asset.openStream());
                        archive.as(JARArchive.class).add(webXmlAsset);
                    } else {
                        webXmlAsset = (WebXmlAsset) asset;
                    }
                }
                webXmlAsset.setContextParam("keycloak.config.resolver", resolverClassName);
            } catch (Throwable e) {
                LOG.warn(String.format(
                        "KeycloakAdapterConfigResolver can not be set up, multitenancy will not be supported", e
                    ));
            }
        }
    }

    private final Archive<?> archive;

    @AttributeDocumentation("Path to Keycloak adapter configuration")
    @Configurable("swarm.keycloak.json.path")
    String keycloakJsonPath;

    @AttributeDocumentation("Keycloak multitenancy paths configuration")
    @Configurable("swarm.keycloak.multitenancy.paths")
    Map<String, String> keycloakMultitenancyPaths;

}
