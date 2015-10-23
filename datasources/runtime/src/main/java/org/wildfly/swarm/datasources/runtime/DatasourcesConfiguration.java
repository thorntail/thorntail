package org.wildfly.swarm.datasources.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
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
    public List<ModelNode> getList(DatasourcesFraction fraction) throws Exception {

        List<ModelNode> list = new ArrayList<>();

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }

}
