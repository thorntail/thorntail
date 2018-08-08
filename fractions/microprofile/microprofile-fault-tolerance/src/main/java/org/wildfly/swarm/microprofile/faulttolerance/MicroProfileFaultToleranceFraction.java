/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.faulttolerance;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.Module;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule.MetaInfDisposition;

import io.smallrye.faulttolerance.HystrixCommandInterceptor;

/**
 * @author Antoine Sabot-Durand
 */
@DeploymentModule(name = "io.smallrye.faulttolerance", metaInf = MetaInfDisposition.IMPORT, export = true, services = Module.ServiceHandling.IMPORT)
public class MicroProfileFaultToleranceFraction implements Fraction<MicroProfileFaultToleranceFraction> {

    private static final String SYNC_CIRCUIT_BREAKER_KEY = "swarm.microprofile.fault-tolerance.synchronous-circuit-breaker";

    public MicroProfileFaultToleranceFraction() {
        // IMPL NOTE: this is not very nice but works because org.wildfly.swarm.container.config.PropertiesManipulator.SystemPropertiesManipulator (used by
        // default) re-sets all properties found when Thorntail starts and the value is picked up later by the default MP config source
        String synchronousCircuitBreaker = System.getProperty(SYNC_CIRCUIT_BREAKER_KEY);
        if (synchronousCircuitBreaker != null) {
            // Thorntail settings take precedence
            System.setProperty(HystrixCommandInterceptor.SYNC_CIRCUIT_BREAKER_KEY, synchronousCircuitBreaker);
        }
    }

    public boolean isSynchronousCircuitBreakerEnabled() {
        return synchronousCircuitBreaker.get();
    }

    // Keep the attribute for docs generation
    @AttributeDocumentation("Enable/disable synchronous circuit breaker functionality. If disabled, `CircuitBreaker#successThreshold()` of value greater than 1 is not supported. Moreover, circuit breaker does not necessarily transition from `CLOSED` to `OPEN` immediately when a fault tolerance operation completes. However, applications are encouraged to disable this feature on high-volume circuits.")
    @Configurable(SYNC_CIRCUIT_BREAKER_KEY)
    private Defaultable<Boolean> synchronousCircuitBreaker = Defaultable.bool(true);

}
