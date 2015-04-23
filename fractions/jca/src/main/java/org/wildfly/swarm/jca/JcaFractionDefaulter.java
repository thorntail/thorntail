package org.wildfly.swarm.jca;

import org.wildfly.swarm.container.AbstractFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class JcaFractionDefaulter extends AbstractFractionDefaulter<JcaFraction> {

    public JcaFractionDefaulter() {
        super(JcaFraction.class);
    }

    @Override
    public JcaFraction getDefaultSubsystem() throws Exception {
        return new JcaFraction();
    }
}
