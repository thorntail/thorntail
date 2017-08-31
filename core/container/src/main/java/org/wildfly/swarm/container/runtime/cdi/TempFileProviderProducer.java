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
package org.wildfly.swarm.container.runtime.cdi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.vfs.TempFileProvider;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.internal.SwarmMessages;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class TempFileProviderProducer {

    private static final String TEMP_DIR_NAME = "wildfly-swarm";

    private TempFileProvider tempFileProvider = null;

    @PostConstruct
    void init() {
        File serverTmp;
        try {
            serverTmp = TempFileManager.INSTANCE.newTempDirectory(TEMP_DIR_NAME, ".d");
            System.setProperty("jboss.server.temp.dir", serverTmp.getAbsolutePath());

            ScheduledExecutorService tempFileExecutor = Executors.newSingleThreadScheduledExecutor();
            this.tempFileProvider = TempFileProvider.create(TEMP_DIR_NAME, tempFileExecutor, true);

        } catch (IOException e) {
            SwarmMessages.MESSAGES.errorSettingUpTempFileProvider(e);
        }
    }

    @Produces
    @Singleton
    TempFileProvider tempFileProvider() {
        return this.tempFileProvider;
    }

    @PreDestroy
    void close() {
        try {
            this.tempFileProvider.close();
        } catch (IOException e) {
            SwarmMessages.MESSAGES.errorCleaningUpTempFileProvider(e);
        }
    }

}
