package io.thorntail.testutils.opentracing;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 2/27/18.
 */
public class SpanNodeAssert {
    public SpanNodeAssert(SpanNode node) {
        this.node = node;
    }

    public SpanNodeAssert hasChildSpans(int expected) {
        assertThat( node.getChildren() ).hasSize(expected);
        return this;
    }

    public SpanNodeAssert hasNoChildSpans() {
        assertThat(node.getChildren()).isEmpty();
        return this;
    }

    public SpanNodeAssert hasOperationName(String expected) {
        assertThat( node.operationName()).isEqualTo(expected);
        return this;
    }

    public SpanNodeAssert hasTag(String key) {
        Object tag = node.getTags().get(key);
        assertThat(tag).describedAs("has tag '" + key + "'").isNotNull();
        return this;
    }

    public SpanNodeAssert hasTag(String key, String value) {
        Object tag = node.getTags().get(key);
        assertThat(tag).describedAs("has tag '" + key + "' with value '" + value + "'").isEqualTo(value);
        return this;
    }


    private final SpanNode node;
}
