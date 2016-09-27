package org.wildfly.swarm.container.internal;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.wildfly.swarm.internal.OutboundSocketBindingRequest;
import org.wildfly.swarm.internal.SocketBindingRequest;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.ProjectStage;

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

    ServerBootstrap withSocketBindings(List<SocketBindingRequest> bindings);

    ServerBootstrap withOutboundSocketBindings(List<OutboundSocketBindingRequest> bindings);

    Server bootstrap() throws Exception;
}
