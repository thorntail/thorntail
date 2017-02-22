package org.wildfly.swarm.management;

/**
 * @author Bob McWhirter
 */
@FunctionalInterface
public interface InMemoryAuthenticationConsumer {
    void accept(InMemoryAuthentication authn);
}
