/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.spi.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/** Qualifier attached to a {@link org.wildfly.swarm.spi.api.Customizer} to assist in ordering.
 *
 * <p>After all fractions are configured by user actions, all instances of {@code Customizer} are
 * executed.  First all marked as {@code Pre} are executed in an arbitrary order, followed by
 * all marked as {@code Post}, also in an arbitrary order.</p>
 *
 * @see Post
 *
 * @author Ken Finnigan
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface Pre {
}
