/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    String WELD_INSTANCE_ID = "internal";

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
