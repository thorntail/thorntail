package org.jboss.unimbus.jca.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class RADeployerProducer {

    @Produces
    @ApplicationScoped
    RADeployer deployer() {
        RADeployer deployer = new RADeployer();
        deployer.setConfiguration( this.configuration );
        deployer.setResourceAdapaterRepository( this.resourceAdapterRepository );
        deployer.setMetadataRepository( this.metadataRepository );
        return deployer;
    }


    @Inject
    DeployerConfiguration configuration;

    @Inject
    ResourceAdapterRepository resourceAdapterRepository;

    @Inject
    MetadataRepository metadataRepository;
}
