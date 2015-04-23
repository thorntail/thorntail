package org.wildfly.swarm.jca;

import org.wildfly.swarm.container.AbstractSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class JcaSubsystemDefaulter extends AbstractSubsystemDefaulter<JcaSubsystem> {

    public JcaSubsystemDefaulter() {
        super(JcaSubsystem.class);
    }

    @Override
    public JcaSubsystem getDefaultSubsystem() throws Exception {
        return new JcaSubsystem();
    }
}
