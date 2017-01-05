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
package org.wildfly.swarm.logstash;

import java.util.Properties;

import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;

import static org.wildfly.swarm.logstash.LogstashProperties.DEFAULT_HOSTNAME;
import static org.wildfly.swarm.logstash.LogstashProperties.DEFAULT_PORT;
import static org.wildfly.swarm.spi.api.Defaultable.ifAnyExplicitlySet;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.string;


/**
 * @author Ken Finnigan
 */
@Configurable("swarm.logstash")
public class LogstashFraction implements Fraction<LogstashFraction> {

    public LogstashFraction() {
        this("metaData", "wildflySwarmNode=${jboss.node.name}");
    }

    public LogstashFraction(String nodeKey, String nodeValue) {
        this.formatterProperties.put(nodeKey, nodeValue);
    }

    public static Fraction createDefaultLogstashFraction() {
        return new LogstashFraction();
    }

    public LogstashFraction level(Level level) {
        this.level = level;
        return this;
    }

    public Level level() {
        return this.level;
    }

    public LogstashFraction hostname(String hostname) {
        this.hostname.set(hostname);
        return this;
    }

    public String hostname() {
        return this.hostname.get();
    }

    public LogstashFraction port(int port) {
        this.port.set(port);
        return this;
    }

    public int port() {
        return this.port.get();
    }

    public Properties formatterProperties() {
        return this.formatterProperties;
    }

    public LogstashFraction metadata(String key, String value) {
        this.formatterProperties.put(key, value);
        return this;
    }

    public LogstashFraction enabled(boolean enable) {
        this.enabled.set(enable);
        return this;
    }

    public boolean enabled() {
        return this.enabled.get();
    }

    private Defaultable<String> hostname = string(DEFAULT_HOSTNAME);

    private Defaultable<Integer> port = integer(DEFAULT_PORT);

    private Defaultable<Boolean> enabled = ifAnyExplicitlySet(hostname, port);

    private Properties formatterProperties = new Properties();

    private Level level;

}
