package org.wildfly.swarm.bean.validation;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class BeanValidationFractionDefaulter extends SimpleFractionDefaulter<BeanValidationFraction> {

    public BeanValidationFractionDefaulter() {
        super(BeanValidationFraction.class);
    }

}
