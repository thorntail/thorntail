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
package org.wildfly.swarm.datasources.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.datasources.DatasourcesFraction;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class DatasourceNameProducer {
    @Inject
    @Any
    Instance<DatasourcesFraction> datasourcesFractionInstance;

    @Produces
    @Dependent
    @DefaultDatasource
    public String getDatasourceName() {
        if (!datasourcesFractionInstance.isUnsatisfied()) {
            return datasourcesFractionInstance.get().subresources().dataSources().get(0).getKey();
        }

        return null;
    }
}
