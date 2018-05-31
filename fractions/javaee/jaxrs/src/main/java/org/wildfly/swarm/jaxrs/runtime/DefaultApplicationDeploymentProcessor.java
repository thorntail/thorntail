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
package org.wildfly.swarm.jaxrs.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Application;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.objectweb.asm.ClassReader;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.JAXRSMessages;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

@DeploymentScoped
public class DefaultApplicationDeploymentProcessor implements DeploymentProcessor {

    /**
     * Path to the web.xml descriptor.
     */
    static final ArchivePath PATH_WEB_XML = ArchivePaths.create(ArchivePaths.create("WEB-INF"), "web.xml");

    @AttributeDocumentation("Set the JAX-RS application path. If set, Thorntail will automatically generate a JAX-RS" +
            " Application class and use this value as the @ApplicationPath")
    @Configurable("swarm.deployment.*.jaxrs.application-path")
    Defaultable<String> applicationPath = Defaultable.string("/");

    private final Archive archive;

    @Inject
    DeploymentContext deploymentContext;

    @Inject
    public DefaultApplicationDeploymentProcessor(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws Exception {
        if (this.deploymentContext != null && this.deploymentContext.isImplicit()) {
            return;
        }

        if (!archive.getName().endsWith(".war")) {
            return;
        }

        if (hasApplicationPathOrServletMapping(archive)) {
            return;
        }

        if (applicationPath.isExplicit()) {
            addGeneratedApplication(archive.as(JAXRSArchive.class));
        }
    }

    private void addGeneratedApplication(JAXRSArchive archive) {
        String name = "org.wildfly.swarm.generated.WildFlySwarmDefaultJAXRSApplication";
        String path = "WEB-INF/classes/" + name.replace('.', '/') + ".class";

        byte[] generatedApp;
        try {
            generatedApp = DefaultApplicationFactory.create(name, applicationPath.get());
            archive.add(new ByteArrayAsset(generatedApp), path);
            archive.addHandlers(new ApplicationHandler(archive, path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean hasApplicationPathAnnotation(Archive<?> archive) {
        Map<ArchivePath, Node> content = archive.getContent();
        for (Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
            Node node = entry.getValue();
            Asset asset = node.getAsset();
            if (hasApplicationPathAnnotation(node.getPath(), asset)) {
                return true;
            }
        }

        return false;
    }


    static boolean hasApplicationPathAnnotation(ArchivePath path, Asset asset) {
        if (asset == null) {
            return false;
        }

        if (asset instanceof ArchiveAsset) {
            return hasApplicationPathAnnotation(((ArchiveAsset) asset).getArchive());
        }

        if (!path.get().endsWith(".class")) {
            return false;
        }

        try (InputStream in = asset.openStream()) {
            ClassReader reader = new ClassReader(in);
            ApplicationPathAnnotationSeekingClassVisitor visitor = new ApplicationPathAnnotationSeekingClassVisitor();
            reader.accept(visitor, 0);
            return visitor.isFound();
        } catch (IOException ignored) {
        }

        return false;
    }

    private static boolean hasApplicationServletMapping(Archive<?> archive) {
        Node webXmlNode = archive.get(PATH_WEB_XML);
        if (webXmlNode != null) {
            return hasApplicationServletMapping(webXmlNode.getAsset());
        }
        return false;
    }

    static boolean hasApplicationServletMapping(Asset asset) {
        if (asset == null) {
            return false;
        }
        WebXmlAsset webXmlAsset;
        if (asset instanceof WebXmlAsset) {
            webXmlAsset = (WebXmlAsset) asset;
        } else {
            try {
                webXmlAsset = new WebXmlAsset(asset.openStream());
            } catch (Exception e) {
                JAXRSMessages.MESSAGES.unableToParseWebXml(e);
                return false;
            }
        }
        return !webXmlAsset.getServletMapping(Application.class.getName()).isEmpty();
    }

    /**
     * See also JAX-RS spec, section 2.3.2 Servlet.
     *
     * @param archive
     * @return <code>true</code> if there is an  {@link javax.ws.rs.core.Application} subclass annotated with {@link javax.ws.rs.ApplicationPath} or web.xml with
     * mapping for <code>javax.ws.rs.core.Application</code> servlet, <code>false</code> otherwise
     */
    private boolean hasApplicationPathOrServletMapping(Archive<?> archive) {
        return hasApplicationServletMapping(archive) || hasApplicationPathAnnotation(archive);
    }

}
