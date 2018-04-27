package io.thorntail.testutils.opentracing;

import org.fest.assertions.Assertions;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by bob on 2/27/18.
 */
public class SpanTreeAssert {

    public SpanTreeAssert(SpanTree tree) {
        this.tree = tree;
    }

    public SpanTreeAssert hasRootSpans(int number) {
        Assertions.assertThat(tree.getRootNodes()).describedAs("root spans").hasSize(number);
        return this;
    }

    private final SpanTree tree;
}
