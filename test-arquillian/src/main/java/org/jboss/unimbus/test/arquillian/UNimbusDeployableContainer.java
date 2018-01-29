package org.jboss.unimbus.test.arquillian;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.test.arquillian.util.ClassLoaderUtil;

/**
 * Created by bob on 1/25/18.
 */
public class UNimbusDeployableContainer implements DeployableContainer<UNimbusContainerConfiguration> {

    @Inject
    @DeploymentScoped
    InstanceProducer<BeanManager> beanManagerProducer;

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
        return new ProtocolDescription("Local");
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        ClassLoader cl = ClassLoaderUtil.of(archive);
        ProtocolMetaData meta = new ProtocolMetaData();
        this.system = new UNimbus(cl);
        meta.addContext(this.system);
        system.start();
        this.beanManagerProducer.set(system.getBeanManager());
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
