/**
 * Copyright 2019 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.ejb.mdb;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

@WildFlyExtension(module = "org.wildfly.extension.messaging-activemq")
@MarshalDMR
@DeploymentModule(name = "javax.jms.api")
public class EjbMdbFraction extends MessagingSubsystemStub implements Fraction<EjbMdbFraction> {
    // this class, together with MessagingSubsystemStub, only exists to install
    // an empty and unconfigurable `messaging-activemq` subsystem, which contains
    // some pieces necessary for using MDBs (e.g. injection of JMS classes)
}
