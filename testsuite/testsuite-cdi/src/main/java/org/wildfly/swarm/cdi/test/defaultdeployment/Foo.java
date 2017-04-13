/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.cdi.test.defaultdeployment;

import javax.inject.Inject;

import org.jboss.cdi.tck.extlib.Strict;
import org.jboss.cdi.tck.extlib.Translator;

public class Foo {

    // Translator comes from the org.jboss.cdi.tck:cdi-tck-ext-lib dependency
    @Inject
    @Strict
    Translator translator;

    String echo(String text) {
        return translator.echo(text);
    }

}
