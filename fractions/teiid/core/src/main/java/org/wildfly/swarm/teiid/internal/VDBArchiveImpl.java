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
package org.wildfly.swarm.teiid.internal;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.impl.base.container.ContainerBase;
import org.wildfly.swarm.teiid.VDBArchive;

public class VDBArchiveImpl extends ContainerBase<VDBArchive> implements VDBArchive {
    private static final ArchivePath PATH_LIBRARY = ArchivePaths.create("lib");
    private static final ArchivePath PATH_RESOURCE = ArchivePaths.create("resources");
    private static final ArchivePath PATH_MANIFEST = ArchivePaths.create("META-INF");
    private static final ArchivePath PATH_CLASSES = ArchivePaths.create("classes");


    public VDBArchiveImpl(Archive<?> archive) {
        super(VDBArchive.class, archive);
    }

    @Override
    protected ArchivePath getManifestPath() {
        return PATH_MANIFEST;
    }

    @Override
    protected ArchivePath getResourcePath() {
        return PATH_RESOURCE;
    }

    @Override
    protected ArchivePath getClassesPath() {
        return PATH_CLASSES;
    }

    @Override
    protected ArchivePath getLibraryPath() {
        return PATH_LIBRARY;
    }

}
