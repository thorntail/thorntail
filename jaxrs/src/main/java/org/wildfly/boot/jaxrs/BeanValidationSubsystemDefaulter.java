package org.wildfly.boot.jaxrs;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class BeanValidationSubsystemDefaulter extends SimpleSubsystemDefaulter<BeanValidationSubsystem> {

    public BeanValidationSubsystemDefaulter() {
        super(BeanValidationSubsystem.class);
    }

}
