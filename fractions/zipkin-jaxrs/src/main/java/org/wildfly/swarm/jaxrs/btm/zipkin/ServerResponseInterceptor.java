package org.wildfly.swarm.jaxrs.btm.zipkin;

import java.io.IOException;

import javax.annotation.Priority;
import javax.naming.NamingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.jaxrs2.BraveContainerResponseFilter;
import org.wildfly.swarm.jaxrs.btm.BraveLookup;

/**
 * @author Heiko Braun
 * @since 07/10/16
 */
@Provider
@Priority(0)
public class ServerResponseInterceptor implements ContainerResponseFilter {

    public ServerResponseInterceptor() {
        try {
            this.brave = BraveLookup.lookup().get();
            this.delegate = new BraveContainerResponseFilter(
                    brave.serverResponseInterceptor()
            );
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup brave", e);
        }
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext, final ContainerResponseContext containerResponseContext) throws IOException {

        delegate.filter(containerRequestContext, containerResponseContext);
    }

    private final Brave brave;

    private final BraveContainerResponseFilter delegate;
}
