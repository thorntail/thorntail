package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config;

import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

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

    public CircuitBreakerConfig(CircuitBreaker cb, Method method) {
        super(cb, method);
    }

    public CircuitBreakerConfig(Annotated a) {
        super(a.getAnnotation(CircuitBreaker.class),a);
    }

    @Override
    public void validate() {
        if(get(DELAY, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid CircuitBreaker on "+ annotated.toString() +" : delay shouldn't be lower than 0");
        }
        if(get(REQUEST_VOLUME_THRESHOLD, Integer.class) < 1) {
            throw new FaultToleranceDefinitionException("Invalid CircuitBreaker on "+ annotated.toString() +" : requestVolumeThreshold shouldn't be lower than 1");
        }
        if(get(FAILURE_RATIO, Double.class) < 0 || get(FAILURE_RATIO, Double.class) >1) {
            throw new FaultToleranceDefinitionException("Invalid CircuitBreaker on "+ annotated.toString() +" : failureRation should be between 0 and 1");
        }
        if(get(SUCCESS_THRESHOLD, Integer.class) < 1) {
            throw new FaultToleranceDefinitionException("Invalid CircuitBreaker on "+ annotated.toString() +" : successThreshold shouldn't be lower than 1");
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

    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {{
        put(DELAY, Long.class);
        put(DELAY_UNIT, ChronoUnit.class);
        put(FAIL_ON, Class[].class);
        put(FAILURE_RATIO, Double.class);
        put(REQUEST_VOLUME_THRESHOLD, Integer.class);
        put(SUCCESS_THRESHOLD, Integer.class);
        put(SYNCHRONOUS_STATE_VALIDATION, Boolean.class);
    }});


}
