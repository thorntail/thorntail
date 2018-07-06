package io.thorntail.config.impl.interpolation;

import java.io.Serializable;

import org.eclipse.microprofile.config.Config;

/**
 * Created by bob on 4/2/18.
 */
public class Interpolator implements Serializable {

    public Interpolator(Config config) {
        this.config = config;
    }

    public String interpolate(String str) {
        EvaluationContext ctx = CTX.get();

        if (ctx == null) {
            try {
                ctx = new EvaluationContext(this);
                CTX.set(ctx);
                return interpolate(str, ctx);
            } finally {
                CTX.remove();
            }
        } else {
            return interpolate(str, ctx);
        }

    }

    private String interpolate(String str, EvaluationContext ctx) {
        if (str == null) {
            return null;
        }
        if (str.indexOf('$') < 0) {
            return str;
        }
        return ExpressionParser.parse(str).evaluate(ctx);
    }

    final Config config;

    final static ThreadLocal<EvaluationContext> CTX = new ThreadLocal<>();
}
