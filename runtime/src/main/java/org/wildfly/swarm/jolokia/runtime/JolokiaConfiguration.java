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
package org.wildfly.swarm.jolokia.runtime;

import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jolokia.JolokiaFraction;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
public class JolokiaConfiguration extends AbstractServerConfiguration<JolokiaFraction> {

    public JolokiaConfiguration() {
        super(JolokiaFraction.class);

        deployment("org.jolokia:jolokia-war:war:*")
                .as("jolokia.war")
                .configure((fraction, archive) -> {
                    archive.as(WARArchive.class).setContextRoot(fraction.context());
                });
    }

    @Override
    public JolokiaFraction defaultFraction() {
        return new JolokiaFraction();
    }
}
