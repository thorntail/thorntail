package io.thorntail.config.impl.interpolation;

import org.fest.assertions.Assertions;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by bob on 4/2/18.
 */
public class ExpressionParserTest {

    @Test
    public void testVariableNode() {
        VariableNode node = new ExpressionParser("${my.var}").variableNode();
        Assertions.assertThat(node).isNotNull();
        Assertions.assertThat(node.getVariableName()).isEqualTo("my.var");

        Assertions.assertThat(node.getDefaultExpression()).isNull();
    }

    @Test
    public void testVariableNodeWithLiteralDefault() {
        VariableNode node = new ExpressionParser("${my.var:42}").variableNode();
        Assertions.assertThat(node).isNotNull();
        Assertions.assertThat(node.getVariableName()).isEqualTo("my.var");

        Expression defaultExpr = node.getDefaultExpression();
        assertThat(defaultExpr).isNotNull();

        assertThat(defaultExpr.getNodes()).hasSize(1);
        assertThat(defaultExpr.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) defaultExpr.getNodes().get(0)).getText()).isEqualTo("42");
    }

    @Test
    public void testVariableNodeWithVariableDefault() {
        VariableNode node = new ExpressionParser("${my.var:${foo.bar}}").variableNode();
        Assertions.assertThat(node).isNotNull();
        Assertions.assertThat(node.getVariableName()).isEqualTo("my.var");

        Expression defaultExpr = node.getDefaultExpression();
        assertThat(defaultExpr).isNotNull();

        assertThat(defaultExpr.getNodes()).hasSize(1);
        assertThat(defaultExpr.getNodes().get(0)).isInstanceOf(VariableNode.class);
        Assertions.assertThat(((VariableNode) defaultExpr.getNodes().get(0)).getVariableName()).isEqualTo("foo.bar");
    }

    @Test
    public void testVariableNodeWithVariableDefaultWithLiteralDefault() {
        VariableNode node = new ExpressionParser("${my.var:tacos${my.other.var:42}}").variableNode();
        Assertions.assertThat(node).isNotNull();
        Assertions.assertThat(node.getVariableName()).isEqualTo("my.var");

        Expression defaultExpr1 = node.getDefaultExpression();
        assertThat(defaultExpr1).isNotNull();

        assertThat(defaultExpr1.getNodes()).hasSize(2);
        assertThat(defaultExpr1.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) defaultExpr1.getNodes().get(0)).getText()).isEqualTo("tacos");
        assertThat(defaultExpr1.getNodes().get(1)).isInstanceOf(VariableNode.class);
        Assertions.assertThat(((VariableNode) defaultExpr1.getNodes().get(1)).getVariableName()).isEqualTo("my.other.var");

        Expression defaultExpr2 = ((VariableNode) defaultExpr1.getNodes().get(1)).getDefaultExpression();
        assertThat(defaultExpr2.getNodes()).hasSize(1);
        assertThat(defaultExpr2.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode)defaultExpr2.getNodes().get(0)).getText()).isEqualTo("42");
    }

    @Test
    public void testUnclosedVariableNode() {
        try {
            new ExpressionParser("${my.var").variableNode();
            fail("should have thrown");
        } catch (IllegalArgumentException e) {
            // expected and correct
        }
    }

    @Test
    public void testUnfollowedEscape() {
        try {
            new ExpressionParser("hi\\").textNode();
            fail("should have thrown");
        } catch (IllegalArgumentException e) {
            // expected and correct;
        }
    }

    @Test
    public void testEscapedEscape() {
        TextNode node = new ExpressionParser("hi\\\\").textNode();
        assertThat(node.getText()).isEqualTo("hi\\\\");
    }

    @Test
    public void testEmpty() {
        Expression expr = parse("");
        assertThat(expr.getNodes()).hasSize(0);
    }

    @Test
    public void testTextOnly() {
        Expression expr = parse("hello");
        assertThat(expr.getNodes()).hasSize(1);
        assertThat(expr.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(0)).getText()).isEqualTo("hello");
    }

    @Test
    public void testVariableOnly() {
        Expression expr = parse("${my.var}");
        assertThat(expr.getNodes()).hasSize(1);
        assertThat(expr.getNodes().get(0)).isInstanceOf(VariableNode.class);
        Assertions.assertThat(((VariableNode) expr.getNodes().get(0)).getVariableName()).isEqualTo("my.var");
    }

    @Test
    public void testTextWithEscapedDollarSign() {
        Expression expr = parse("\\${my.var}");
        assertThat(expr.getNodes()).hasSize(1);
        assertThat(expr.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(0)).getText()).isEqualTo("${my.var}");
    }

    @Test
    public void testTextWithEscapedComma() {
        Expression expr = parse("foo\\,bar");
        assertThat(expr.getNodes()).hasSize(1);
        assertThat(expr.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(0)).getText()).isEqualTo("foo\\,bar");
    }

    @Test
    public void testMixed1() {
        Expression expr = parse("foo${my.var}bar");
        assertThat(expr.getNodes()).hasSize(3);
        assertThat(expr.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(0)).getText()).isEqualTo("foo");

        assertThat(expr.getNodes().get(1)).isInstanceOf(VariableNode.class);
        Assertions.assertThat(((VariableNode) expr.getNodes().get(1)).getVariableName()).isEqualTo("my.var");

        assertThat(expr.getNodes().get(2)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(2)).getText()).isEqualTo("bar");
    }

    @Test
    public void testMixed2() {
        Expression expr = parse("foo${my.var}");
        assertThat(expr.getNodes()).hasSize(2);
        assertThat(expr.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(0)).getText()).isEqualTo("foo");

        assertThat(expr.getNodes().get(1)).isInstanceOf(VariableNode.class);
        Assertions.assertThat(((VariableNode) expr.getNodes().get(1)).getVariableName()).isEqualTo("my.var");
    }

    @Test
    public void testMixed3() {
        Expression expr = parse("${my.var}bar");
        assertThat(expr.getNodes()).hasSize(2);

        assertThat(expr.getNodes().get(0)).isInstanceOf(VariableNode.class);
        Assertions.assertThat(((VariableNode) expr.getNodes().get(0)).getVariableName()).isEqualTo("my.var");

        assertThat(expr.getNodes().get(1)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(1)).getText()).isEqualTo("bar");
    }

    @Test
    public void testRandomDollarSigns() {
        Expression expr = parse("%4$-");
        assertThat( expr.getNodes()).hasSize(1);
        assertThat( expr.getNodes().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) expr.getNodes().get(0)).getText()).isEqualTo("%4$-");


    }

    protected Expression parse(String str) {
        return ExpressionParser.parse(str);
    }

}
