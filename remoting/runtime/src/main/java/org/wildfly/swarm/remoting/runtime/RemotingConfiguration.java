/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.remoting.runtime;

import org.wildfly.swarm.container.runtime.Configuration;
import org.wildfly.swarm.container.runtime.MarshallingServerConfiguration;
import org.wildfly.swarm.remoting.RemotingFraction;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
@Configuration
public class RemotingConfiguration extends MarshallingServerConfiguration<RemotingFraction> {

    public static final String MODULE_EXTENSION = "org.jboss.as.remoting";

    public RemotingConfiguration() {
        super(RemotingFraction.class, MODULE_EXTENSION);
    }

    @Override
    public RemotingFraction defaultFraction() {
        return RemotingFraction.defaultFraction();
    }

}
