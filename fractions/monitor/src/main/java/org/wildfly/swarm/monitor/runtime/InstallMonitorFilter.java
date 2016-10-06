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
package org.wildfly.swarm.monitor.runtime;

import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Ken Finnigan
 */
@Singleton
public class InstallMonitorFilter implements ArchivePreparer {

    //private static String HEALTH_RESPONSE_FILTER_CLASS_NAME = "org.wildfly.swarm.monitor.runtime.HealthResponseFilter";

    /**
     * Path to the WEB-INF inside of the Archive.
     */
    private static final ArchivePath PATH_WEB_INF = ArchivePaths.create("WEB-INF");

    /**
     * Path to the classes inside of the Archive.
     */
    private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_WEB_INF, "classes");

    @Override
    public void prepareArchive(Archive<?> archive) {
        JARArchive jarArchive = archive.as(JARArchive.class);

        Asset resource = new ClassAsset(HealthResponseFilter.class);
        ArchivePath location = new BasicPath(PATH_CLASSES, AssetUtil.getFullPathForClassResource(HealthResponseFilter.class));
        jarArchive.add(resource, location);
    }
}
