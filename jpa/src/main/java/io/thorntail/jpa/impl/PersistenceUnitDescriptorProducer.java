package io.thorntail.jpa.impl;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

@ApplicationScoped
public class PersistenceUnitDescriptorProducer {

    @Produces
    @ApplicationScoped
    PersistenceUnitDescriptor persistenceUnitDescriptor() {
        URL url = getClass().getClassLoader().getResource("META-INF/persistence.xml");
        return PersistenceXmlParser.locateIndividualPersistenceUnit(url);
    }

}
