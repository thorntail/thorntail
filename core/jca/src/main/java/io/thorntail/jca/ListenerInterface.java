package io.thorntail.jca;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Created by bob on 2/21/18.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface ListenerInterface {
    @Nonbinding
    Class<?> value();

    final class Literal extends AnnotationLiteral<ListenerInterface> implements ListenerInterface {

        public static final Literal INSTANCE = new Literal();

        @Override
        public Class<?> value() {
            return null;
        }

        private static final long serialVersionUID = 1L;
    }
}
