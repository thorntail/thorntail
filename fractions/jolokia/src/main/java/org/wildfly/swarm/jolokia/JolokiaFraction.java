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
package org.wildfly.swarm.jolokia;

import java.io.File;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.jolokia.access.APIJolokiaAccessPreparer;
import org.wildfly.swarm.jolokia.access.FileJolokiaAccessPreparer;
import org.wildfly.swarm.jolokia.access.JolokiaAccess;
import org.wildfly.swarm.jolokia.access.URLJolokiaAccessPreparer;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public class JolokiaFraction implements Fraction<JolokiaFraction> {

    public JolokiaFraction() {
        this("jolokia");
    }

    public JolokiaFraction(String context) {
        this.context = context;
    }

    public JolokiaFraction context(String context) {
        this.context = context;
        return this;
    }

    public String context() {
        return this.context;
    }

    public JolokiaFraction prepareJolokiaWar(Consumer<Archive> jolokiaWarPreparer) {
        this.jolokiaWarPreparer = jolokiaWarPreparer;
        return this;
    }

    public Consumer<Archive> jolokiaWarPreparer() {
        return this.jolokiaWarPreparer;
    }

    public static Consumer<Archive> jolokiaAccessXml(File file) {
        return new FileJolokiaAccessPreparer(file);

    }

    public static Consumer<Archive> jolokiaAccessXml(URL url) {
        return new URLJolokiaAccessPreparer(url);
    }

    public static Consumer<Archive> jolokiaAccess(Consumer<JolokiaAccess> config) {
        JolokiaAccess access = new JolokiaAccess();
        config.accept(access);
        return new APIJolokiaAccessPreparer(access);
    }

    public static Consumer<Archive> jolokiaAccess(Supplier<JolokiaAccess> supplier) {
        return new APIJolokiaAccessPreparer(supplier.get());
    }

    @AttributeDocumentation("Context path for the Jolokia endpoints")
    private String context;

    private Consumer<Archive> jolokiaWarPreparer;

}
