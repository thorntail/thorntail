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

/** Provides for simple runtime configuration of a Fraction.
 *
 * @author Bob McWhirter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
    /** Name of the module for the WildFly extension, if any. */
    String extension() default "";

    /** Flag to determine if DMR marshalling of this fraction should be used. */
    boolean marshal() default false;

    /** Flag to determine if this fraction is ignorable.  Whut? */
    boolean ignorable() default false;

    /** Specific parser-factory class name.  Prefer 'extension' and 'extensionClassName' though. */
    String parserFactoryClassName() default "";

    /** Name of a specific Extension implementation within the extension module. */
    String extensionClassName() default "";

    /** Additional modules to be added to each deployment (via prepareArchive()). */
    String[] deploymentModules() default {};
}
