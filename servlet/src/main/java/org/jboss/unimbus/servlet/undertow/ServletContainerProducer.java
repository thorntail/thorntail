package org.jboss.unimbus.servlet.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.ServletContainer;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class ServletContainerProducer {

    @Produces
    @ApplicationScoped
    ServletContainer servletContainer() {
        return ServletContainer.Factory.newInstance();
    }
}
