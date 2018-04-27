package io.thorntail.jca.impl.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jca.core.mdr.SimpleMetadataRepository;
import org.jboss.jca.core.spi.mdr.MetadataRepository;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class MetadataRepositoryProducer {


    @Produces
    @ApplicationScoped
    MetadataRepository repository() {
        return new SimpleMetadataRepository();
    }

}
