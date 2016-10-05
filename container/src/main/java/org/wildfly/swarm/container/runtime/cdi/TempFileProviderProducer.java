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
package org.wildfly.swarm.container.runtime.cdi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.vfs.TempFileProvider;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * @author Bob McWhirter
 */
@Singleton
public class TempFileProviderProducer implements Runnable {

    private TempFileProvider tempFileProvider = null;

    @PostConstruct
    void init() {
        File serverTmp;
        try {
            serverTmp = TempFileManager.INSTANCE.newTempDirectory("wildfly-swarm", ".d");
            System.setProperty("jboss.server.temp.dir", serverTmp.getAbsolutePath());

            ScheduledExecutorService tempFileExecutor = Executors.newSingleThreadScheduledExecutor();
            this.tempFileProvider = TempFileProvider.create("wildfly-swarm", tempFileExecutor, true);

            Runtime.getRuntime().addShutdownHook(new Thread(this));
        } catch (IOException e) {
            //TODO This should be properly logged
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            if (this.tempFileProvider != null) {
                this.tempFileProvider.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Produces
    TempFileProvider tempFileProvider() {
        return this.tempFileProvider;
    }

}
