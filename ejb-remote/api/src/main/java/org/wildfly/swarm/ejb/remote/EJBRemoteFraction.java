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
package org.wildfly.swarm.ejb.remote;

import org.wildfly.swarm.config.ejb3.RemoteService;
import org.wildfly.swarm.ejb.EJBFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Ken Finnigan
 */
public class EJBRemoteFraction extends EJBFraction {

    @Override
    public void initialize(Fraction.InitContext initContext) {
        initContext.fraction(
                createDefaultFraction()
                        .remoteService(
                                new RemoteService()
                                        .connectorRef("http-remoting-connector")
                                        .threadPoolName("default")
                        )
        );
    }
}
