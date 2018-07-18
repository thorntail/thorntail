package io.thorntail.tracing.impl;

import io.opentracing.Scope;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import io.opentracing.Tracer;
import org.eclipse.microprofile.opentracing.Traced;
import io.thorntail.tracing.SpanHandler;

/**
 * Created by bob on 3/2/18.
 */
@ApplicationScoped
@Priority(Integer.MIN_VALUE)
public class DefaultSpanHandler extends SpanHandler {
    @Override
    public boolean canHandle(InvocationContext ctx) {
        return true;
    }

    @Override
    public Scope handle(InvocationContext context) {
        return this.tracer.buildSpan(getOperationName(context.getMethod())).startActive(true);
    }

    protected String getOperationName(Method method) {
        Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
        Traced methodTraced = method.getAnnotation(Traced.class);
        if (methodTraced != null && methodTraced.operationName().length() > 0) {
            return methodTraced.operationName();
        } else if (classTraced != null && classTraced.operationName().length() > 0) {
            return classTraced.operationName();
        }
        return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
    }

    @Inject
    Tracer tracer;
}
