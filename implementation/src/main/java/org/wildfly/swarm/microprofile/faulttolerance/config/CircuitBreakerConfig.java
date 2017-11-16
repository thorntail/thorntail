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
package org.wildfly.swarm.microprofile.faulttolerance.config;

import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.HystrixCommandInterceptor;

/**
 * @author Antoine Sabot-Durand
 */
public class CircuitBreakerConfig extends GenericConfig<CircuitBreaker> {

    public static final String DELAY = "delay";

    public static final String DELAY_UNIT = "delayUnit";

    public static final String FAIL_ON = "failOn";

    public static final String FAILURE_RATIO = "failureRatio";

    public static final String REQUEST_VOLUME_THRESHOLD = "requestVolumeThreshold";

    public static final String SUCCESS_THRESHOLD = "successThreshold";

    public static final String SYNCHRONOUS_STATE_VALIDATION = "synchronousStateValidation";

    private static final Logger LOGGER =  Logger.getLogger(CircuitBreakerConfig.class);

    public CircuitBreakerConfig(CircuitBreaker cb, Method method) {
        super(cb, method);
    }

    public CircuitBreakerConfig(Annotated a) {
        super(a.getAnnotation(CircuitBreaker.class),a);
    }

    @Override
    public void validate() {
        if (get(DELAY, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid CircuitBreaker on " + annotated.toString() + " : delay shouldn't be lower than 0");
        }
        if (get(REQUEST_VOLUME_THRESHOLD, Integer.class) < 1) {
            throw new FaultToleranceDefinitionException(
                    "Invalid CircuitBreaker on " + annotated.toString() + " : requestVolumeThreshold shouldn't be lower than 1");
        }
        if (get(FAILURE_RATIO, Double.class) < 0 || get(FAILURE_RATIO, Double.class) > 1) {
            throw new FaultToleranceDefinitionException("Invalid CircuitBreaker on " + annotated.toString() + " : failureRation should be between 0 and 1");
        }
        int successThreshold = get(SUCCESS_THRESHOLD, Integer.class);
        if (successThreshold < 1) {
            throw new FaultToleranceDefinitionException("Invalid CircuitBreaker on " + annotated.toString() + " : successThreshold shouldn't be lower than 1");
        }
        if (!getConfig().getOptionalValue(HystrixCommandInterceptor.SYNC_CIRCUIT_BREAKER_KEY, Boolean.class).orElse(true) && successThreshold > 1) {
            LOGGER.warnf("Synchronous circuit breaker disabled - successThreshold of value greater than 1 is not supported: " + annotated);
        }
    }

    @Override
    protected String getConfigType() {
        return "CircuitBreaker";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = initKeys();

    private static Map<String, Class<?>> initKeys() {
        Map<String, Class<?>> keys = new HashMap<>();
        keys.put(DELAY, Long.class);
        keys.put(DELAY_UNIT, ChronoUnit.class);
        keys.put(FAIL_ON, Class[].class);
        keys.put(FAILURE_RATIO, Double.class);
        keys.put(REQUEST_VOLUME_THRESHOLD, Integer.class);
        keys.put(SUCCESS_THRESHOLD, Integer.class);
        keys.put(SYNCHRONOUS_STATE_VALIDATION, Boolean.class);
        return Collections.unmodifiableMap(keys);
    }

}
