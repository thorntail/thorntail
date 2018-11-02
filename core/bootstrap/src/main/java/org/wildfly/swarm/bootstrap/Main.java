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
package org.wildfly.swarm.bootstrap;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import __redirected.__JAXPRedirected;
import org.jboss.modules.Module;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.BootstrapUtil;

/**
 * @author Bob McWhirter
 */
public class Main {

    public static final String MAIN_PROCESS_FILE = "org.wildfly.swarm.mainProcessFile";

    private static WatchService watcher;

    private static ExecutorService shutdownService;

    private static MainInvoker mainInvoker;

    public Main(String... args) throws Throwable {
        this.args = args;
    }

    public static void main(String... args) throws Throwable {
        try {
            Performance.start();
            BootstrapUtil.convertSwarmSystemPropertiesToThorntail();
            //TODO Move property key to -spi
            System.setProperty(BootstrapProperties.IS_UBERJAR, Boolean.TRUE.toString());

            String processFile = System.getProperty(MAIN_PROCESS_FILE);

            if (processFile != null) {
                shutdownService = Executors.newSingleThreadExecutor();
                shutdownService.submit(() -> {
                        File uuidFile = new File(processFile);
                        try {
                            File watchedDir = uuidFile.getParentFile();
                            register(watchedDir);
                            processEvents(watchedDir, uuidFile.toPath());
                            if (mainInvoker != null) {
                                mainInvoker.stop();
                            }
                            //Exit gracefully
                            System.exit(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                );
            }

            new Main(args).run();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    private static void register(File directory) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        directory.toPath().register(watcher, ENTRY_DELETE);
    }

    @SuppressWarnings("unchecked")
    private static void processEvents(File watchedDir, Path file) {
        for (;;) {

            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                Kind<?> kind = event.kind();

                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path name = ev.context();
                Path child = watchedDir.toPath().resolve(name);

                if (kind == ENTRY_DELETE && child.equals(file)) {
                    return;
                }
            }

            boolean valid = key.reset();
            if (!valid) {
               break;
            }
        }
    }

    public void run() throws Throwable {
        setupBootModuleLoader();

        __JAXPRedirected.changeAll("swarm.container", Module.getBootModuleLoader());
        mainInvoker = new MainInvoker(ApplicationEnvironment.get().getMainClassName(), this.args);
        mainInvoker.invoke();
    }

    public void setupBootModuleLoader() {
        System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
    }

    private final String[] args;
}
