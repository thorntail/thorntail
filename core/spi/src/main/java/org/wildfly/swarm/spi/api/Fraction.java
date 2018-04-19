/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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

/** Root of a tree of configuration for a particular subset of functionality.
 *
 * <p>While each portion of functionality is called a "fraction", this class
 * maps to the configuration there-of.  It should be implemented to include
 * whatever configuration is appropriate for the subset of functionality.</p>
 *
 * <p>In the event a logical "fraction" requires no configuration, a subclass
 * of this class is <b>not</b> required.</p>
 *
 * <p>For functionality coming in through WildFly, the implementation is
 * a sub-class of the matching WildFly subsystem configuration tree from
 * the {@code wildfly-config-api} project.</p>
 *
 * @link https://github.com/wildfly-swarm/wildfly-config-api
 *
 * @author Bob McWhirter
 */
public interface Fraction<T extends Fraction> {

    /**
     * Apply whatever defaults are required.
     *
     * @implNote The default implementation for this is a no-op.
     *
     * @return this fraction.
     * @see Fraction#applyDefaults(boolean)
     */
    @SuppressWarnings("unchecked")
    default T applyDefaults() {
        return (T) this;
    }

    /**
     * The container always calls this method, but the default implementation delegates to {@link #applyDefaults()}.
     *
     * <p>
     * Unlike {@link #applyDefaults()} this method allows a fraction to customize the defaults depending on whether this fraction was explicitly configured by a
     * user or not.
     * </p>
     *
     * @param hasConfiguration
     * @return this fraction
     */
    default T applyDefaults(boolean hasConfiguration) {
        return applyDefaults();
    }
}
