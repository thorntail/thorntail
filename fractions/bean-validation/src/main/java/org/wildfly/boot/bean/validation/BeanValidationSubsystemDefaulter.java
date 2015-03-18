package org.wildfly.boot.bean.validation;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class BeanValidationSubsystemDefaulter extends SimpleSubsystemDefaulter<BeanValidationSubsystem> {

    public BeanValidationSubsystemDefaulter() {
        super(BeanValidationSubsystem.class);
    }

}
