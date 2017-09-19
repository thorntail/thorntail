package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class FallbackConfig extends GenericConfig<Fallback> {

    public static final String VALUE = "value";

    public static final String FALLBACK_METHOD = "fallbackMethod";

    public FallbackConfig(Fallback annotation, Method method) {
        super(annotation, method);
    }

    public FallbackConfig(Annotated annotated) {
        super(annotated.getAnnotation(Fallback.class), annotated);
    }

    @Override
    public void validate() {
        if (!"".equals(get(FALLBACK_METHOD))) {
            if (!Fallback.DEFAULT.class.equals(get(VALUE))) {
                throw new FaultToleranceDefinitionException("Fallback configuration can't contain an handler class and method at the same time");
            }
            Method fbm;
            try {
                fbm = method.getDeclaringClass().getMethod(get(FALLBACK_METHOD), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new FaultToleranceDefinitionException("Fallback method " + get(FALLBACK_METHOD) + " with same parameters than " + method.getName() + " not found", e);
            }
            if (!method.getReturnType().isAssignableFrom(fbm.getReturnType())) {
                throw new FaultToleranceDefinitionException("Fallback method " + get(FALLBACK_METHOD) + " must have a return type assignable to " + method.getName());
            }
        }
        if(!Fallback.DEFAULT.class.equals(get(VALUE))) {
            Class fbhc = get(VALUE);
            if(!method.getReturnType().isAssignableFrom((Class<?>) ((ParameterizedType)fbhc.getGenericInterfaces()[0]).getActualTypeArguments()[0])) {
                throw new FaultToleranceDefinitionException("Fallback handler type is not assignable to " + method.getName());
            }
        }
    }

    @Override
    protected String getConfigType() {
        return "Fallback";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {{
        put(VALUE, Class.class);
        put(FALLBACK_METHOD, String.class);
    }});
}
