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
package org.wildfly.swarm.mongodb.runtime;

import javax.enterprise.context.ApplicationScoped;

import org.wildfly.swarm.container.util.DriverModuleBuilder;
import org.wildfly.swarm.container.util.Messages;
import org.wildfly.swarm.mongodb.MongoDBFraction;

/**
 * Auto-detection for MongoDB NoSQL driver (based on org.wildfly.swarm.datasources.runtime.DriverInfo, thanks Bob!).
 * <p>
 * <p>mark as a {@link javax.inject.Singleton}, due
 * to the fact that state is retained as to if the driver has or has not
 * been detected.</p>
 *
 * @author Scott Marlow
 *
 */
@ApplicationScoped
public class MongoDBDriverInfo extends DriverModuleBuilder {

    public MongoDBDriverInfo() {
        super("MongoDB", "com.mongodb.MongoClient",
                new String[]{
                        "com.mongodb.MongoClientOptions",
                        "com.mongodb.client.MongoDatabase",
                        "com.mongodb.WriteConcern",
                        "com.mongodb.ReadConcern",
                        "com.mongodb.ReadConcernLevel"
                },
                new String[]{
                        "javax.api",
                        "org.picketbox",
                });
    }

    public boolean detect(MongoDBFraction fraction) {

        if (0 == fraction.subresources().mongos().size()) {
            return false;  // no NoSQL profiles defined
        }

        final String moduleName = (fraction.subresources().mongos().get(0).module() != null ?
                fraction.subresources().mongos().get(0).module() : "org.mongodb.driver");

        // ensure that application only specifies one MongoDB module
        fraction.subresources().mongos().forEach(mongo -> {
            if (mongo.module() != null && !moduleName.equals(mongo.module())) {
                throw Messages.MESSAGES.cannotAddReferenceToModule(mongo.module(), moduleName);
            }
        });
        return super.detect(moduleName);
    }
}

