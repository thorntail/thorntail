package org.wildfly.swarm.messaging;

import org.wildfly.swarm.container.AbstractFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class MessagingFractionDefaulter extends AbstractFractionDefaulter<MessagingFraction> {

    public MessagingFractionDefaulter() {
        super(MessagingFraction.class);
    }

    @Override
    public MessagingFraction getDefaultSubsystem() throws Exception {
        MessagingFraction fraction = new MessagingFraction();

        fraction.server(new MessagingServer()
                .enableInVmConnector()
        );

        return fraction;
    }
}
