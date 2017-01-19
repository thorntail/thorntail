package org.wildfly.swarm.container.test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 */
@Pre
@Singleton
public class ConfigViewInjectable implements Customizer {

    @Inject
    private Instance<ConfigView> configView;

    @Override
    public void customize() {
        if (this.configView.isUnsatisfied()) {
            throw new AssertionError("project stages not present");
        }
    }
}
