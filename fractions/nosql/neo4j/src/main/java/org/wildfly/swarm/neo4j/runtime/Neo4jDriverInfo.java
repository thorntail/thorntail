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

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.neo4j.Neo4jFraction;
import org.wildfly.swarm.container.util.Messages;
import org.wildfly.swarm.container.util.DriverModuleBuilder;

/**
 * Auto-detection for Neo4j NoSQL driver (based on org.wildfly.swarm.datasources.runtime.DriverInfo, thanks Bob!).
 * <p>
 * <p>mark as a {@link javax.inject.Singleton}, due
 * to the fact that state is retained as to if the driver has or has not
 * been detected.</p>
 *
 * @author Scott Marlow
 */
@ApplicationScoped
public class Neo4jDriverInfo extends DriverModuleBuilder {

    public Neo4jDriverInfo() {
        super("Neo4j", "org.neo4j.driver.v1.Driver",
                new String[]{
                        "org.neo4j.driver.v1.GraphDatabase",
                        "org.wildfly.extension.nosql.cdi.Neo4jExtension",
                        "org.neo4j.driver.v1.AuthTokens",
                        "org.neo4j.driver.v1.AuthToken"
                },
                new ModuleIdentifier[]{
                        ModuleIdentifier.create("javax.api"),
                        ModuleIdentifier.create("org.picketbox"),
                });
    }

    public boolean detect(Neo4jFraction fraction) {
        if (0 == fraction.subresources().neo4js().size()) {
            return false;  // no NoSQL profiles defined
        }

        final String moduleName = (fraction.subresources().neo4js().get(0).module() != null ?
                fraction.subresources().neo4js().get(0).module() : "org.neo4j.driver");

        // ensure that application only specifies one module
        fraction.subresources().neo4js().forEach(neo4j -> {
            if (neo4j.module() != null && !moduleName.equals(neo4j.module())) {
                throw Messages.MESSAGES.cannotAddReferenceToModule(neo4j.module(), moduleName);
            }
        });

        return super.detect(moduleName);
    }
}

