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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

@DeploymentScoped
public class SecuredArchivePreparer implements DeploymentProcessor {

    private static final Logger LOG = Logger.getLogger(SecuredArchivePreparer.class);

    @Inject
    public SecuredArchivePreparer(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws IOException {
        InputStream keycloakJson = null;
        if (keycloakJsonPath != null) {
            keycloakJson = getKeycloakJsonFromCustomPath();
        }
        if (keycloakJson == null) {
            keycloakJson = getKeycloakJson();
        }

        if (keycloakJson != null) {
            archive.add(createAsset(keycloakJson), "WEB-INF/keycloak.json");
        } else {
            // not adding it.
        }

    }

    private InputStream getKeycloakJsonFromCustomPath() {
        URI keycloakJsonUri = URI.create(keycloakJsonPath);
        try {
            Path path = keycloakJsonUri.getScheme() != null
                ? Paths.get(keycloakJsonUri) : Paths.get(keycloakJsonPath);
            return Files.newInputStream(path);
        } catch (IOException e) {
            LOG.warn(String.format(
                    "Unable to get keycloak.json from '%s', fall back to get from classpath: %s",
                    keycloakJsonPath, e
            ));
        }

        return null;
    }

    private InputStream getKeycloakJson() {
        InputStream keycloakJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
        if (keycloakJson == null) {

            String appArtifact = System.getProperty(BootstrapProperties.APP_ARTIFACT);

            if (appArtifact != null) {
                try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
                    Archive tmpArchive = ShrinkWrap.create(JARArchive.class);
                    tmpArchive.as(ZipImporter.class).importFrom(in);
                    Node jsonNode = tmpArchive.get("keycloak.json");
                    if (jsonNode == null) {
                        jsonNode = tmpArchive.get("WEB-INF/keycloak.json");
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

    private Asset createAsset(InputStream in) throws IOException {
        StringBuilder str = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line = null;

            while ((line = reader.readLine()) != null) {
                str.append(line).append("\n");
            }
        }
        return new ByteArrayAsset(str.toString().getBytes());
    }

    private final Archive archive;

    @AttributeDocumentation("Path to keycloak.json configuration")
    @Configurable("swarm.keycloak.json.path")
    String keycloakJsonPath;

}
