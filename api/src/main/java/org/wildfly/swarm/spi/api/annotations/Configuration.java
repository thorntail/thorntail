package org.wildfly.swarm.spi.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Bob McWhirter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
    String extension() default "";
    boolean marshal() default false;
    boolean ignorable() default false;
    String parserFactoryClassName() default "";
}
