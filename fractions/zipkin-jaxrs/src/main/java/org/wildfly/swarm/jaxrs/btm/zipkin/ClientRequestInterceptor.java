package org.wildfly.swarm.jaxrs.btm.zipkin;

/**
 * @author Heiko Braun
 * @since 07/10/16
 */

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.jaxrs2.BraveClientRequestFilter;

@Provider
@Priority(0)
public class ClientRequestInterceptor implements ClientRequestFilter {

    public ClientRequestInterceptor() {
        this.brave = new BraveFactory().create();
        this.delegate = new BraveClientRequestFilter(
                new DefaultSpanNameProvider(),
                brave.clientRequestInterceptor()
        );
    }

    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        this.delegate.filter(clientRequestContext);
    }

    private final Brave brave;

    private final BraveClientRequestFilter delegate;
}