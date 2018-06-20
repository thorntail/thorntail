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

import javax.enterprise.event.Event;
import javax.inject.Qualifier;

import io.vertx.core.eventbus.EventBus;

/**
 * Qualifier used together with {@link Event} to publish a message:
 *
 * <pre>
 * class Emitter {
 *
 *     &#64;VertxPublish("hello.address")
 *     &#64;Inject
 *     Event<String> event;
 *
 *     void hello() {
 *         event.fire("Hello!");
 *     }
 * }
 * </pre>
 *
 * If you need to configure the delivery then use the {@link EventBus} directly:
 *
 * <pre>
 * class Emitter {
 *
 *     &#64;Inject
 *     Vertx vertx;
 *
 *     void hello() {
 *         vertx.eventBus().publish("hello.address", "Hello!", new DeliveryOptions());
 *     }
 * }
 * </pre>
 *
 * @author Martin Kouba
 * @see EventBus#publish(String, Object)
 */
@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
public @interface VertxPublish {

    /**
     * Represents an address a message will be published to.
     *
     * @return the address
     */
    String value();

}
