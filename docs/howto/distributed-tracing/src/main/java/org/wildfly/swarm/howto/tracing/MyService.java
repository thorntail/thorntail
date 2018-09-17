package org.wildfly.swarm.howto.tracing;

import org.eclipse.microprofile.opentracing.ClientTracingRegistrar;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@ApplicationScoped
public class MyService {
    // tag::traced-service-method[]
    @Traced
    public String call() {
        // tag::client-registration[]
        Client client = ClientTracingRegistrar.configure(ClientBuilder.newBuilder()).build();
        // end::client-registration[]
        try {
            String response = client.target("http://localhost:8080")
                    .path("/simple")
                    .request()
                    .get(String.class);
            return "Called an external service successfully, it responded: " + response;
        } finally {
            client.close();
        }
    }
    // end::traced-service-method[]
}
