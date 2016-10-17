package org.wildfly.swarm.jaxrs.btm.zipkin;

import java.io.IOException;

import javax.annotation.Priority;
import javax.naming.NamingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.jaxrs2.BraveContainerRequestFilter;
import org.wildfly.swarm.jaxrs.btm.BraveLookup;

/**
 * @author Heiko Braun
 * @since 07/10/16
 */
@Provider
@PreMatching
@Priority(0)
public class ServerRequestInterceptor implements ContainerRequestFilter {

    public ServerRequestInterceptor() {
        try {
            this.brave = BraveLookup.lookup().get();
            this.delegate = new BraveContainerRequestFilter(
                    brave.serverRequestInterceptor(),
                    new DefaultSpanNameProvider()
            );
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup brave", e);
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        delegate.filter(containerRequestContext);
    }

    private final Brave brave;

    private final BraveContainerRequestFilter delegate;
}
