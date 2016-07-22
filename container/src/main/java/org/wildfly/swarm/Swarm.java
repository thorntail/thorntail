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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.LogManager;

import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.runtime.JBossLoggingManager;
import org.wildfly.swarm.internal.ArtifactManager;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

/**
 * Default {@code main(...)} if an application does not provide one.
 *
 * <p>This simply constructs a default container, starts it and performs
 * a default deployment.  Typically only useful for barren WAR applications.</p>
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Singleton
public class Swarm extends Container {

    public static ArtifactManager ARTIFACT_MANAGER;

    public static String[] COMMAND_LINE_ARGS;

    public Swarm() throws Exception {
    }

    public Swarm(boolean debugBootstrap) throws Exception {
        super(debugBootstrap);
    }


    public Swarm(String...args) throws Exception {
        super( args );
    }

    public Swarm(boolean debugBootstrap, String...args) throws Exception {
        super( debugBootstrap, args );
    }

    public void initiate() throws Exception {
        super.start();
        super.deploy();
    }

    @Override
    @Deprecated
    public Swarm withStageConfig(URL url) {
        return (Swarm) super.withStageConfig(url);
    }

    @Override
    @Deprecated
    public Swarm withXmlConfig(URL url) {
        return (Swarm) super.withXmlConfig(url);
    }

    @Override
    @Deprecated
    public Swarm fraction(Supplier<Fraction> supplier) {
        return (Swarm) super.fraction(supplier);
    }

    @Override
    @Deprecated
    public Swarm fraction(Fraction fraction) {
        return (Swarm) super.fraction(fraction);
    }

    @Override
    @Deprecated
    public Swarm iface(String name, String expression) {
        return (Swarm) super.iface(name, expression);
    }

    @Override
    @Deprecated
    public Swarm socketBindingGroup(SocketBindingGroup group) {
        return (Swarm) super.socketBindingGroup(group);
    }

    @Override
    @Deprecated
    public Swarm start(boolean eagerlyOpen) throws Exception {
        return (Swarm) super.start(eagerlyOpen);
    }

    @Override
    @Deprecated
    public Swarm stop() throws Exception {
        return (Swarm) super.stop();
    }

    @Override
    @Deprecated
    public Swarm start() throws Exception {
        return (Swarm) super.start();
    }

    @Override
    @Deprecated
    public Swarm start(Archive<?> deployment) throws Exception {
        return (Swarm) super.start(deployment);
    }

    @Override
    @Deprecated
    public Swarm deploy() throws DeploymentException {
        return (Swarm) super.deploy();
    }

    @Override
    @Deprecated
    public Swarm deploy(Archive<?> deployment) throws DeploymentException {
        return (Swarm) super.deploy(deployment);
    }

    /**
     * Main entry-point.
     *
     * @param args Ignored.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        COMMAND_LINE_ARGS = args;

        boolean isUberJar = true;

        if (System.getProperty("boot.module.loader") == null) {
            isUberJar = false;
            System.setProperty("boot.module.loader", "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }

        // Need to setup Logging here so that Weld doesn't default to JUL.
        // TODO Is there a better way?

        try {
            Module loggingModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.logging", "runtime"));

            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(loggingModule.getClassLoader());
                System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
                System.setProperty("org.jboss.logmanager.configurator", "org.wildfly.swarm.container.runtime.LoggingConfigurator");
                //force logging init
                LogManager.getLogManager();
                BootstrapLogger.setBackingLoggerManager(new JBossLoggingManager());
            } finally {
                Thread.currentThread().setContextClassLoader(originalCl);
            }
        } catch (ModuleLoadException e) {
            System.err.println("[WARN] logging not available, logging will not be configured");
        }

        Weld weld = new Weld();
        if (isUberJar) {
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));
            weld.setClassLoader(module.getClassLoader());
        }

        try (WeldContainer weldContainer = weld.initialize()) {
            weldContainer.select(Swarm.class).get().initiate();
        }

        //TODO Support user constructed container via annotations for testing and custom use
    }

    public static ArtifactManager artifactManager() throws IOException {
        if (ARTIFACT_MANAGER == null) {
            ARTIFACT_MANAGER = new ArtifactManager();
            ArtifactLookup.INSTANCE.set(ARTIFACT_MANAGER);
        }
        return ARTIFACT_MANAGER;
    }

    public static JavaArchive artifact(String gav) throws Exception {
        return artifactManager().artifact(gav);
    }

    public static JavaArchive artifact(String gav, String asName) throws Exception {
        return artifactManager().artifact(gav, asName);
    }

    public static List<JavaArchive> allArtifacts() throws Exception {
        return artifactManager().allArtifacts();
    }
}
