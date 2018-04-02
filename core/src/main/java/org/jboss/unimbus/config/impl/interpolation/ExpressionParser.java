package org.jboss.unimbus.config.impl.interpolation;

/**
 * Created by bob on 4/2/18.
 */
class ExpressionParser {

    static Expression parse(String str) {
        return new ExpressionParser(str).expression(false);
    }

    ExpressionParser(String str) {
        this.chars = str.toCharArray();
        this.cur = -1;
    }

    Expression expression(boolean isSubExpression) {
        char c = 0;

        StringBuilder text = new StringBuilder();
        Expression expr = new Expression();

        LOOP:
        while ((c = la()) > 0) {
            switch (c) {
                case '$':
                    if (la(2) == '{') {
                        expr.add(variableNode());
                    } else {
                        expr.add(textNode());
                    }
                    break;
                case '}':
                    if ( isSubExpression ) {
                        break LOOP;
                    }
                default:
                    expr.add(textNode(isSubExpression));
                    break;
            }
        }

        return expr;
    }

    TextNode textNode() {
        return textNode(false);
    }

    TextNode textNode(boolean isSubExpression) {
        char c = 0;

        StringBuilder text = new StringBuilder();

        LOOP:
        while ((c = la()) > 0) {
            switch (c) {
                case '$':
                    if (la(2) == '{') {
                        break LOOP;
                    }
                    text.append(consume(c));
                    break;
                case '\\':
                    consume('\\');
                    char next = consume();
                    if (next == 0) {
                        throw new IllegalArgumentException("Escape found as final character");
                    }
                    if (next != '$') {
                        text.append('\\');
                    }
                    text.append(next);
                    break;
                case '}':
                    if ( isSubExpression ) {
                        break LOOP;
                    }
                default:
                    text.append(consume(c));
                    break;
            }
        }

        if (text.length() > 0) {
            return new TextNode(text.toString());
        }

        return null;
    }

    VariableNode variableNode() {
        consume("${");

        char c = 0;

        StringBuilder varName = new StringBuilder();
        Expression defaultExpr = null;

        LOOP:
        while ((c = la()) >= 0) {
            switch (c) {
                case 0:
                    throw new IllegalArgumentException("Missing '}' " + varName);
                case '}':
                    consume('}');
                    break LOOP;
                case ':':
                    consume(':');
                    defaultExpr = expression(true);
                    break;
                default:
                    varName.append(consume(c));
                    break;
            }
        }

        return new VariableNode(varName.toString(), defaultExpr);
    }

    char la() {
        return la(1);
    }

    char la(int n) {
        if (this.cur + n >= this.chars.length) {
            return (char) 0;
        }

        return this.chars[this.cur + n];
    }

    void consume(String expected) {
        consume(expected.toCharArray());
    }

    void consume(char... expected) {
        for (char c : expected) {
            consume(c);
        }
    }

    char consume(char expected) {
        char c = consume();
        if (c != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' but got '" + c + "' at " + this.cur + " of '" + new String(this.chars) + "'");
        }

        return c;
    }

    char consume() {
        if (this.cur + 1 >= this.chars.length) {
            return (char) 0;
        }

        ++this.cur;

        return this.chars[this.cur];
    }

    private final char[] chars;

    private int cur;

}
