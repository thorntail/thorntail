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
package org.wildfly.swarm.fluentd;

import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Heiko Braun
 */
public class FluentdFraction implements Fraction<FluentdFraction> {

    public FluentdFraction() {
        this.hostname = "localhost";
        this.port = 24224;
        this.level = Level.INFO;
    }

    public static Fraction createDefaultFluentdFraction() {
        return new FluentdFraction();
    }

    public FluentdFraction level(Level level) {
        this.level = level;
        return this;
    }

    public Level level() {
        return this.level;
    }

    public FluentdFraction hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public String hostname() {
        return this.hostname;
    }

    public FluentdFraction port(int port) {
        this.port = port;
        return this;
    }

    public int port() {
        return this.port;
    }

    public String getTag() {
        return tag;
    }

    @AttributeDocumentation("Host name of the fluentd server")
    private String hostname;

    @AttributeDocumentation("Port of the fluentd server")
    private int port;

    @AttributeDocumentation("Logging level")
    private Level level;

    @AttributeDocumentation("Logging tag")
    private String tag = "local";

}
