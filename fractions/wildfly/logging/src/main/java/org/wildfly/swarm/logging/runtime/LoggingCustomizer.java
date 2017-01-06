package org.wildfly.swarm.logging.runtime;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.bootstrap.logging.InitialLoggerManager;
import org.wildfly.swarm.bootstrap.logging.LevelNode;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@Singleton
public class LoggingCustomizer implements Customizer {

    @Inject
    @Any
    private LoggingFraction fraction;

    @Override
    public void customize() {
        LevelNode root = InitialLoggerManager.INSTANCE.getRoot();
        apply(root);
    }

    private void apply(LevelNode node) {
        if (!node.getName().equals("")) {
            this.fraction.logger(node.getName(), (l) -> {
                l.level(Level.valueOf(node.getLevel().toString()));
            });
        }
        for (LevelNode each : node.getChildren()) {
            apply(each);
        }
    }
}
