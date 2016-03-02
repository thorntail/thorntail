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

import org.wildfly.swarm.SwarmProperties;
import org.wildfly.swarm.config.logging.CustomHandler;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.logging.LoggingFraction;

/**
 * @author Ken Finnigan
 */
public class LogstashFraction implements Fraction {

    public LogstashFraction() {
        this("metaData", "wildflySwarmNode=${jboss.node.name}");
        hostname(SwarmProperties.propertyVar("logstash.host"));
    }

    public LogstashFraction(String nodeKey, String nodeValue) {
        this.formatterProperties.put(nodeKey, nodeValue);
    }

    public static Fraction createDefaultLogstashFraction() {
        return createDefaultLogstashFraction(true);
    }

    public static Fraction createDefaultLogstashFraction(boolean loggingFractionIfNoLogstash) {
        String hostname = System.getProperty(LogstashProperties.HOSTNAME);
        String port = System.getProperty(LogstashProperties.PORT);

        if (hostname != null && port != null) {
            return new LogstashFraction()
                    .hostname(hostname)
                    .port(port);
        }

        if (loggingFractionIfNoLogstash) {
            return LoggingFraction.createDefaultLoggingFraction();
        }
        return null;
    }

    public LogstashFraction level(Level level) {
        this.level = level;
        return this;
    }

    public LogstashFraction hostname(String hostname) {
        this.handlerProperties.put("hostname", SwarmProperties.propertyVar(LogstashProperties.HOSTNAME, hostname));
        return this;
    }

    public LogstashFraction port(String port) {
        this.handlerProperties.put("port", SwarmProperties.propertyVar(LogstashProperties.PORT, port));
        return this;
    }

    public LogstashFraction port(int port) {
        port("" + port);
        return this;
    }

    public LogstashFraction metadata(String key, String value) {
        this.formatterProperties.put(key, value);
        return this;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        final CustomHandler<?> logstashHandler = new CustomHandler<>("logstash-handler")
                .module("org.jboss.logmanager.ext")
                .attributeClass("org.jboss.logmanager.ext.handlers.SocketHandler")
                .namedFormatter("logstash")
                .properties(handlerProperties);
        initContext.fraction(new LoggingFraction()
                                     .customFormatter("logstash", "org.jboss.logmanager.ext", "org.jboss.logmanager.ext.formatters.LogstashFormatter", this.formatterProperties)
                                     .customHandler(logstashHandler)
                                     .rootLogger(this.level, logstashHandler.getKey()));
    }

    private Properties handlerProperties = new Properties();

    private Properties formatterProperties = new Properties();

    private Level level;

}
