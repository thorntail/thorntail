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
package org.wildfly.swarm.management;

import org.wildfly.swarm.SwarmProperties;
import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.management.HTTPInterfaceManagementInterfaceConsumer;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.SocketBinding;

/**
 * @author Bob McWhirter
 */
public class ManagementFraction extends ManagementCoreService<ManagementFraction> implements Fraction {

    public ManagementFraction() {

    }

    @Override
    public ManagementFraction httpInterfaceManagementInterface() {
        return httpInterfaceManagementInterface( (iface)->{
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManagementFraction httpInterfaceManagementInterface(HTTPInterfaceManagementInterfaceConsumer consumer) {
        return super.httpInterfaceManagementInterface( (iface)->{
            iface.consoleEnabled(false);
            iface.httpUpgradeEnabled(true);
            iface.socketBinding( "management-http" );
            consumer.accept(iface);
        });
    }

    public ManagementFraction securityRealm(String childKey, EnhancedSecurityRealm.Consumer consumer) {
        return securityRealm( ()->{
            EnhancedSecurityRealm realm = new EnhancedSecurityRealm(childKey);
            consumer.accept(realm);
            return realm;
        });
    }

    public static ManagementFraction createDefaultFraction() {
        ManagementFraction fraction = new ManagementFraction();

        fraction.httpInterfaceManagementInterface();

        return fraction;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        initContext.socketBinding(
                new SocketBinding("management-http")
                        .port(SwarmProperties.propertyVar(ManagementProperties.HTTP_PORT, "9990")));
        initContext.socketBinding(
                new SocketBinding("management-https")
                        .port(SwarmProperties.propertyVar(ManagementProperties.HTTPS_PORT, "9993")));
    }

}
