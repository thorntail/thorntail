/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.datasources.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.config.EE;
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
    String defaultDatasourceJndiName;

    @Inject
    Instance<EE> eeInstance;

    @Override
    public void customize() {
        if (!eeInstance.isUnsatisfied() && defaultDatasourceJndiName != null) {
            eeInstance.get().subresources().defaultBindingsService()
                    .datasource(defaultDatasourceJndiName);
        }
    }
}
