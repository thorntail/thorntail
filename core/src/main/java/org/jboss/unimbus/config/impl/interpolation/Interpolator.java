package org.jboss.unimbus.config.impl.interpolation;

import org.eclipse.microprofile.config.Config;

/**
 * Created by bob on 4/2/18.
 */
public class Interpolator {

    public Interpolator(Config config) {
        this.config = config;
    }

    public String interpolate(String str) {
        EvaluationContext ctx = CTX.get();

        if ( ctx == null ) {
            ctx = new EvaluationContext(this, str);
            CTX.set(ctx);
        }

        try {
            return interpolate(str, ctx);
        } finally {
            if ( ctx.uses() == 0 ) {
                CTX.remove();
            }
        }
    }

    private String interpolate(String str, EvaluationContext ctx) {
        ctx.incr();
        try {
            if (str == null) {
                return null;
            }
            if (str.indexOf('$') < 0) {
                return str;
            }
            return ExpressionParser.parse(str).evaluate(ctx);
        } finally {
            ctx.decr();
        }
    }

    final Config config;

    final static ThreadLocal<EvaluationContext> CTX = new ThreadLocal<>();
}
