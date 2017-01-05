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
package org.wildfly.swarm.bootstrap;

import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;

/**
 * @author Bob McWhirter
 */
public class Main {

    public Main(String... args) throws Throwable {
        this.args = args;
    }

    public static void main(String... args) throws Throwable {
        try {
            //TODO Move property key to -spi
            System.setProperty(BootstrapProperties.IS_UBERJAR, Boolean.TRUE.toString());
            new Main(args).run();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public void run() throws Throwable {
        setupBootModuleLoader();
        new MainInvoker(ApplicationEnvironment.get().getMainClassName(), this.args).invoke();
    }

    public void setupBootModuleLoader() {
        System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
    }

    private final String[] args;
}
