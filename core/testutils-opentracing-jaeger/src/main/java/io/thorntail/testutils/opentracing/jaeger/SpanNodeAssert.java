package io.thorntail.testutils.opentracing.jaeger;

import org.assertj.core.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bob on 2/27/18.
 */
public class SpanNodeAssert {
    public SpanNodeAssert(SpanNode node) {
        this.node = node;
    }

    public SpanNodeAssert hasChildSpans(int expected) {
        Assertions.assertThat(node.getChildren() ).hasSize(expected);
        return this;
    }

    public SpanNodeAssert hasNoChildSpans() {
        Assertions.assertThat(node.getChildren()).isEmpty();
        return this;
    }

    public SpanNodeAssert hasOperationName(String expected) {
        Assertions.assertThat(node.operationName()).isEqualTo(expected);
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
    public SpanNodeAssert hasTag(String key, Integer value) {
        Object tag = node.getTags().get(key);
        assertThat(tag).describedAs("has tag '" + key + "' with value '" + value + "'").isEqualTo(value);
        return this;
    }

    public SpanNodeAssert hasTag(String key, Long value) {
        Object tag = node.getTags().get(key);
        assertThat(tag).describedAs("has tag '" + key + "' with value '" + value + "'").isEqualTo(value);
        return this;
    }


    private final SpanNode node;
}
