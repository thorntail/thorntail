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
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.internal.UndertowExternalMountsAsset;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class ContextPathArchivePreparer implements ArchivePreparer {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(ContextPathArchivePreparer.class.getName());

    @Configurable("swarm.context.path")
    Defaultable<String> contextPath = Defaultable.string("/");

    @Inject
    ConfigView configView;

    @Override
    public void prepareArchive(Archive<?> archive) {
        WARArchive warArchive = archive.as(WARArchive.class);

        if (warArchive.getContextRoot() == null) {
            warArchive.setContextRoot(contextPath.get());
        }

        UndertowExternalMountsAsset ut = null;
        ConfigKey parentKey = ConfigKey.parse("swarm.context.mount");
        if (configView != null && configView.hasKeyOrSubkeys(parentKey)) {
            for (SimpleKey mount : configView.simpleSubkeys(parentKey)) {
                String mountPath = configView.resolve(parentKey.append(mount)).getValue();
                Path staticPath = Paths.get(mountPath);
                if (!staticPath.isAbsolute()) {
                    staticPath = Paths.get(System.getProperty("user.dir"), staticPath.toString()).normalize();
                }
                log.info("External mounting directory " + staticPath);
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
