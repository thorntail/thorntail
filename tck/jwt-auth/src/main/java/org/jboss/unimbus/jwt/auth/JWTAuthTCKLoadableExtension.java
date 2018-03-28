package org.jboss.unimbus.jwt.auth;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Created by bob on 3/27/18.
 */
public class JWTAuthTCKLoadableExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        System.err.println("**** EXTENSION");
        builder.service(ApplicationArchiveProcessor.class, JWTAuthTCKArchiveProcessor.class);
    }
}
