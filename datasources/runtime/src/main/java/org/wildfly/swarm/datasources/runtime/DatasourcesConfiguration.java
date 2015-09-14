package org.wildfly.swarm.datasources.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.datasources.DatasourcesFraction;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class DatasourcesConfiguration extends AbstractServerConfiguration<DatasourcesFraction> {

    public DatasourcesConfiguration() {
        super(DatasourcesFraction.class);
    }

    @Override
    public DatasourcesFraction defaultFraction() {
        return new DatasourcesFraction();
    }

    @Override
    public List<ModelNode> getList(DatasourcesFraction fraction) {

        List<ModelNode> list = new ArrayList<>();

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure datasources subsystem");
        }
        return list;
    }

}
