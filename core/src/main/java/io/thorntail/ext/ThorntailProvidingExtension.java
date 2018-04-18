package io.thorntail.ext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import io.thorntail.ServiceRegistry;
import io.thorntail.Thorntail;

/**
 * Created by bob on 1/19/18.
 */
public class ThorntailProvidingExtension implements Extension {

    public ThorntailProvidingExtension(Thorntail system) {
        this.system = system;
    }

    void produce(@Observes AfterBeanDiscovery event) {
        event.addBean()
                .addType(Thorntail.class)
                .scope(ApplicationScoped.class)
                .addQualifier(Default.Literal.INSTANCE)
                .addQualifier(Any.Literal.INSTANCE)
                .produceWith((obj) -> system);

        event.addBean()
                .addType(ServiceRegistry.class)
                .scope(ApplicationScoped.class)
                .addQualifier(Default.Literal.INSTANCE)
                .addQualifier(Any.Literal.INSTANCE)
                .produceWith((obj) -> system.getServiceRegistry());
    }

    private final Thorntail system;
}
