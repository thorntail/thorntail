package org.wildfly.swarm.jaxrs.btm.zipkin;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.jaxrs2.BraveClientResponseFilter;

/**
 * @author Heiko Braun
 * @since 07/10/16
 */
@Provider
@Priority(0)
public class ClientResponseInterceptor implements ClientResponseFilter {

    public ClientResponseInterceptor() {
        this.brave = new BraveFactory().create();
        this.delegate = new BraveClientResponseFilter(
                brave.clientResponseInterceptor()
        );
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
        this.delegate.filter(clientRequestContext, clientResponseContext);
    }

    private final Brave brave;

    private final BraveClientResponseFilter delegate;
}
