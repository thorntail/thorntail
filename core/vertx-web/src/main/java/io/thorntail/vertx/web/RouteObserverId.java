package io.thorntail.vertx.web;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * Qualifier used to distinguish observer methods annotated with {@link WebRoute}.
 *
 * @author Martin Kouba
 */
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface RouteObserverId {

    String value();

    public static class Literal extends AnnotationLiteral<RouteObserverId> implements RouteObserverId {

        private static final long serialVersionUID = 1L;

        public static Literal of(String value) {
            return new Literal(value);
        }

        public Literal(String value) {
            this.value = value;
        }

        private final String value;

        @Override
        public String value() {
            return value;
        }

    }

}
