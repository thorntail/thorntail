/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.discovery;

import org.wildfly.swarm.config.Discovery;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

@WildFlyExtension(module = "org.wildfly.extension.discovery")
@MarshalDMR
public class DiscoveryFraction extends Discovery<DiscoveryFraction> implements Fraction<DiscoveryFraction> {
}
