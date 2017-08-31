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
package org.wildfly.swarm.jaxrs.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.impl.base.container.WebContainerBase;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.objectweb.asm.ClassReader;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class JAXRSArchiveImpl extends WebContainerBase<JAXRSArchive> implements JAXRSArchive {

    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Create a new JAXRS Archive with any type storage engine as backing.
     *
     * @param delegate The storage backing.
     */
    public JAXRSArchiveImpl(Archive<?> delegate) throws IOException {
        super(JAXRSArchive.class, delegate);

        addFaviconExceptionHandler();
    }

    @Override
    public JAXRSArchive addResource(Class<?> resource) {
        addClass(resource);
        return covarientReturn();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getManifestPath()
     */
    @Override
    protected ArchivePath getManifestPath() {
        return PATH_MANIFEST;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getClassesPath()
     */
    @Override
    protected ArchivePath getClassesPath() {
        return PATH_CLASSES;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getResourcePath()
     */
    @Override
    protected ArchivePath getResourcePath() {
        return PATH_RESOURCE;
    }

    public static boolean isJAXRS(Archive<?> archive) {
        Map<ArchivePath, Node> content = archive.getContent();
        for (Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
            Node node = entry.getValue();
            Asset asset = node.getAsset();
            if (isJAXRS(node.getPath(), asset)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isJAXRS(ArchivePath path, Asset asset) {
        if (asset == null) {
            return false;
        }

        if (asset instanceof ArchiveAsset) {
            return isJAXRS(((ArchiveAsset) asset).getArchive());
        }

        if (!path.get().endsWith(".class")) {
            return false;
        }
        try (InputStream in = asset.openStream()) {
            ClassReader reader = new ClassReader(in);
            JAXRSAnnotationSeekingClassVisitor visitor = new JAXRSAnnotationSeekingClassVisitor();
            reader.accept(visitor, 0);
            return visitor.isFound();
        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getLibraryPath()
     */
    @Override
    protected ArchivePath getLibraryPath() {
        return PATH_LIBRARY;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebPath()
     */
    @Override
    protected ArchivePath getWebPath() {
        return PATH_WEB;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getWebInfPath() {
        return PATH_WEB_INF;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getServiceProvidersPath() {
        return PATH_SERVICE_PROVIDERS;
    }

    // -------------------------------------------------------------------------------------||
    // Required Implementations -----------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(WebArchiveImpl.class.getName());

    /**
     * Path to the web inside of the Archive.
     */
    private static final ArchivePath PATH_WEB = ArchivePaths.root();

    /**
     * Path to the WEB-INF inside of the Archive.
     */
    private static final ArchivePath PATH_WEB_INF = ArchivePaths.create("WEB-INF");

    /**
     * Path to the resources inside of the Archive.
     */
    private static final ArchivePath PATH_RESOURCE = ArchivePaths.create(PATH_WEB_INF, "classes");

    /**
     * Path to the libraries inside of the Archive.
     */
    private static final ArchivePath PATH_LIBRARY = ArchivePaths.create(PATH_WEB_INF, "lib");

    /**
     * Path to the classes inside of the Archive.
     */
    private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_WEB_INF, "classes");

    /**
     * Path to the manifests inside of the Archive.
     */
    private static final ArchivePath PATH_MANIFEST = ArchivePaths.create("META-INF");

    /**
     * Path to web archive service providers.
     */
    private static final ArchivePath PATH_SERVICE_PROVIDERS = ArchivePaths.create(PATH_CLASSES, "META-INF/services");

}
