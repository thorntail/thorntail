package org.wildfly.swarm.jaxrs;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Bob McWhirter
 */
public class FaviconExceptionMapper implements ExceptionMapper<NotFoundException> {

    private final FaviconHandler handler;

    public FaviconExceptionMapper() {
        this.handler = new FaviconHandler();
    }

    @Override
    public Response toResponse(NotFoundException e) {
        return handler.toResponse(e);
    }
}
