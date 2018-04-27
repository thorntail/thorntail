package io.thorntail.config.impl.interpolation;

/**
 * Created by bob on 4/2/18.
 */
public interface ASTNode {
    String evaluate(EvaluationContext ctx);
}
