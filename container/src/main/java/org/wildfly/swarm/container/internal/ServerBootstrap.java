package org.wildfly.swarm.container.internal;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.StageConfig;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface ServerBootstrap {
    ServerBootstrap withArguments(String[] args);

    ServerBootstrap withStageConfig(Optional<ProjectStage> stageConfig);

    ServerBootstrap withStageConfigUrl(String stageConfigUrl);

    ServerBootstrap withXmlConfig(Optional<URL> url);

    ServerBootstrap withBootstrapDebug(boolean debugBootstrap);

    ServerBootstrap withExplicitlyInstalledFractions(Collection<Fraction> explicitlyInstalledFractions);

    ServerBootstrap withUserComponents(Set<Class<?>> userComponentClasses);

    Server bootstrap() throws Exception;
}
