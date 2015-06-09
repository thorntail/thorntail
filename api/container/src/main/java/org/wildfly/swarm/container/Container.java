/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package org.wildfly.swarm.container;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Configuration;
import org.jboss.shrinkwrap.api.ConfigurationBuilder;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/** A WildFly-Swarm container.
 *
 * @author Bob McWhirter
 */
public class Container {

    private List<Fraction> fractions = new ArrayList<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();

    private Server server;
    private Deployer deployer;
    private Domain domain;

    /** Construct a new, un-started container.
     *
     * @throws Exception If an error occurs performing classloading and initialization magic.
     */
    public Container() throws Exception {
        createServer();
        createShrinkWrapDomain();
    }

    /** Retrieve the ShrinkWrap domain for creating archives.
     *
     * @see #create(String, Class)
     *
     * @return The ShrinkWrap Domain.
     */
    public Domain getShrinkWrapDomain() {
        return this.domain;
    }

    private void createShrinkWrapDomain() throws ModuleLoadException {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.extensionLoader( new ShrinkWrapExtensionLoader() );

        Set<ClassLoader> classLoaders = new HashSet<>();

        classLoaders.add( Module.getBootModuleLoader().loadModule( ModuleIdentifier.create("org.wildfly.swarm.bootstrap" ) ).getClassLoader() );
        classLoaders.add( ClassLoader.getSystemClassLoader() );

        builder.classLoaders(classLoaders);
        Configuration config = builder.build();
        this.domain = ShrinkWrap.createDomain( config );
    }

    private void createServer() throws Exception {
        if ( System.getProperty( "boot.module.loader" ) == null ) {
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

    /** Add a fraction to the container.
     *
     * @param fraction The fraction to add.
     *
     * @return The container.
     */
    public Container subsystem(Fraction fraction) {
        return fraction(fraction);
    }

    /** Add a fraction to the container.
     *
     * @param fraction The fraction to add.
     *
     * @return The container.
     */
    public Container fraction(Fraction fraction) {
        this.fractions.add(fraction);
        return this;
    }

    public List<Fraction> fractions() {
        return this.fractions;
    }

    /** Configure a network interface.
     *
     * @param name The name of the interface.
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

    /** Configure a socket-binding-group.
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

    /** Start the container.
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container start() throws Exception {
        this.deployer = this.server.start( this );
        return this;
    }

    /** Stop the container, undeploying all deployments.
     *
     * @return THe container.
     * @throws Exception If an error occurs.
     */
    public Container stop() throws Exception {
        this.server.stop();
        return this;
    }

    /** Start the container with a deployment.
     *
     * <p>Effectively calls {@code start().deploy(deployment)}</p>
     *
     * @see #start()
     * @see #deploy(Deployment)
     *
     * @param deployment The deployment to deploy.
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container start(Deployment deployment) throws Exception {
        return start().deploy( deployment );
    }

    /** Create a ShrinkWrap archive with a given name and type.
     *
     * @param name The name of the archive.
     * @param type The type of the archive.
     * @param <T> An interface of a ShrinkWrap archive type.
     *
     * @return The newly created archive.
     */
    public <T extends Archive> T create(String name, Class<T> type) {
        T archive = this.domain.getArchiveFactory().create(type, name);
        return archive;
    }

    /** Deploy the default WAR deployment.
     *
     * <p>For WAR-based applications, the primary WAR artifact iwll be deployed.</p>
     *
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container deploy() throws Exception {
        return deploy( new DefaultWarDeployment(this));
    }

    /** Deploy an archive.
     *
     * @param deployment The ShrinkWrap archive to deploy.
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container deploy(Archive deployment) throws Exception {
        this.deployer.deploy(deployment);
        return this;
    }

    /** Deploy a deployment
     *
     * @param deployment The deployment to deploy.
     * @return The container.
     * @throws Exception if an error occurs.
     */
    public Container deploy(Deployment deployment) throws Exception {
        return deploy( deployment.getArchive(true) );
    }

}
