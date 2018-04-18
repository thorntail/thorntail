package io.thorntail.servlet.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.thorntail.servlet.ResourceProvider;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
public class ResourceProviderProducer {

    @Produces
    @ApplicationScoped
    ResourceProvider staticDirProvider() {
        return new ClasspathResourceProvider("/static");
    }

    @Produces
    @ApplicationScoped
    ResourceProvider publicDirProvider() {
        return new ClasspathResourceProvider("/public");
    }

    @Produces
    @ApplicationScoped
    ResourceProvider resourcesDirProvider() {
        return new ClasspathResourceProvider("/META-INF/resources");
    }
}
