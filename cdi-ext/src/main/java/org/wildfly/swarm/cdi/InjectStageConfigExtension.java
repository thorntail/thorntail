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
package org.wildfly.swarm.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.wildfly.swarm.container.runtime.ConfigurationValueProducer;
import org.wildfly.swarm.spi.api.StageConfig;

/**
 * @author Ken Finnigan
 */
public class InjectStageConfigExtension implements Extension {

    private static StageConfig stageConfig;

    public void addStageConfig(@Observes BeforeBeanDiscovery bbd, BeanManager beanManager) {
        bbd.addAnnotatedType(beanManager.createAnnotatedType(ConfigurationValueProducer.class));
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {
        abd.addBean(new StageConfigBean(stageConfig));
    }


}
