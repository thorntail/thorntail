package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.MemoryMapArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class Container {

    private List<Fraction> fractions = new ArrayList<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();

    private Server server;
    private Deployer deployer;

    public Container() throws Exception {
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        createServer();
    }

    private void createServer() throws Exception {
        if ( System.getProperty( "boot.module.loader" ) == null ) {
            System.setProperty("boot.module.loader", BootModuleLoader.class.getName());
        }
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.runtime.container"));
        Class<?> serverClass = module.getClassLoader().loadClass("org.wildfly.swarm.runtime.container.RuntimeServer");
        this.server = (Server) serverClass.newInstance();
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

    public <T extends Archive> T create(String name, Class<T> type) {
        if ( type.equals(WebArchive.class) ) {
            return (T) new WebArchiveImpl( new MemoryMapArchiveImpl( name, ShrinkWrap.getDefaultDomain().getConfiguration()) );
        }

        return null;
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
