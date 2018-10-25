/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.swarm.internal.wildfly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.persistence.ExtensibleConfigurationPersister;
import org.jboss.as.server.Bootstrap;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.version.ProductConfig;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.stdio.LoggingOutputStream;
import org.jboss.stdio.NullInputStream;
import org.jboss.stdio.SimpleStdioContextSelector;
import org.jboss.stdio.StdioContext;
import org.wildfly.security.manager.WildFlySecurityManager;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * The main-class entry point for self-contained server instances.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author John Bailey
 * @author Brian Stansberry
 * @author Anil Saldhana
 * @author Bob McWhirter
 */
public final class SelfContainedContainer {

    private static final String PRODUCT_SLOT = "main";

    private Bootstrap.ConfigurationPersisterFactory persisterFactory = null;

    private File tmpDir;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private ServiceContainer serviceContainer;

    public SelfContainedContainer() {
        tmpDir = createTmpDir();
    }

    public SelfContainedContainer(Bootstrap.ConfigurationPersisterFactory configuration) {
        this();
        this.persisterFactory = configuration;
    }

    public ServiceContainer start(final List<ModelNode> containerDefinition) throws ExecutionException, InterruptedException, ModuleLoadException {
        return start(containerDefinition, Collections.emptyList());
    }

    /**
     * The main method.
     *
     * @param containerDefinition The container definition.
     */
    public ServiceContainer start(final List<ModelNode> containerDefinition, Collection<ServiceActivator> additionalActivators) throws ExecutionException, InterruptedException, ModuleLoadException {

        final long startTime = System.currentTimeMillis();


        if (java.util.logging.LogManager.getLogManager().getClass().getName().equals("org.jboss.logmanager.LogManager")) {
            try {
                Class.forName(org.jboss.logmanager.handlers.ConsoleHandler.class.getName(), true, org.jboss.logmanager.handlers.ConsoleHandler.class.getClassLoader());

                // Install JBoss Stdio to avoid any nasty crosstalk, after command line arguments are processed.
                StdioContext.install();
                final StdioContext context = StdioContext.create(
                        new NullInputStream(),
                        new LoggingOutputStream(org.jboss.logmanager.Logger.getLogger("stdout"), org.jboss.logmanager.Level.INFO),
                        new LoggingOutputStream(org.jboss.logmanager.Logger.getLogger("stderr"), org.jboss.logmanager.Level.ERROR)
                );
                StdioContext.setStdioContextSelector(new SimpleStdioContextSelector(context));

            } catch (Throwable ignored) {
            }
        }

        Module.registerURLStreamHandlerFactoryModule(Module.getBootModuleLoader().loadModule("org.jboss.vfs"));
        ServerEnvironment serverEnvironment = determineEnvironment(WildFlySecurityManager.getSystemPropertiesPrivileged(), WildFlySecurityManager.getSystemEnvironmentPrivileged(), ServerEnvironment.LaunchType.SELF_CONTAINED, startTime);
        final Bootstrap bootstrap = Bootstrap.Factory.newInstance();

        final Bootstrap.Configuration configuration = new Bootstrap.Configuration(serverEnvironment);

        configuration.setConfigurationPersisterFactory(
                new Bootstrap.ConfigurationPersisterFactory() {
                    @Override
                    public ExtensibleConfigurationPersister createConfigurationPersister(ServerEnvironment serverEnvironment, ExecutorService executorService) {

                        ExtensibleConfigurationPersister delegate;
                        delegate = persisterFactory.createConfigurationPersister(serverEnvironment, executorService);

                        configuration.getExtensionRegistry().setWriterRegistry(delegate);
                        return delegate;
                    }
                });


        configuration.setModuleLoader(Module.getBootModuleLoader());

        List<ServiceActivator> activators = new ArrayList<>();
        //activators.add(new ContentProviderServiceActivator(contentProvider));
        activators.addAll(additionalActivators);

        serviceContainer = bootstrap.startup(configuration, activators).get();
        return serviceContainer;
    }

    /**
     * Stops the service container and cleans up all file system resources.
     *
     * @throws Exception
     */
    public void stop() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        this.serviceContainer.addTerminateListener(info -> latch.countDown());
        this.serviceContainer.shutdown();

        latch.await();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                TempFileManager.deleteRecursively(tmpDir);
            }
        });
        executor.shutdown();
    }

    public ServerEnvironment determineEnvironment(Properties systemProperties, Map<String, String> systemEnvironment, ServerEnvironment.LaunchType launchType, long startTime) {
        ProductConfig productConfig = ProductConfig.fromKnownSlot(PRODUCT_SLOT, Module.getBootModuleLoader(), systemProperties);
        systemProperties.put(ServerEnvironment.SERVER_TEMP_DIR, tmpDir.getAbsolutePath());
        ServerEnvironment serverEnvironment = new ServerEnvironment(null, systemProperties, systemEnvironment, null, null, launchType, RunningMode.NORMAL, productConfig, startTime, false, null, null, null);
        return serverEnvironment;
    }

    private File createTmpDir() {
        try {
            File tmpDir = TempFileManager.INSTANCE.newTempDirectory("wildfly-self-contained", ".d");
            if (tmpDir.exists()) {
                for (int i = 0; i < 10; ++i) {
                    if (tmpDir.exists()) {
                        if (TempFileManager.deleteRecursively(tmpDir)) {
                            break;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                if (tmpDir.exists()) {
                    //throw ServerLogger.ROOT_LOGGER.unableToCreateSelfContainedDir();
                    throw new RuntimeException("Unable to create directory");
                }
            }
            return tmpDir;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
