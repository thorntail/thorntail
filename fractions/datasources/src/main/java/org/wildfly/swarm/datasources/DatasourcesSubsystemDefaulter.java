package org.wildfly.swarm.datasources;

import org.wildfly.swarm.container.AbstractSubsystemDefaulter;
import org.wildfly.swarm.container.SubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class DatasourcesSubsystemDefaulter extends AbstractSubsystemDefaulter<DatasourcesSubsystem> {

    public DatasourcesSubsystemDefaulter() {
        super(DatasourcesSubsystem.class);
    }

    @Override
    public DatasourcesSubsystem getDefaultSubsystem() throws Exception {
        return new DatasourcesSubsystem();
    }
}
