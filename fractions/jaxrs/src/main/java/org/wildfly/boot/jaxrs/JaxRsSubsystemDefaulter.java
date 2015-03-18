package org.wildfly.boot.jaxrs;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;
import org.wildfly.boot.jaxrs.JaxRsSubsystem;

/**
 * @author Bob McWhirter
 */
public class JaxRsSubsystemDefaulter extends SimpleSubsystemDefaulter<JaxRsSubsystem> {

    public JaxRsSubsystemDefaulter() {
        super(JaxRsSubsystem.class);
    }

}
