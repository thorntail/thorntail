package org.wildfly.swarm.container.runtime;

/**
 * Created by bob on 5/16/17.
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD, TYPE, PARAMETER})
public @interface ImplicitDeployment {
    class Literal extends AnnotationLiteral<ImplicitDeployment> implements ImplicitDeployment {
        public static Literal INSTANCE = new Literal();
    }
}
