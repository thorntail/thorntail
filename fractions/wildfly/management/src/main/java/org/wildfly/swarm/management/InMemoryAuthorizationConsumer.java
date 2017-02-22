package org.wildfly.swarm.management;

/**
 * @author Bob McWhirter
 */
@FunctionalInterface
public interface InMemoryAuthorizationConsumer {
    void accept(InMemoryAuthorization authz);
}
