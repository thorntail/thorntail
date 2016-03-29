package org.wildfly.swarm.monitor;

import java.util.Optional;

import org.jboss.dmr.ModelNode;

/**
 * @author Heiko Braun
 * @since 23/03/16
 */
public class HealthStatus implements Status {

    private Optional<ModelNode> message = Optional.empty();
    private final State state;

    HealthStatus(State state) {
        this.state = state;
    }

    public static HealthStatus up() {
        return new HealthStatus(State.UP);
    }

    public static HealthStatus down() {
        return new HealthStatus(State.DOWN);
    }

    public HealthStatus withAttribute(String key, String value) {
        ModelNode payload = getPayloadWrapper();
        payload.set(key, value);
        return this;
    }

    public HealthStatus withAttribute(String key, long value) {
            ModelNode payload = getPayloadWrapper();
            payload.set(key, value);
            return this;
        }

    public HealthStatus withAttribute(String key, boolean b) {
        ModelNode payload = getPayloadWrapper();
        payload.set(key, b);
        return this;
    }

    private ModelNode getPayloadWrapper() {
        if(!this.message.isPresent())
            this.message = Optional.of(new ModelNode());
        return this.message.get();
    }

    public Optional<String> getMessage() {
        return message.isPresent() ? Optional.of(getPayloadWrapper().toJSONString(false)) : Optional.empty();
    }

    public State getState() {
        return state;
    }
}
