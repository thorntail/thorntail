package org.eclipse.microprofile.opentracing.wfswarm;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

/**
 * TODO remove
 * fix to pass TCK testException test
 *
 * The correct status code (5xx) is set after SpanFinishing filter.
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
