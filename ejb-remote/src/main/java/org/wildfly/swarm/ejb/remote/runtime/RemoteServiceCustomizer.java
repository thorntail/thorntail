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
package org.wildfly.swarm.ejb.remote.runtime;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.ejb3.RemoteService;
import org.wildfly.swarm.ejb.EJBFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Ken Finnigan
 */
@Singleton
@Post
public class RemoteServiceCustomizer implements Customizer {
    @Inject
    @Any
    Instance<EJBFraction> ejbInstance;

    @Override
    public void customize() {
        if (!ejbInstance.isUnsatisfied()) {
            ejbInstance.get()
                    .remoteService(
                            new RemoteService()
                                    .connectorRef("http-remoting-connector")
                                    .threadPoolName("default")
                    );
        }
    }
}
