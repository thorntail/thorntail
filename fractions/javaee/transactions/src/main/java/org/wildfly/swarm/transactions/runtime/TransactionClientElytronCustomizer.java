package org.wildfly.swarm.transactions.runtime;

import org.wildfly.swarm.config.Elytron;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;

@Post
@ApplicationScoped
public class TransactionClientElytronCustomizer implements Customizer {
    @Inject
    private Instance<Elytron> elytron;

    @Override
    public void customize() {
        if (!this.elytron.isUnsatisfied()) {
            elytron.get()
                    .subresources()
                    .permissionSet("default-permissions")
                    .permission(new HashMap() {{
                        put("class-name", "org.wildfly.transaction.client.RemoteTransactionPermission");
                        put("module", "org.wildfly.transaction.client");
                    }});
        }
    }
}
