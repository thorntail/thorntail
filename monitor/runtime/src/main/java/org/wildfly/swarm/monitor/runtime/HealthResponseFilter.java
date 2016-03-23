package org.wildfly.swarm.monitor.runtime;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.wildfly.swarm.monitor.Status;

@Provider
public class HealthResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {

        if(resp.hasEntity() && (resp.getEntity() instanceof Status)) {
            Status status = (Status)resp.getEntity();
            int code = (Status.State.UP == status.getState()) ? 200 : 503;
            resp.setStatus(code);
            if(status.getMessage().isPresent())
                resp.setEntity(status.getMessage().get());
            else
                resp.setEntity("{ \"status\": \""+ status.getState().name()+"\"}");
        }
    }

}

