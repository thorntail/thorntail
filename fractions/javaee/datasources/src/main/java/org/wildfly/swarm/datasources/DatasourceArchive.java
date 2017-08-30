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
package org.wildfly.swarm.datasources;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Assignable;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.DataSourceConsumer;

/** A simplistic archive to allow deployment-based creation of datasources.
 *
 * <p>While not the recommended method for deploying datasources, this archive
 * type helps with the creation of WildFly-specific {@code -ds.xml} files
 * within an archive.  It can be driver by exactly the same API for creating
 * datasources through {@link DatasourcesFraction} configuration</p>
 *
 * @see DatasourcesFraction
 *
 * @author Bob McWhirter
 */
public interface DatasourceArchive extends Assignable, Archive<DatasourceArchive> {

    /** Create an configure a datasource.
     *
     * @param key The key of the datasource.
     * @param consumer The configuring consumer.
     * @return This archive.
     */
    DatasourceArchive dataSource(String key, DataSourceConsumer consumer);

    /** Create a configured datasource
     *
     * @param ds The completely configured datasource.
     * @return This archive.
     */
    DatasourceArchive dataSource(DataSource ds);

}
