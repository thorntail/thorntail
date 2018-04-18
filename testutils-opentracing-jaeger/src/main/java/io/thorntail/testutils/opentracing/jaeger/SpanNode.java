package io.thorntail.testutils.opentracing.jaeger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by bob on 2/21/18.
 */
public class SpanNode implements Comparable<SpanNode> {

    SpanNode(Map<String, ?> span) {
        this.span = span;
    }

    public String parentId() {
        Optional<Map<String, ?>> parentRef = ((List<Map<String, ?>>) this.span.get("references")).stream()
                .filter(e -> e.get("refType").equals("CHILD_OF"))
                .findFirst();

        if ( ! parentRef.isPresent() ) {
            return null;
        }
        return (String) parentRef.get().get("spanID");
    }

    public long startTime() {
        return (Long) this.span.get("startTime");
    }

    public long duration() {
        return (Long) this.span.get( "duration");
    }

    public String spanId() {
        return (String) this.span.get("spanID");
    }

    public String operationName() {
        return (String) this.span.get("operationName");
    }

    public Map<String, Object> getTags() {
        return ((List<Map<String,?>>)this.span.get("tags")).stream()
                .collect(Collectors.toMap( e-> (String) e.get("key"), e-> e.get("value")));
    }

    public void add(SpanNode node) {
        this.children.add(node);
    }

    public String toString() {
        return toString("");
    }

    public String toString(String indent) {
        StringBuilder str = new StringBuilder();

        str.append(indent).append("[ spanId: " + spanId() + ", operation:" + operationName() + ", tags: " + getTags());
        if (!this.children.isEmpty()) {
            str.append("\n");
            str.append(indent).append("  children: [\n");
            for (SpanNode child : this.children) {
                str.append(child.toString(indent + "  "));
            }
            str.append(indent).append("  ]\n");
            str.append(indent).append("]\n");
        } else {
            str.append("]\n");

        }

        return str.toString();
    }

    public List<SpanNode> getChildren() {
        return this.children;
    }

    public static SpanNodeAssert assertThat(SpanNode node) {
        return new SpanNodeAssert(node);
    }

    @Override
    public int compareTo(SpanNode o) {
        return Long.compare(this.startTime(), o.startTime());
    }

    public void sort() {
        Collections.sort(this.children);
        for (SpanNode child : this.children) {
            child.sort();
        }
    }


    private final Map<String, ?> span;

    private List<SpanNode> children = new ArrayList<>();

}
