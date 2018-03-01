package org.jboss.unimbus.testutils.opentracing.jaeger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.transformation.SortedList;

/**
 * Created by bob on 2/21/18.
 */
public class SpanTree {

    public SpanTree(Map<String, ?> data) {

        List<Map<String, ?>> spans = (List<Map<String, ?>>) data.get("spans");

        for (Map<String, ?> each : spans) {
            SpanNode node = new SpanNode(each);
            this.nodes.put(node.spanId(), node);
        }
        for (Map<String, ?> each : spans) {
            SpanNode node = this.nodes.get(each.get("spanID"));
            if (node.parentId() == null) {
                this.rootNodes.add(node);
            } else {
                SpanNode parent = this.nodes.get(node.parentId());
                parent.add(node);
            }
        }

        Collections.sort(this.rootNodes);

        for (SpanNode rootNode : this.rootNodes) {
            rootNode.sort();
        }

    }

    public List<SpanNode> getRootNodes() {
        return this.rootNodes;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("[\n");
        for (SpanNode rootNode : this.rootNodes) {
            str.append(rootNode.toString("  "));
        }

        str.append("]\n");

        return str.toString();
    }

    public static SpanTreeAssert assertThat(SpanTree tree) {
        return new SpanTreeAssert(tree);
    }


    private List<SpanNode> rootNodes = new ArrayList<>();

    private Map<String, SpanNode> nodes = new HashMap<>();

}
