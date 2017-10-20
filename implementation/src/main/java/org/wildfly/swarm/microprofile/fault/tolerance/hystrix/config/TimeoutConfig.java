package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class TimeoutConfig extends GenericConfig<Timeout> {

    public static final String VALUE = "value";

    public static final String UNIT = "unit";

    public TimeoutConfig(Timeout annotation,Method method) {
        super(annotation, method);
    }

    public TimeoutConfig(Annotated annotated) {
        super(annotated.getAnnotation(Timeout.class), annotated);
    }

    @Override
    public void validate() {
        if(get(VALUE,Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Timeout on "+ annotated.toString() +" : value shouldn't be lower than 0");
        }
    }


    @Override
    protected String getConfigType() {
        return "Timeout";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }


    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {{
        put(VALUE, Long.class);
        put(UNIT, ChronoUnit.class);
    }});
}
