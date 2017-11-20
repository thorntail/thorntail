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

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class RetryConfig extends GenericConfig<Retry> {

    public static final String MAX_RETRIES = "maxRetries";

    public static final String DELAY = "delay";

    public static final String DELAY_UNIT = "delayUnit";

    public static final String MAX_DURATION = "maxDuration";

    public static final String DURATION_UNIT = "durationUnit";

    public static final String JITTER = "jitter";

    public static final String JITTER_DELAY_UNIT = "jitterDelayUnit";

    public static final String RETRY_ON = "retryOn";

    public static final String ABORT_ON = "abortOn";

    public RetryConfig(Method method) {
        super(Retry.class, method);
    }

    public RetryConfig(AnnotatedMethod<?> annotatedMethod) {
        super(Retry.class, annotatedMethod);
    }

    @Override
    public void validate() {
        if (get(MAX_RETRIES, Integer.class) < -1) {
            throw new FaultToleranceDefinitionException("Invalid @Retry on " + getMethodInfo() + " : maxRetries shouldn't be lower than -1");
        }
        if (get(DELAY, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid @Retry on " + getMethodInfo() + " : delay shouldn't be lower than 0");
        }
        if (get(MAX_DURATION, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid @Retry on " + getMethodInfo() + " : maxDuration shouldn't be lower than 0");
        }
        if (get(MAX_DURATION, Long.class) <= get(DELAY, Long.class)) {
            throw new FaultToleranceDefinitionException("Invalid @Retry on " + getMethodInfo() + " : maxDuration should be greater than delay");
        }
        if (get(JITTER, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid @Retry on " + getMethodInfo() + " : jitter shouldn't be lower than 0");
        }
    }

    @Override
    protected Class<Retry> getConfigType() {
        return Retry.class;
    }

    public Class<?>[] getAbortOn() {
        return get(ABORT_ON);
    }

    public Class<?>[] getRetryOn() {
        return get(RETRY_ON);
    }

    public Long getJitter() {
        return get(JITTER);
    }

    public ChronoUnit getJitterDelayUnit() {
        return get(JITTER_DELAY_UNIT);
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = initKeys();

    private static Map<String, Class<?>> initKeys() {
        Map<String, Class<?>> keys = new HashMap<>();
        keys.put(MAX_RETRIES, Integer.class);
        keys.put(DELAY, Long.class);
        keys.put(DELAY_UNIT, ChronoUnit.class);
        keys.put(MAX_DURATION, Long.class);
        keys.put(DURATION_UNIT, ChronoUnit.class);
        keys.put(JITTER, Long.class);
        keys.put(JITTER_DELAY_UNIT, ChronoUnit.class);
        keys.put(RETRY_ON, Class[].class);
        keys.put(ABORT_ON, Class[].class);
        return Collections.unmodifiableMap(keys);
    }

}
