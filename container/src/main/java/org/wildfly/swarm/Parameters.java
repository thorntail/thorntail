package org.wildfly.swarm;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Ken Finnigan
 */
@Qualifier
@Retention(RUNTIME)
@Target({ METHOD, PARAMETER, FIELD })
public @interface Parameters {
}
