package org.jboss.unimbus.test.arquillian;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.test.arquillian.util.ClassLoaderUtil;

/**
 * Created by bob on 1/25/18.
 */
public class UNimbusDeployableContainer implements DeployableContainer<UNimbusContainerConfiguration> {

    @Override
    public Class<UNimbusContainerConfiguration> getConfigurationClass() {
        return UNimbusContainerConfiguration.class;
    }

    @Override
    public void setup(UNimbusContainerConfiguration configuration) {
    }

    @Override
    public void start() throws LifecycleException {
    }

    @Override
    public void stop() throws LifecycleException {
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return UNimbusProtocol.DESCRIPTION;
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        ClassLoader cl = ClassLoaderUtil.of(archive);
        ProtocolMetaData meta = new ProtocolMetaData();
        this.system = new UNimbus(cl);
        meta.addContext(this.system);
        system.start();
        return meta;
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        system.stop();

    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
    }

    private UNimbus system;
}
