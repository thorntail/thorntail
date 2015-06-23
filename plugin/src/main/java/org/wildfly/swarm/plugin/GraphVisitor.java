package org.wildfly.swarm.plugin;

/**
 * @author Bob McWhirter
 */
public interface GraphVisitor {

    void visit(Graph graph);
    void visit(Graph.Module module);
    void visit(Graph.Artifact artifact);
}
