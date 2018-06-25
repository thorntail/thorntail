package io.thorntail.tracing.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.weld.inject.WeldInstance;

import io.opentracing.ActiveSpan;
import io.thorntail.tracing.SpanHandler;
import io.thorntail.util.Annotations;

/**
 * @author Pavol Loffay
 * @author Martin Kouba
 */
@Traced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class TracingInterceptor {

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
        for (SpanHandler handler : spanHandlers()) {
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

    private List<SpanHandler> spanHandlers() {
        List<SpanHandler> handlers = spanHandlers;
        if (handlers == null) {
            synchronized (this) {
                if (spanHandlers == null) {
                    handlers = spanHandlersInstance.handlersStream().sorted(spanHandlersInstance.getPriorityComparator()).map(h -> h.get())
                            .collect(Collectors.toList());
                    spanHandlers = handlers;
                }
            }
        }
        return handlers;
    }

    @Inject
    @Any
    private WeldInstance<SpanHandler> spanHandlersInstance;

    private volatile List<SpanHandler> spanHandlers;
}
