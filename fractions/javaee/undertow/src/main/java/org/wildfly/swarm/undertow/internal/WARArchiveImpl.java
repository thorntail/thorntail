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
package org.wildfly.swarm.undertow.internal;

import java.io.IOException;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.impl.base.container.ContainerBase;
import org.jboss.shrinkwrap.impl.base.container.WebContainerBase;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class WARArchiveImpl extends WebContainerBase<WARArchive> implements WARArchive {

    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Create a new JAXRS Archive with any type storage engine as backing.
     *
     * @param delegate The storage backing.
     */
    public WARArchiveImpl(Archive<?> delegate) throws IOException {
        super(WARArchive.class, delegate);
        addFaviconExceptionHandler();
    }

    /**
     * {@inheritDoc}
     *
     * @see ContainerBase#getManifestPath()
     */
    @Override
    protected ArchivePath getManifestPath() {
        return PATH_MANIFEST;
    }

    /**
     * {@inheritDoc}
     *
     * @see ContainerBase#getClassesPath()
     */
    @Override
    protected ArchivePath getClassesPath() {
        return PATH_CLASSES;
    }

    /**
     * {@inheritDoc}
     *
     * @see ContainerBase#getResourcePath()
     */
    @Override
    protected ArchivePath getResourcePath() {
        return PATH_RESOURCE;
    }

    /**
     * {@inheritDoc}
     *
     * @see ContainerBase#getLibraryPath()
     */
    @Override
    protected ArchivePath getLibraryPath() {
        return PATH_LIBRARY;
    }

    /**
     * {@inheritDoc}
     *
     * @see WebContainerBase#getWebPath()
     */
    @Override
    protected ArchivePath getWebPath() {
        return PATH_WEB;
    }

    /**
     * {@inheritDoc}
     *
     * @see WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getWebInfPath() {
        return PATH_WEB_INF;
    }

    /**
     * {@inheritDoc}
     *
     * @see WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getServiceProvidersPath() {
        return PATH_SERVICE_PROVIDERS;
    }

    // -------------------------------------------------------------------------------------||
    // Instance Members -------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    // -------------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(WebArchiveImpl.class.getName());

    // -------------------------------------------------------------------------------------||
    // Required Implementations -----------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

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
