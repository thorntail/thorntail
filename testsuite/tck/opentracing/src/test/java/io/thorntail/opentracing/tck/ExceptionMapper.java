package io.thorntail.opentracing.tck;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

/**
 * TODO https://issues.jboss.org/browse/RESTEASY-1758
 *
 * The correct status code is set after {@link io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter}.
 *
 * @author Pavol Loffay
 */
@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(RuntimeException exception) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }
}
