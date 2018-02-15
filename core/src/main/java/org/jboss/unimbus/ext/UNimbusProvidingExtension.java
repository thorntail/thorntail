package org.jboss.unimbus.ext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.unimbus.UNimbus;

/**
 * Created by bob on 1/19/18.
 */
public class UNimbusProvidingExtension implements Extension {

    public UNimbusProvidingExtension(UNimbus system) {
        this.system = system;
    }

    void produce(@Observes AfterBeanDiscovery event) {
        event.addBean()
                .addType(UNimbus.class)
                .scope(ApplicationScoped.class)
                .addQualifier(Default.Literal.INSTANCE)
                .addQualifier(Any.Literal.INSTANCE)
                .produceWith((obj) -> system);
    }

    private final UNimbus system;
}
