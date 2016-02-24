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
package org.wildfly.swarm.spring;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.spring.SpringFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Ken Finnigan
 */
public class SpringConfiguration extends AbstractServerConfiguration<SpringFraction> {
    public SpringConfiguration() {
        super(SpringFraction.class);
    }

    @Override
    public SpringFraction defaultFraction() {
        return new SpringFraction();
    }

    @Override
    public void prepareArchive(Archive<?> archive) {
        if (JARArchive.class.isAssignableFrom(archive.getClass())) {
            // Prevent sun.jdk module from being added to JAR, otherwise wildfly-swarm:run
            // will fail as Spring jars are on system classpath
            archive.as(JARArchive.class).excludeModule("sun.jdk");
        } else if (WARArchive.class.isAssignableFrom(archive.getClass())) {
            // Prevent sun.jdk module from being added to WAR, otherwise wildfly-swarm:run
            // will fail as Spring jars are on system classpath
            archive.as(WARArchive.class).excludeModule("sun.jdk");
        }
    }
}
