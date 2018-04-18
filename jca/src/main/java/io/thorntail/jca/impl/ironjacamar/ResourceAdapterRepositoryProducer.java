package io.thorntail.jca.impl.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jca.core.rar.SimpleResourceAdapterRepository;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class ResourceAdapterRepositoryProducer {

    @Produces
    @ApplicationScoped
    ResourceAdapterRepository repository() {
        SimpleResourceAdapterRepository repository = new SimpleResourceAdapterRepository();
        repository.setMetadataRepository(this.metadataRepository);
        return repository;
    }

    @Inject
    MetadataRepository metadataRepository;
}
