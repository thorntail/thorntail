/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.wildfly.swarm;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.container.Container;

import java.io.IOException;
import java.util.List;

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
        Container container = new Container().start();
        container.deploy();
    }

    public static JavaArchive artifact(String gav) throws Exception {
        return ArtifactManager.artifact( gav );
    }

    public static List<JavaArchive> allArtifacts() throws Exception {
        return ArtifactManager.allArtifacts();
    }
}
