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
package org.wildfly.swarm.topology.consul.runtime;

import java.net.MalformedURLException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.topology.consul.ConsulTopologyFraction;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class ConsulURLCustomizer implements Customizer {

    @Inject
    @Any
    ConsulTopologyFraction fraction;

    @Inject
    @ConfigurationValue("thorntail.consul.url")
    private String consulUrl;


    @Override
    public void customize() throws MalformedURLException {

        if (this.consulUrl != null) {
            this.fraction.url(this.consulUrl);
        }

    }
}
