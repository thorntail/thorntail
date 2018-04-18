package io.thorntail.test.arquillian.impl;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.impl.enricher.resource.URIResourceProvider;
import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * Created by bob on 1/25/18.
 */
public class ThorntailLoadableExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(DeployableContainer.class, ThorntailDeployableContainer.class);

        extensionBuilder.override(ResourceProvider.class,
                                  URIResourceProvider.class,
                                  ThorntailURIResourceProvider.class);
        extensionBuilder.override(ResourceProvider.class,
                                  URLResourceProvider.class,
                                  ThorntailURLResourceProvider.class);
        extensionBuilder.observer(ContextLifecycleHandler.class);
    }
}
