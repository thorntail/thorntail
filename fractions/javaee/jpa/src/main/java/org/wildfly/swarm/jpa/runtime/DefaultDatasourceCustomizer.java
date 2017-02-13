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
package org.wildfly.swarm.jpa.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.datasources.runtime.DefaultDatasource;
import org.wildfly.swarm.jpa.JPAFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Ken Finnigan
 */
@Post
@ApplicationScoped
public class DefaultDatasourceCustomizer implements Customizer {

    @Inject
    @DefaultDatasource
    Instance<String> defaultDatasourceInstance;

    @Inject
    Instance<JPAFraction> jpaFractionInstance;

    @Override
    public void customize() {
        if (!jpaFractionInstance.isUnsatisfied() && !defaultDatasourceInstance.isUnsatisfied()) {
            jpaFractionInstance.get().defaultDatasource("jboss/datasources/" + defaultDatasourceInstance.get());
        }
    }
}
