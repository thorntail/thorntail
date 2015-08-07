package org.wildfly.swarm.transactions;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class TransactionsRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.transactions";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
