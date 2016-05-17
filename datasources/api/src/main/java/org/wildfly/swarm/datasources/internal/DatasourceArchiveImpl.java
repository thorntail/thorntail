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
package org.wildfly.swarm.datasources.internal;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.impl.base.container.ContainerBase;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.DataSourceConsumer;
import org.wildfly.swarm.datasources.DSXmlAsset;
import org.wildfly.swarm.datasources.DatasourceArchive;

/**
 * @author Bob McWhirter
 */
public class DatasourceArchiveImpl extends ContainerBase<DatasourceArchive> implements DatasourceArchive {
    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public DatasourceArchiveImpl(Archive<?> archive) {
        super(DatasourceArchive.class, archive);
    }


    @SuppressWarnings("unchecked")
    public DatasourceArchive dataSource(String key, DataSourceConsumer consumer) {
        DataSource ds = new DataSource(key);
        consumer.accept(ds);
        dataSource(ds);
        return this;
    }

    @Override
    public DatasourceArchive dataSource(DataSource ds) {
        if (ds.jndiName() == null) {
            ds.jndiName("java:jboss/datasources/" + ds.getKey());
        }

        String name = ds.getKey() + "-ds.xml";

        getArchive().add(new DSXmlAsset(ds), "META-INF/" + name);

        return this;
    }

    @Override
    protected ArchivePath getManifestPath() {
        return null;
    }

    @Override
    protected ArchivePath getResourcePath() {
        return null;
    }

    @Override
    protected ArchivePath getClassesPath() {
        return null;
    }

    @Override
    protected ArchivePath getLibraryPath() {
        return null;
    }
}
