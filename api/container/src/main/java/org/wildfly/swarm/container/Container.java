package org.wildfly.swarm.container;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Configuration;
import org.jboss.shrinkwrap.api.ConfigurationBuilder;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.MemoryMapArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class Container {

    private List<Fraction> fractions = new ArrayList<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();

    private Server server;
    private Deployer deployer;
    private Domain domain;

    public Container() throws Exception {
        createServer();
        createShrinkWrapDomain();
    }

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

    public Container subsystem(Fraction fraction) {
        return fraction(fraction);
    }

    public Container fraction(Fraction fraction) {
        this.fractions.add(fraction);
        return this;
    }

    public List<Fraction> fractions() {
        return this.fractions;
    }

    public Container iface(String name, String expression) {
        this.interfaces.add(new Interface(name, expression));
        return this;
    }

    public List<Interface> ifaces() {
        return this.interfaces;
    }

    public Container socketBindingGroup(SocketBindingGroup group) {
        this.socketBindingGroups.add(group);
        return this;
    }

    public List<SocketBindingGroup> socketBindingGroups() {
        return this.socketBindingGroups;
    }

    public Container start() throws Exception {
        this.deployer = this.server.start( this );
        return this;
    }

    public Container start(Deployment deployment) throws Exception {
        return start().deploy( deployment );
    }

    public <T extends Archive> T create(String name, Class<T> type) {
        T archive = this.domain.getArchiveFactory().create(type, name);
        return archive;
    }

    public Container deploy() throws Exception {
        return deploy( new DefaultWarDeployment(create("app.war", WebArchive.class)) );
    }

    public Container deploy(Archive deployment) throws Exception {
        this.deployer.deploy(deployment);
        return this;
    }

    public Container deploy(Deployment deployment) throws Exception {
        return deploy( deployment.getArchive() );

    }



}
