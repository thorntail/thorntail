package org.wildfly.swarm.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Heiko Braun
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface ConfigValue {

    @Nonbinding
    String value();
}