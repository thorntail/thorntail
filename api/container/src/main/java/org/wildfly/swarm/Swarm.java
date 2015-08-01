/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.wildfly.swarm;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.container.Container;

/** Default {@code main(...)} if an application does not provide one.
 *
 * <p>This simply constructs a default container, starts it and performs
 * a default deployment.  Typically only useful for barren WAR applications.</p>
 *
 * @author Bob McWhirter
 */
public class Swarm {

    /** Main entry-point.
     *
     * @param args Ignored.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        Module bootstrap = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap"));

        ServiceLoader<ContainerFactory> factory = bootstrap.loadService(ContainerFactory.class);
        Iterator<ContainerFactory> factoryIter = factory.iterator();

        if ( ! factoryIter.hasNext() ) {
            simpleMain( args );
        } else {
            factoryMain( factoryIter.next(), args );
        }
    }

    public static void simpleMain(String...args) throws Exception {
        Container container = new Container().start();
        container.deploy();
    }

    public static void factoryMain(ContainerFactory factory, String...args) throws Exception {
        Container container = factory.newContainer(args).start();
        container.deploy();
    }

    public static JavaArchive artifact(String gav) throws Exception {
        return ArtifactManager.artifact( gav );
    }

    public static List<JavaArchive> allArtifacts() throws Exception {
        return ArtifactManager.allArtifacts();
    }
}
