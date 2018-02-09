package org.jboss.unimbus.jms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface MessageDriven {

    @Nonbinding
    String topic() default "";

    @Nonbinding
    String queue() default "";

    public static final class Literal extends AnnotationLiteral<MessageDriven> implements MessageDriven {

        public static final MessageDriven INSTANCE = new Literal();

        @Override
        public String topic() {
            return "";
        }

        @Override
        public String queue() {
            return "";
        }
    }
}
