package io.thorntail.servlet.impl;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.thorntail.servlet.ResourceProvider;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
public class ResourceProviders {

    public URL getResource(String path) {
        URL resource = null;

        for (ResourceProvider provider : this.providers) {
            resource = provider.getResource(path);
            if (resource != null) {
                break;
            }
        }

        return resource;
    }


    @Inject
    @Any
    Instance<ResourceProvider> providers;
}
