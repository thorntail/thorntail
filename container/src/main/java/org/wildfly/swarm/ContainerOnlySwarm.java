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
package org.wildfly.swarm;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.enterprise.inject.Vetoed;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.container.Container;

/**
 * Default {@code main(...)} if an application does not provide one.
 *
 * <p>This simply constructs a default container, starts it and performs
 * a default deployment.  Typically only useful for barren WAR applications.</p>
 *
 * @author Bob McWhirter
 */
@Vetoed
public class ContainerOnlySwarm {

    /**
     * Main entry-point.
     *
     * @param args Ignored.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }
        Module bootstrap = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));

        ServiceLoader<ContainerFactory> factory = bootstrap.loadService(ContainerFactory.class);
        Iterator<ContainerFactory> factoryIter = factory.iterator();

        if (!factoryIter.hasNext()) {
            simpleMain(args);
        } else {
            factoryMain(factoryIter.next(), args);
        }
    }

    public static void simpleMain(String... args) throws Exception {
        new Container().start();
    }

    public static void factoryMain(ContainerFactory factory, String... args) throws Exception {
        factory.newContainer(args).start();
    }
}
