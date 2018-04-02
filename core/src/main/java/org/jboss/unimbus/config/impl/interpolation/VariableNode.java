package org.jboss.unimbus.config.impl.interpolation;

/**
 * Created by bob on 4/2/18.
 */
class VariableNode implements ASTNode {

    VariableNode(String variableName) {
        this(variableName, null);
    }

    VariableNode(String variableName, Expression defaultExpression) {
        this.variableName = variableName;
        this.defaultExpression = defaultExpression;
    }

    @Override
    public String evaluate(EvaluationContext ctx) {
        ctx.markSeen(this.variableName);
        if (this.defaultExpression == null) {
            return ctx.getValue(this.variableName);
        }

        return ctx.getOptionalValue(this.variableName)
                .orElseGet(() -> this.defaultExpression.evaluate(ctx));
    }

    String getVariableName() {
        return this.variableName;
    }

    Expression getDefaultExpression() {
        return this.defaultExpression;

    }

    private final String variableName;

    private final Expression defaultExpression;
}
