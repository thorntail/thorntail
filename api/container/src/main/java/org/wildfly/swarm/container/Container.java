/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package org.wildfly.swarm.container;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;

/**
 * A WildFly-Swarm container.
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class Container {

    private Map<Class<? extends Fraction>, Fraction> fractions = new ConcurrentHashMap<>();
    private List<Fraction> dependentFractions = new ArrayList<>();
    private Set<Class<? extends Fraction>> defaultFractionTypes = new HashSet<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private Map<String, List<SocketBinding>> socketBindings = new HashMap<>();
    private List<Interface> interfaces = new ArrayList<>();

    private Server server;
    private Deployer deployer;
    private Domain domain;

    /**
     * Construct a new, un-started container.
     *
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Container() throws Exception {
        createServer();
        createShrinkWrapDomain();
    }

    private void createShrinkWrapDomain() throws ModuleLoadException {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Container.class.getClassLoader());
            this.domain = ShrinkWrap.getDefaultDomain();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    private void createServer() throws Exception {
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
        }
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.runtime.container"));
        Class<?> serverClass = module.getClassLoader().loadClass("org.wildfly.swarm.runtime.container.RuntimeServer");
        this.server = (Server) serverClass.newInstance();

        Module loggingModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.runtime.logging"));

        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loggingModule.getClassLoader());
            System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
            //force logging init
            Logger.getGlobal();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    public void applyFractionDefaults(Server server) throws Exception {
        Set<Class<? extends Fraction>> availFractions = server.getFractionTypes();

        // Process any dependent fractions from Application added fractions
        if (!this.dependentFractions.isEmpty()) {
            this.dependentFractions.stream().filter(dependentFraction -> this.fractions.get(dependentFraction.getClass()) == null).forEach(this::fraction);
            this.dependentFractions.clear();
        }

        // Provide defaults for those remaining
        availFractions.stream().filter(fractionClass -> this.fractions.get(fractionClass) == null).forEach(fractionClass -> fractionDefault(server.createDefaultFor(fractionClass)));

        // Determine if any dependent fractions should override non Application added fractions
        if (!this.dependentFractions.isEmpty()) {
            this.dependentFractions.stream().filter(dependentFraction -> this.fractions.get(dependentFraction.getClass()) == null || (this.fractions.get(dependentFraction.getClass()) != null && this.defaultFractionTypes.contains(dependentFraction.getClass()))).forEach(this::fraction);
            this.dependentFractions.clear();
        }
    }

    /**
     * Add a fraction to the container.
     *
     * @param fraction The fraction to add.
     * @return The container.
     */
    public Container subsystem(Fraction fraction) {
        return fraction(fraction);
    }

    /**
     * Add a fraction to the container.
     *
     * @param fraction The fraction to add.
     * @return The container.
     */
    public Container fraction(Fraction fraction) {
        this.fractions.put(fractionRoot(fraction.getClass()), fraction);
        fraction.initialize(new InitContext());
        return this;
    }

    public List<Fraction> fractions() {
        return this.fractions.values().stream().collect(Collectors.toList());
    }

    private void fractionDefault(Fraction defaultFraction) {
        this.defaultFractionTypes.add(fractionRoot(defaultFraction.getClass()));
        fraction(defaultFraction);
    }

    private Class<? extends Fraction> fractionRoot(Class<? extends Fraction> fractionClass) {
        Class<? extends Fraction> fractionRoot = fractionClass;
        boolean rootFound = false;

        while (!rootFound) {
            Class<?>[] interfaces = fractionRoot.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (anInterface.getName().equals(Fraction.class.getName())) {
                    rootFound = true;
                    break;
                }
            }

            if (!rootFound) {
                fractionRoot = (Class<? extends Fraction>) fractionRoot.getSuperclass();
            }
        }

        return fractionRoot;
    }

    /**
     * Add a fraction to the container that is a dependency of another fraction.
     *
     * @param fraction The dependent fraction to add.
     */
    private void dependentFraction(Fraction fraction) {
        this.dependentFractions.add(fraction);
    }

    /**
     * Configure a network interface.
     *
     * @param name       The name of the interface.
     * @param expression The expression to define the interface.
     * @return The container.
     */
    public Container iface(String name, String expression) {
        this.interfaces.add(new Interface(name, expression));
        return this;
    }

    public List<Interface> ifaces() {
        return this.interfaces;
    }

    /**
     * Configure a socket-binding-group.
     *
     * @param group The socket-binding group to add.
     * @return The container.
     */
    public Container socketBindingGroup(SocketBindingGroup group) {
        this.socketBindingGroups.add(group);
        return this;
    }

    public List<SocketBindingGroup> socketBindingGroups() {
        return this.socketBindingGroups;
    }

    public SocketBindingGroup getSocketBindingGroup(String name) {
        for (SocketBindingGroup each : this.socketBindingGroups) {
            if (each.name().equals(name)) {
                return each;
            }
        }

        return null;
    }


    public Map<String, List<SocketBinding>> socketBindings() {
        return this.socketBindings;
    }


    void socketBinding(SocketBinding binding) {
        socketBinding("default-sockets", binding);
    }

    void socketBinding(String groupName, SocketBinding binding) {
        List<SocketBinding> list = this.socketBindings.get(groupName);

        if (list == null) {
            list = new ArrayList<>();
            this.socketBindings.put(groupName, list);
        }

        for (SocketBinding each : list) {
            if (each.name().equals(binding.name())) {
                throw new RuntimeException("Socket binding '" + binding.name() + "' already configured for '" + each.portExpression() + "'");
            }
        }

        list.add( binding );
    }

    /**
     * Start the container.
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container start() throws Exception {
        this.deployer = this.server.start(this);
        return this;
    }

    /**
     * Stop the container, undeploying all deployments.
     *
     * @return THe container.
     * @throws Exception If an error occurs.
     */
    public Container stop() throws Exception {
        this.server.stop();
        return this;
    }

    /**
     * Start the container with a deployment.
     * <p/>
     * <p>Effectively calls {@code start().deploy(deployment)}</p>
     *
     * @param deployment The deployment to deploy.
     * @return The container.
     * @throws Exception if an error occurs.
     * @see #start()
     * @see #deploy(Archive)
     */
    public Container start(Archive deployment) throws Exception {
        return start().deploy(deployment);
    }

    /**
     * Deploy the default WAR deployment.
     * <p/>
     * <p>For WAR-based applications, the primary WAR artifact iwll be deployed.</p>
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container deploy() throws Exception {
        return deploy(createDefaultDeployment());
    }

    /**
     * Deploy an archive.
     *
     * @param deployment The ShrinkWrap archive to deploy.
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container deploy(Archive deployment) throws Exception {
        this.deployer.deploy(deployment);
        return this;
    }

    /**
     * Initialization Context to be passed to Fractions to allow them to provide
     * additional functionality into the Container.
     */
    public class InitContext {
        public void fraction(Fraction fraction) {
            Container.this.dependentFraction(fraction);
        }

        public void socketBinding(SocketBinding binding) {
            socketBinding("default-sockets", binding );
        }

        public void socketBinding(String groupName, SocketBinding binding) {
            Container.this.socketBinding(groupName, binding );
        }
    }

    protected Archive createDefaultDeployment() throws Exception {
        Module m1 = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap"));
        ServiceLoader<DefaultDeploymentFactory> providerLoader = m1.loadService(DefaultDeploymentFactory.class);

        Iterator<DefaultDeploymentFactory> providerIter = providerLoader.iterator();

        if (!providerIter.hasNext()) {
            providerLoader = ServiceLoader.load(DefaultDeploymentFactory.class, ClassLoader.getSystemClassLoader());
            providerIter = providerLoader.iterator();
        }

        Map<String, DefaultDeploymentFactory> factories = new HashMap<>();

        while (providerIter.hasNext()) {
            DefaultDeploymentFactory factory = providerIter.next();
            DefaultDeploymentFactory current = factories.get(factory.getType());
            if (current == null) {
                factories.put(factory.getType(), factory);
            } else {
                // if this one is high priority than the previously-seen
                // factory, replace it.
                if (factory.getPriority() > current.getPriority()) {
                    factories.put(factory.getType(), factory);
                }
            }
        }

        DefaultDeploymentFactory factory = factories.get(determineDeploymentType());

        if (factory == null) {
            throw new RuntimeException("Unable to create default deployment");
        }

        return factory.create(this);
    }

    protected String determineDeploymentType() throws IOException {
        String artifact = System.getProperty("wildfly.swarm.app.path");
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        artifact = System.getProperty("wildfly.swarm.app.artifact");
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        if (Files.exists(Paths.get("pom.xml"))) {
            try (BufferedReader in = new BufferedReader(new FileReader(Paths.get("pom.xml").toFile()))) {
                String line = null;

                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("<packaging>jar</packaging>")) {
                        return "jar";
                    } else if (line.equals("<packaging>war</packaging>")) {
                        return "war";
                    }
                }
            }
        }

        if (Files.exists(Paths.get("Mavenfile"))) {
            try (BufferedReader in = new BufferedReader(new FileReader(Paths.get("Mavenfile").toFile()))) {
                String line = null;

                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("packaging :jar")) {
                        return "jar";
                    } else if (line.equals("packaging :war")) {
                        return "war";
                    }
                }
            }
        }

        return "unknown";
    }
}
