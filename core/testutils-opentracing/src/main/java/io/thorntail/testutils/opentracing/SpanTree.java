package io.thorntail.testutils.opentracing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;

/**
 * Created by bob on 2/21/18.
 */
public class SpanTree {

    public SpanTree(MockTracer tracer) {
        for (MockSpan each : tracer.finishedSpans()) {
            SpanNode node = new SpanNode(each);
            this.nodes.put( node.spanId(), node );
        }
        for (MockSpan each : tracer.finishedSpans()) {
            SpanNode node = this.nodes.get( each.context().spanId() );
            if ( node.parentId() == 0 ) {
                this.rootNodes.add(node);
            } else {
                SpanNode parent = this.nodes.get(node.parentId());
                parent.add( node );
            }
        }
    }

    public List<SpanNode> getRootNodes() {
        return this.rootNodes;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append( "[\n");
        for (SpanNode rootNode : this.rootNodes) {
            str.append( rootNode.toString( "  " ) );
        }

        str.append( "]\n");

        return str.toString();
    }

    public static SpanTreeAssert assertThat(SpanTree tree) {
        return new SpanTreeAssert(tree);
    }


    private List<SpanNode> rootNodes = new ArrayList<>();
    private Map<Long,SpanNode> nodes = new HashMap<>();

}
