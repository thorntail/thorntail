package io.thorntail.tracing;

import io.opentracing.Scope;
import javax.interceptor.InvocationContext;


/**
 * Simplified one-argument {@link SpanHandler} abstract class.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public abstract class OneArgSpanHandler<P1> extends SpanHandler {

    public Scope handle(InvocationContext ctx) {
        Object[] parameters = ctx.getParameters();
        if ( parameters.length != 1 ) {
            throw new RuntimeException("Too many parameters for " + this.getClass().getName());
        }

        P1 param = (P1) parameters[0];
        return handle(param);
    }

    public abstract Scope handle(P1 param);
}
