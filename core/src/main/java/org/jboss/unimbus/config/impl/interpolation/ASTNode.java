package org.jboss.unimbus.config.impl.interpolation;

import org.eclipse.microprofile.config.Config;

/**
 * Created by bob on 4/2/18.
 */
public interface ASTNode {
    String evaluate(EvaluationContext ctx);
}
