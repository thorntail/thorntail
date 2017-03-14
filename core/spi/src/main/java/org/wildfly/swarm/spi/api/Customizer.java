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
package org.wildfly.swarm.spi.api;

/** A CDI component which may customize the fraction configuration.
 *
 * <p>Customizers run after all configurations have been applied to
 * the fractions, including XML, YAML and system properties.  They may
 * then perform additional work to further customize the configuration.</p>
 *
 * <p>The execution of customizers is currently divided into two phases,
 * {@link org.wildfly.swarm.spi.runtime.annotations.Pre} and {@link org.wildfly.swarm.spi.runtime.annotations.Post}.
 * All {@code Pre} customizers are executed in arbitrary order, followed immediately
 * by all {@code Post} customizers, also in arbitrary order.</p>
 *
 * <p>Usually a {@code Customizer} may {@link javax.inject.Inject} various other
 * components, such as various {@link Fraction} or {@link SocketBindingGroup} instances.</p>
 *
 * @apiNote Used by {@code Fraction} authors.
 *
 * @author Bob McWhirter
 */
public interface Customizer {

    /** Perform customization.
     */
    void customize() throws Exception;
}
