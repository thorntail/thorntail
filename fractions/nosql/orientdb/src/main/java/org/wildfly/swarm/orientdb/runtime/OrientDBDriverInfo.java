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
package org.wildfly.swarm.orientdb.runtime;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.container.util.DriverModuleBuilder;
import org.wildfly.swarm.container.util.Messages;
import org.wildfly.swarm.orientdb.OrientDBFraction;

/**
 * Auto-detection for OrientDB NoSQL driver (based on org.wildfly.swarm.datasources.runtime.DriverInfo, thanks Bob!).
 * <p>
 * <p>mark as a {@link javax.inject.Singleton}, due
 * to the fact that state is retained as to if the driver has or has not
 * been detected.</p>
 *
 * @author Scott Marlow
 */
@ApplicationScoped
public class OrientDBDriverInfo extends DriverModuleBuilder {

    public OrientDBDriverInfo() {
        super("OrientDB", "com.orientechnologies.orient.core.db.OPartitionedDatabasePool",
                new String[]{
                        "com.google.common.util.concurrent.AsyncFunction",
                        "com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap",
                        "com.tinkerpop.blueprints.Graph",
                        "com.tinkerpop.blueprints.impls.orient.OrientVertex",
                        "com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx",
                        "com.orientechnologies.orient.client.remote.ORemoteConnectionPool",
                        "com.orientechnologies.orient.object.db.OObjectDatabaseTx",
                        "com.orientechnologies.orient.server.distributed.impl.task.OSyncClusterTask"
                },
                new ModuleIdentifier[]{
                        ModuleIdentifier.create("javax.api"),
                        ModuleIdentifier.create("org.picketbox"),
                        ModuleIdentifier.create("sun.jdk"),
                        ModuleIdentifier.create("org.slf4j"),
                        ModuleIdentifier.create("javax.transaction.api")
                });
    }

    public boolean detect(OrientDBFraction fraction) {
        if (0 == fraction.subresources().orients().size()) {
            return false;  // no NoSQL profiles defined
        }

        final String moduleName = (fraction.subresources().orients().get(0).module() != null ?
                fraction.subresources().orients().get(0).module() : "com.orientechnologies");

        // ensure that application only specifies one module
        fraction.subresources().orients().forEach(orient -> {
            if (orient.module() != null && !moduleName.equals(orient.module())) {
                throw Messages.MESSAGES.cannotAddReferenceToModule(orient.module(), moduleName);
            }
        });
        return super.detect(moduleName);
    }
}

