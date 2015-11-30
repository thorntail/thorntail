/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.datasources;

import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.DataSourceConsumer;

/**
 * @author Bob McWhirter
 */
public class DatasourceArchiveImpl extends AssignableBase<ArchiveBase<?>> implements DatasourceArchive {
    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public DatasourceArchiveImpl(ArchiveBase<?> archive) {
        super(archive);
    }


    public DatasourceArchive dataSource(String key, DataSourceConsumer consumer) {
        DataSource ds = new DataSource( key );
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

        getArchive().add( new DSXmlAsset( ds ), "META-INF/" + name );

        return this;
    }
}
