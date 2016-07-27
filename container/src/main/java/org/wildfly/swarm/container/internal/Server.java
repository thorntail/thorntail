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
import java.util.Set;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.ProjectStage;

/**
 * @author Bob McWhirter
 */
public interface Server {

    Deployer start(boolean eagerlyOpen) throws Exception;

    void stop() throws Exception;

    void setXmlConfig(URL xmlConfig);

    void setStageConfig(ProjectStage stageConfig);

    Set<Class<? extends Fraction>> getFractionTypes();

    Fraction createDefaultFor(Class<? extends Fraction> fractionClazz);

}
