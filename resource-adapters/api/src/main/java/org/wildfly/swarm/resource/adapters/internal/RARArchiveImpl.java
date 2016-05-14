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
package org.wildfly.swarm.resource.adapters.internal;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.impl.base.container.ResourceAdapterContainerBase;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapter;
import org.wildfly.swarm.config.resource.adapters.ResourceAdapterConsumer;
import org.wildfly.swarm.resource.adapters.IronJacamarXmlAsset;
import org.wildfly.swarm.resource.adapters.RARArchive;

/**
 * @author Ralf Battenfeld
 */
public class RARArchiveImpl extends ResourceAdapterContainerBase<RARArchive> implements RARArchive {

    @Override
    @SuppressWarnings("unchecked")
    public RARArchiveImpl resourceAdapter(final String key, final ResourceAdapterConsumer consumer) {
        final ResourceAdapter<?> ra = new ResourceAdapter(key);
        consumer.accept(ra);
        resourceAdapter(ra);
        return this;
    }

    @Override
    public RARArchive resourceAdapter(final ResourceAdapter ra) {
        getArchive().add(new IronJacamarXmlAsset(ra), "META-INF/ironjacamar.xml");
        return this;
    }

    @Override
    public RARArchive resourceAdapter(final File ironjacamarFile) {
        getArchive().add(new FileAsset(ironjacamarFile), "META-INF/ironjacamar.xml");
        return this;
    }

    // -------------------------------------------------------------------------------------||
    // Class Members
    // ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(RARArchiveImpl.class.getName());

    /**
     * Path to the manifests inside of the Archive.
     */
    private static final ArchivePath PATH_MANIFEST = new BasicPath("META-INF");

    /**
     * Path to the resources inside of the Archive.
     */
    private static final ArchivePath PATH_RESOURCE = new BasicPath("/");

    /**
     * Path to the application libraries.
     */
    private static final ArchivePath PATH_LIBRARY = new BasicPath("/");

    // -------------------------------------------------------------------------------------||
    // Constructor
    // ------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Create a new ResourceAdapterArchive with any type storage engine as
     * backing.
     *
     * @param delegate
     *            The storage backing.
     */
    public RARArchiveImpl(final Archive<?> delegate) {
        super(RARArchive.class, delegate);
    }

    // -------------------------------------------------------------------------------------||
    // Required Implementations
    // -----------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.declarchive.impl.base.ContainerBase#getLibraryPath()
     */
    @Override
    public ArchivePath getLibraryPath() {
        return PATH_LIBRARY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.declarchive.impl.base.ContainerBase#getResourcePath()
     */
    @Override
    protected ArchivePath getResourcePath() {
        return PATH_RESOURCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.declarchive.impl.base.ContainerBase#getManifestPath()
     */
    @Override
    protected ArchivePath getManifestPath() {
        return PATH_MANIFEST;
    }

    /**
     * Classes are not supported by ResourceAdapterArchive.
     *
     * @throws UnsupportedOperationException
     *             ResourceAdapterArchive does not support classes
     */
    @Override
    protected ArchivePath getClassesPath() {
        throw new UnsupportedOperationException("ResourceAdapterArchive does not support classes");
    }

}
