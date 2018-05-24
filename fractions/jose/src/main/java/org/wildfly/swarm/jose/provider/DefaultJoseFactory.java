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
package org.wildfly.swarm.jose.provider;

import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseConfiguration;

public class DefaultJoseFactory extends JoseFactory {

    @Override
    public Jose getJose(JoseConfiguration config) {
        return new DefaultJoseImpl(config);
    }
}
