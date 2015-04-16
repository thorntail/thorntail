package org.wildfly.swarm.jaxrs;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class JaxRsSubsystemDefaulter extends SimpleSubsystemDefaulter<JaxRsSubsystem> {

    public JaxRsSubsystemDefaulter() {
        super(JaxRsSubsystem.class);
    }

}
