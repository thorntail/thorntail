package io.thorntail.jms.artemis.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class ArtemisClientConfiguration {

    public String getUsername() {
        return this.username.orElse(null);
    }

    public String getPassword() {
        return this.password.orElse(null);
    }

    public String getUrl() {
        return this.url.orElse(null);
    }

    public String getHost() {
        return this.host.orElse(null);
    }

    public Integer getPort() {
        return this.port.orElse(null);
    }

    public boolean isHa() {
        return false;
    }

    public boolean hasAuthentication() {
        return (this.username.isPresent());
    }

    @Inject
    @ConfigProperty(name="artemis.username")
    Optional<String> username;

    @Inject
    @ConfigProperty(name="artemis.password")
    Optional<String> password;

    @Inject
    @ConfigProperty(name="artemis.host")
    Optional<String> host;

    @Inject
    @ConfigProperty(name="artemis.port")
    Optional<Integer> port;

    @Inject
    @ConfigProperty(name="artemis.url")
    Optional<String> url;
}
