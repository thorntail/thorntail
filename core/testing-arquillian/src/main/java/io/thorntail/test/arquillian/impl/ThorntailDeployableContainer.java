package io.thorntail.test.arquillian.impl;

import javax.enterprise.inject.spi.BeanManager;

import io.thorntail.Thorntail;
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
import io.thorntail.test.arquillian.impl.util.ClassLoaderUtil;
import org.jboss.weld.manager.api.WeldManager;

/**
 * Created by bob on 1/25/18.
 */
public class ThorntailDeployableContainer implements DeployableContainer<ThorntailContainerConfiguration> {

    @Inject
    @DeploymentScoped
    private InstanceProducer<BeanManager> beanManagerProducer;

    @Inject
    @DeploymentScoped
    private InstanceProducer<WeldManager> weldManagerProducer;

    @Override
    public Class<ThorntailContainerConfiguration> getConfigurationClass() {
        return ThorntailContainerConfiguration.class;
    }

    @Override
    public void setup(ThorntailContainerConfiguration configuration) {
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
        try {
            ClassLoader cl = ClassLoaderUtil.of(archive);
            this.system = new Thorntail(cl);
            this.system.start();
            this.beanManagerProducer.set(this.system.getBeanManager());
            this.weldManagerProducer.set((WeldManager) this.system.getBeanManager());

            return new ProtocolMetaData();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
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

    private Thorntail system;
}
