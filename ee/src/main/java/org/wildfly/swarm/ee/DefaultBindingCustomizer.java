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
package org.wildfly.swarm.ee;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.config.MessagingActiveMQ;
import org.wildfly.swarm.config.ee.DefaultBindingsService;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class DefaultBindingCustomizer implements Customizer {

    @Inject
    @Any
    private Instance<MessagingActiveMQ> messaging;

    @Inject @Any
    private EEFraction fraction;

    @Override
    public void customize() {
        if (!this.messaging.isUnsatisfied()) {
            if (this.fraction.subresources().defaultBindingsService() == null) {
                this.fraction.defaultBindingsService(new DefaultBindingsService());
            }
            if (this.fraction.subresources().defaultBindingsService().jmsConnectionFactory() == null) {
                this.fraction.subresources().defaultBindingsService()
                        .jmsConnectionFactory("java:jboss/DefaultJMSConnectionFactory");
            }
        }
    }
}
