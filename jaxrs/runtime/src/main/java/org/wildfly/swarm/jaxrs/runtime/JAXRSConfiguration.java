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
package org.wildfly.swarm.jaxrs.runtime;

import org.wildfly.swarm.container.runtime.MarshallingServerConfiguration;
import org.wildfly.swarm.jaxrs.JAXRSFraction;

/**
 * @author Bob McWhirter
 */
public class JAXRSConfiguration extends MarshallingServerConfiguration<JAXRSFraction> {

    public static final String EXTENSION_MODULE = "org.jboss.as.jaxrs";

    public JAXRSConfiguration() {
        super(JAXRSFraction.class, EXTENSION_MODULE);
    }
}
