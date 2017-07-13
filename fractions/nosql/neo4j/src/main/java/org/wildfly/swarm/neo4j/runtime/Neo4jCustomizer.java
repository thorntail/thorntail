/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.neo4j.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.neo4j.Neo4jFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.container.util.Messages;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * Neo4jCustomizer
 *
 * @author Scott Marlow
 */
@Pre
@ApplicationScoped
public class Neo4jCustomizer implements Customizer {

    @Inject
    @Any
    Instance<Neo4jFraction> allDrivers;

    @Override
    public void customize() throws Exception {
        customizeDrivers();
    }

    protected void customizeDrivers() {
            this.allDrivers.forEach(this::attemptInstallation);
        }

    protected void attemptInstallation(Neo4jFraction info) {
        Neo4jDriverInfo neo4jDBDriverInfo = new Neo4jDriverInfo();
        if (neo4jDBDriverInfo.detect(info)) {
            Messages.MESSAGES.autodetectedDriver(neo4jDBDriverInfo.name());
        }
    }
}
