package org.wildfly.swarm.weld;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractFraction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ken Finnigan
 */
public class WeldJaxRsFraction extends AbstractFraction {

    private List<ModelNode> list = new ArrayList<>();

    public WeldJaxRsFraction() {
    }

    public List<ModelNode> getList() {
        return this.list;
    }

}
