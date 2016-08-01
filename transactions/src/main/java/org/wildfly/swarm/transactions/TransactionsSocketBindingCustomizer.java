package org.wildfly.swarm.transactions;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class TransactionsSocketBindingCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    @Inject @Any
    private Instance<TransactionsFraction> fraction;

    @Override
    public void customize() {
        System.err.println( "group: " + group );
        System.err.println( "fraction: " + fraction );
        this.group.socketBinding(new SocketBinding("txn-recovery-environment")
                .port(this.fraction.get().port()));

        this.group.socketBinding(new SocketBinding("txn-status-manager")
                .port(this.fraction.get().statusPort()));
    }
}
