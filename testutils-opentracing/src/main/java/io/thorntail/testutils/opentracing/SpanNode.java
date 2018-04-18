package io.thorntail.testutils.opentracing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.opentracing.mock.MockSpan;

/**
 * Created by bob on 2/21/18.
 */
public class SpanNode {

    SpanNode(MockSpan span) {
        this.span = span;
    }

    public long parentId() {
        return this.span.parentId();
    }
    public long spanId() {
        return this.span.context().spanId();
    }

    public String operationName() {
        return this.span.operationName();
    }

    public Map<String, Object> getTags() {
        return this.span.tags();
    }

    public void add(SpanNode node) {
        this.children.add(node);
    }

    public String toString(String indent) {
        StringBuilder str = new StringBuilder();

        str.append(indent).append( "[ spanId: " + spanId() + ", operation:" + this.span.operationName() + ", tags: " + this.span.tags() );
        if ( ! this.children.isEmpty() ) {
            str.append( "\n");
            str.append(indent).append("  children: [\n");
            for (SpanNode child : this.children) {
                str.append(child.toString(indent + "  "));
            }
            str.append(indent).append( "  ]\n");
            str.append(indent).append( "]\n");
        } else {
            str.append( "]\n");

        }

        return str.toString();
    }

    public List<SpanNode> getChildren() {
        return this.children;
    }

    public static SpanNodeAssert assertThat(SpanNode node) {
        return new SpanNodeAssert(node);
    }


    private final MockSpan span;

    private List<SpanNode> children = new ArrayList<>();

    private boolean childNodes;
}
