package org.jboss.unimbus.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface DefaultValue {

    String value() default "";

    final class Literal extends AnnotationLiteral<DefaultValue> implements DefaultValue {

        public static final Literal INSTANCE = new Literal();

        public Literal() {
            this("");
        }

        public Literal(String key) {
            this.key = key;
        }

        @Override
        public String value() {
            return this.key;
        }

        private String key;

        private static final long serialVersionUID = 1L;

    }
}
