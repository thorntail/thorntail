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
package org.wildfly.swarm.spi.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/** A group of configuration properties.
 *
 * <p>The root class of any configuration group <b>must</b> contain this annotation.
 * Any inner classes must be static, but are not required to use this annotation,
 * but they man.</p>
 *
 * <p>When calculating configuration item names, the presence of this annotation
 * will override the naming heuristics usually used.</p>
 *
 * @author Bob McWhirter
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ConfigurationName {
    @Nonbinding String value();

    final class Literal extends AnnotationLiteral<ConfigurationName> implements ConfigurationName {

        public static final Literal INSTANCE = new Literal();

        private static final long serialVersionUID = 1L;

        @Nonbinding
        @Override
        public String value() {
            return null;
        }
    }
}
