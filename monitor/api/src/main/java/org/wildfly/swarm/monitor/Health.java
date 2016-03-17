package org.wildfly.swarm.monitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a JAX-RS method should be used check liveliness
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Health {

    /**
     * By default all health endpoints will inherit the security of the implicit '/health' endpoint.
     * This method allows to exclude certain endpoints from that policy.
     * @return
     */
    boolean inheritSecurity() default true;
}
