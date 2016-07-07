/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.keycloak.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.keycloak.Secured;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.undertow.descriptors.SecurityConstraint;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class SecuredImpl extends AssignableBase<ArchiveBase<?>> implements Secured {

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public SecuredImpl(ArchiveBase<?> archive) {
        super(archive);

        Node node = getArchive().as(JARArchive.class).get("WEB-INF/web.xml");
        if (node == null) {
            this.asset = new WebXmlAsset();
            getArchive().as(JARArchive.class).add(this.asset);
        } else {
            NamedAsset asset = (NamedAsset) node.getAsset();
            if (!(asset instanceof WebXmlAsset)) {
                this.asset = new WebXmlAsset(asset.openStream());
                getArchive().as(JARArchive.class).add(this.asset);
            } else {
                this.asset = (WebXmlAsset) asset;
            }
        }

        getArchive().as(JARArchive.class).addModule("org.wildfly.swarm.keycloak", "runtime");
        getArchive().as(JARArchive.class).addAsServiceProvider("io.undertow.servlet.ServletExtension", "org.wildfly.swarm.keycloak.runtime.SecurityContextServletExtension");

        InputStream keycloakJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json");
        if (keycloakJson == null) {

            String appArtifact = System.getProperty(BootstrapProperties.APP_ARTIFACT);

            if (appArtifact != null) {
                try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("_bootstrap/" + appArtifact)) {
                    ZipImporterImpl importer = new ZipImporterImpl(archive);
                    importer.importFrom(in);
                    Node jsonNode = archive.get("keycloak.json");
                    if (jsonNode == null) {
                        jsonNode = archive.get("WEB-INF/keycloak.json");
                    }

                    if (jsonNode != null && jsonNode.getAsset() != null) {
                        keycloakJson = jsonNode.getAsset().openStream();
                    }
                } catch (IOException e) {
                    // ignore
                    // e.printStackTrace();
                }
            }
        }

        // Setup web.xml
        this.asset.setContextParam("resteasy.scan", "true");
        this.asset.setLoginConfig("KEYCLOAK", "ignored");

        if (keycloakJson != null) {
            getArchive().as(JARArchive.class).add(createAsset(keycloakJson), "WEB-INF/keycloak.json");
        } else {
            // not adding it.
        }
    }

    @Override
    public SecurityConstraint protect() {
        return this.asset.protect();
    }

    @Override
    public SecurityConstraint protect(String urlPattern) {
        return this.asset.protect(urlPattern);
    }

    private Asset createAsset(InputStream in) {

        StringBuilder str = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line = null;

            while ((line = reader.readLine()) != null) {
                str.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayAsset(str.toString().getBytes());
    }

    private WebXmlAsset asset;
}

