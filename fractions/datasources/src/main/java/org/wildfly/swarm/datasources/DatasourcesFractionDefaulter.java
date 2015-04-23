package org.wildfly.swarm.datasources;

import org.wildfly.swarm.container.AbstractFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class DatasourcesFractionDefaulter extends AbstractFractionDefaulter<DatasourcesFraction> {

    public DatasourcesFractionDefaulter() {
        super(DatasourcesFraction.class);
    }

    @Override
    public DatasourcesFraction getDefaultSubsystem() throws Exception {
        return new DatasourcesFraction();
    }
}
