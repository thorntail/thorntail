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
package org.wildfly.swarm.jose.runtime;

import javax.enterprise.inject.Vetoed;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseFraction;
import org.wildfly.swarm.jose.JoseLookup;

/**
 *
 */
@Vetoed
public class JoseService implements JoseLookup, Service<JoseService> {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "jose");

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.jose");

    public JoseService(JoseFraction joseInstance) {
        this.jose = joseInstance;
    }

    @Override
    public Jose get() {
        return this.jose.getJoseInstance();
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        LOG.info("Jose Service started: " + this.jose);
    }

    @Override
    public void stop(StopContext stopContext) {
        if (this.jose != null) {
            LOG.info("Shutdown Jose Service");
        }
    }

    @Override
    public JoseService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }


    private JoseFraction jose;
}

