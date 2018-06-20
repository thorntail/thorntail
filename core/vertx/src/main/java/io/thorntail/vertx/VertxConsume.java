/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.thorntail.vertx;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import io.vertx.core.eventbus.EventBus;

/**
 * Qualifier used to observe a message sent via {@link EventBus}:
 * <pre>
 * class Observer {
 *
 *     void onHello(&#64;Observes &#64;VertxConsume("hello.address") VertxMessage message) {
 *         System.out.println(message.body());
 *     }
 * }
 * </pre>
 *
 * An observer method must declare an event parameter of the type {@link VertxMessage} with {@link VertxConsume} qualifier in order to be notified.
 *
 * @author Martin Kouba
 * @see VertxMessage
 */
@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
public @interface VertxConsume {

    /**
     * Represents an address a consumer will be registered to.
     *
     * @return the address
     */
    String value();

    /**
     * If there is at least one blocking consumer for a specified address all observer methods will be notified using a worker thread.
     *
     * @return true if an observer notification involves some blocking code
     */
    @Nonbinding
    boolean blocking() default false;

    /**
     *
     *
     */
    public final class Literal extends AnnotationLiteral<VertxConsume> implements VertxConsume {

        private static final long serialVersionUID = 1L;

        private final String value;

        private final boolean blocking;

        public static Literal of(String value) {
            return new Literal(value, false);
        }

        public static Literal of(String value, boolean blocking) {
            return new Literal(value, blocking);
        }

        public String value() {
            return value;
        }

        @Override
        public boolean blocking() {
            return blocking;
        }

        private Literal(String value, boolean blocking) {
            this.value = value;
            this.blocking = blocking;
        }

    }

}
