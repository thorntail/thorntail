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
package org.wildfly.swarm.logstash.runtime;

import java.util.Properties;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.logging.CustomHandler;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.logstash.LogstashFraction;
import org.wildfly.swarm.logstash.LogstashProperties;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@Singleton
public class LogstashCustomizer implements Customizer {

    @Inject
    @Any
    private LogstashFraction logstashFraction;

    @Inject
    @Any
    private LoggingFraction loggingFraction;

    @Inject
    @ConfigurationValue(LogstashProperties.HOSTNAME)
    private String hostname;

    @Inject
    @ConfigurationValue(LogstashProperties.PORT)
    private Integer port;

    @Override
    public void customize() {
        try {
            String hostname = (this.hostname != null ? this.hostname : this.logstashFraction.hostname());
            int port = (this.port != null ? this.port : this.logstashFraction.port());

            if (hostname != null) {
                Properties handlerProps = new Properties();

                handlerProps.put("hostname", hostname);
                handlerProps.put("port", "" + port);

                final CustomHandler<?> logstashHandler = new CustomHandler<>("logstash-handler")
                        .module("org.jboss.logmanager.ext")
                        .attributeClass("org.jboss.logmanager.ext.handlers.SocketHandler")
                        .namedFormatter("logstash")
                        .properties(handlerProps);

                this.loggingFraction
                        .customFormatter("logstash", "org.jboss.logmanager.ext", "org.jboss.logmanager.ext.formatters.LogstashFormatter",
                                this.logstashFraction.formatterProperties())
                        .customHandler(logstashHandler)
                        .rootLogger(this.logstashFraction.level(), logstashHandler.getKey());
            } else {
                System.err.println("not enabling logstash, no host set");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
