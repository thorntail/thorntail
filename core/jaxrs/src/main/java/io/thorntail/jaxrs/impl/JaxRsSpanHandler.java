package io.thorntail.jaxrs.impl;

import io.opentracing.Scope;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;

import io.thorntail.tracing.SpanHandler;

import static io.thorntail.util.Annotations.hasAnnotation;

/**
 * Created by bob on 3/2/18.
 */
@ApplicationScoped
public class JaxRsSpanHandler extends SpanHandler {

    private static final String JAXRS_PATH_ANNOTATION_NAME = "javax.ws.rs.Path";

    @Override
    public boolean canHandle(InvocationContext ctx) {
        return hasAnnotation(ctx.getMethod(), JAXRS_PATH_ANNOTATION_NAME) || hasAnnotation(ctx.getMethod().getDeclaringClass(), JAXRS_PATH_ANNOTATION_NAME);
    }

    @Override
    public Scope handle(InvocationContext ctx) {
        // defer to the other server features.
        return null;
    }
}
