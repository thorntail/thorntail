package org.jboss.unimbus.config.impl.interpolation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;

/**
 * Created by bob on 4/2/18.
 */
public class Expression {

    String evaluate(EvaluationContext ctx) {
        if (this.nodes.isEmpty()) {
            return null;
        }

        return this.nodes.stream().map(e -> e.evaluate(ctx)).collect(Collectors.joining());
    }

    void add(ASTNode node) {
        if (node != null) {
            this.nodes.add(node);
        }
    }

    List<ASTNode> getNodes() {
        return this.nodes;
    }

    private List<ASTNode> nodes = new ArrayList<>();

}
