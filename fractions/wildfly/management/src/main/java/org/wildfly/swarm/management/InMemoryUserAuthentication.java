package org.wildfly.swarm.management;

import org.wildfly.swarm.spi.api.annotations.Configurable;

/**
 * Created by bob on 5/22/17.
 */
@Configurable
public class InMemoryUserAuthentication {
    public InMemoryUserAuthentication() {
    }

    public InMemoryUserAuthentication password(String password) {
        this.password = password;
        return this;
    }

    public String password() {
        return this.password;
    }

    public InMemoryUserAuthentication hash(String hash) {
        this.hash = hash;
        return this;
    }

    public String hash() {
        return this.hash;
    }

    private String password;

    private String hash;
}
