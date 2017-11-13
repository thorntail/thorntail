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
package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

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

    public RetryConfig(Retry annotation, Method method) {
        super(annotation, method);
        this.maxExecNumber = (int) get(MAX_RETRIES) + 1;
        this.maxDuration = Duration.of(get(MAX_DURATION), get(DURATION_UNIT)).toNanos();
        this.delay = Duration.of(get(DELAY), get(DELAY_UNIT)).toMillis();
    }

    public RetryConfig(Annotated annotated) {
        super(annotated.getAnnotation(Retry.class), annotated);
        this.maxExecNumber = (int) get(MAX_RETRIES) + 1;
        this.maxDuration = Duration.of(get(MAX_DURATION), get(DURATION_UNIT)).toNanos();
        this.delay = Duration.of(get(DELAY), get(DELAY_UNIT)).toMillis();
    }

    @Override
    public void validate() {
        if(get(MAX_RETRIES,Integer.class) < -1) {
            throw new FaultToleranceDefinitionException("Invalid Retry on "+ annotated.toString() +" : maxRetries shouldn't be lower than -1");
        }
        if(get(DELAY, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Retry on "+ annotated.toString() +" : delay shouldn't be lower than 0");
        }
        if(get(MAX_DURATION, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Retry on "+ annotated.toString() +" : maxDuration shouldn't be lower than 0");
        }
        if(get(MAX_DURATION, Long.class) <= get(DELAY, Long.class)) {
            throw new FaultToleranceDefinitionException("Invalid Retry on "+ annotated.toString() +" : maxDuration should be greater than delay");
        }
        if(get(JITTER,Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Retry on "+ annotated.toString() +" : jitter shouldn't be lower than 0");
        }
    }

    @Override
    protected String getConfigType() {
        return "Retry";
    }

    public int getMaxExecNumber() {
        return maxExecNumber;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public long getDelay() {
        return delay;
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

    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {{
        put(MAX_RETRIES, Integer.class);
        put(DELAY, Long.class);
        put(DELAY_UNIT, ChronoUnit.class);
        put(MAX_DURATION, Long.class);
        put(DURATION_UNIT, ChronoUnit.class);
        put(JITTER, Long.class);
        put(JITTER_DELAY_UNIT, ChronoUnit.class);
        put(RETRY_ON, Class[].class);
        put(ABORT_ON, Class[].class);
    }});

    private final long maxDuration;

    private final long delay;

    private final int maxExecNumber;

}
