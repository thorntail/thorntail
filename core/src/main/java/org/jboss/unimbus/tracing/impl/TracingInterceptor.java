package org.jboss.unimbus.tracing.impl;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.unimbus.tracing.SpanHandler;
import org.jboss.unimbus.util.Annotations;
import org.jboss.unimbus.util.Priorities;

/**
 * @author Pavol Loffay
 */
@Traced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class TracingInterceptor {


    List<SpanHandler> sortedHandlers() {
        return Priorities.highToLow(this.handlers);
    }

    @AroundInvoke
    public Object interceptTraced(InvocationContext ctx) throws Exception {
        ActiveSpan span = spanFor(ctx);
        try {
            return ctx.proceed();
        } finally {
            if (span != null) {
                span.deactivate();
            }
        }
    }

    protected ActiveSpan spanFor(InvocationContext ctx) {
        if (!isTraced(ctx.getTarget(), ctx.getMethod())) {
            return null;

        }
        SpanHandler handler = spanHandlerFor(ctx);
        if (handler == null) {
            return null;
        }

        return handler.handle(ctx);
    }

    protected SpanHandler spanHandlerFor(InvocationContext ctx) {
        for (SpanHandler handler : sortedHandlers()) {
            if (handler.canHandle(ctx)) {
                return handler;
            }
        }

        return null;
    }

    /**
     * Determines whether invoked method should be traced or not
     *
     * @param method invoked method
     * @return true if {@link Traced} defined on method or class has value true
     */
    protected boolean isTraced(Object target, Method method) {
        Traced classTraced = Annotations.getAnnotation(target, Traced.class);
        Traced methodTraced = method.getAnnotation(Traced.class);

        if (methodTraced != null) {
            return methodTraced.value();
        }
        return classTraced.value();
    }

    @Inject
    @Any
    Instance<SpanHandler> handlers;

    @Inject
    private Tracer tracer;
}
