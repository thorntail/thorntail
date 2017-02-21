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
package org.wildfly.swarm.undertow.runtime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.internal.UndertowExternalMountsAsset;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class ContextPathArchivePreparer implements ArchivePreparer {

    @Configurable("swarm.context.path")
    Defaultable<String> contextPath = Defaultable.string("/");

    @Configurable("swarm.context.mounts")
    List<String> mounts;

    @Override
    public void prepareArchive(Archive<?> archive) {
        WARArchive warArchive = archive.as(WARArchive.class);

        if (warArchive.getContextRoot() == null) {
            warArchive.setContextRoot(contextPath.get());
        }

        UndertowExternalMountsAsset ut = null;
        if (mounts != null) {
            for (String mountPath : mounts) {
                Path staticPath = Paths.get(mountPath);
                if (!staticPath.isAbsolute()) {
                    staticPath = Paths.get(System.getProperty("user.dir"), staticPath.toString()).normalize();
                }
                if (ut == null) {
                    ut = new UndertowExternalMountsAsset();
                }
                ut.externalMount(staticPath.toString());
            }
        }

        if (ut != null) {
            warArchive.add(ut, WARArchive.EXTERNAL_MOUNT_PATH);
        }

    }
}
