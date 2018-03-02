package org.jboss.unimbus.tracing;

import javax.interceptor.InvocationContext;

import io.opentracing.ActiveSpan;

/**
 * Tracing interceptor span-starting handler.
 *
 * <p>Implementations should be marked as regular CDI components, typically using {@code ApplicationScoped}.</p>
 *
 * <p>Additionally, a {@code @Priority} should be applied, where priority is
 * evaluated with higher-numbered priorities having preference over lower-numbered priorities.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public abstract class SpanHandler {
    /**
     * Can this handler handle this invocation.
     *
     * @param ctx The invocation context.
     * @return {@code true} if it can handle the invocation, otherwise {@code false}
     */
    public abstract boolean canHandle(InvocationContext ctx);

    /**
     * Start the span for this invocation.
     *
     * <p>This will only be called if the root interceptor has determined that the intercepted method is actually traceable.</p>
     *
     * @param ctx The invocation context.
     * @return The new span, or {@code null} is no span should be created.
     */
    public abstract ActiveSpan handle(InvocationContext ctx);
}
