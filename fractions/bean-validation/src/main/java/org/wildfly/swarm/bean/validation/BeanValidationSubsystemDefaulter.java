package org.wildfly.swarm.bean.validation;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class BeanValidationSubsystemDefaulter extends SimpleSubsystemDefaulter<BeanValidationSubsystem> {

    public BeanValidationSubsystemDefaulter() {
        super(BeanValidationSubsystem.class);
    }

}
