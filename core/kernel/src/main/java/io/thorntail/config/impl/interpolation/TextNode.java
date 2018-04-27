package io.thorntail.config.impl.interpolation;

/**
 * Created by bob on 4/2/18.
 */
class TextNode implements ASTNode {

    TextNode(String text) {
        this.text = text;
    }

    @Override
    public String evaluate(EvaluationContext ctx) {
        return this.text;
    }

    String getText() {
        return this.text;
    }

    private final String text;
}
