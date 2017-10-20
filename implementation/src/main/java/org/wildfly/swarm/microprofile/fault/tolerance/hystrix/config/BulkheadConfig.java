package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class BulkheadConfig extends GenericConfig<Bulkhead> {

    public BulkheadConfig(Bulkhead annotation, Method method) {
        super(annotation, method);
    }

    public BulkheadConfig(Annotated a) {
        super(a.getAnnotation(Bulkhead.class),a);
    }

    @Override
    public void validate() {
        if(get(VALUE, Integer.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Bulkhead on "+ annotated.toString() +" : value shouldn't be lower than 0");
        }

        if(get(WAITING_TASK_QUEUE, Integer.class) < 1) {
            throw new FaultToleranceDefinitionException("Invalid Bulkhead on "+ annotated.toString() +" : waitingTaskQueue shouldn't be lower than 1");
        }

    }

    @Override
    protected String getConfigType() {
        return "Bulkhead";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }


    public static final String VALUE = "value";

    public static final String WAITING_TASK_QUEUE = "waitingTaskQueue";

    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {
        {
            put(VALUE, Integer.class);
            put(WAITING_TASK_QUEUE, Integer.class);
        }
    });

}
