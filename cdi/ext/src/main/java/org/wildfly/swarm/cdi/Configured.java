package org.wildfly.swarm.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Heiko Braun
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface Configured {

}