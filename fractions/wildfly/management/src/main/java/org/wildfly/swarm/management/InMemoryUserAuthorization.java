package org.wildfly.swarm.management;

import java.util.ArrayList;
import java.util.List;

import org.wildfly.swarm.spi.api.annotations.Configurable;

/**
 * Created by bob on 5/22/17.
 */
@Configurable
public class InMemoryUserAuthorization {
    public InMemoryUserAuthorization() {
    }

    public InMemoryUserAuthorization roles(List<String> roles) {
        this.roles.addAll(roles);
        return this;
    }

    public List<String> roles() {
        return this.roles;
    }

    private List<String> roles = new ArrayList<>();
}
