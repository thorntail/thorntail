package org.wildfly.swarm.container.runtime.xmlconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * @author Bob McWhirter
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface XMLConfig {
    class Literal extends AnnotationLiteral<XMLConfig> implements XMLConfig {
        public static Literal INSTANCE = new Literal();
    }
}
