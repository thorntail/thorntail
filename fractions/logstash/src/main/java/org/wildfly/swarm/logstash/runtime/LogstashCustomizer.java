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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.logging.CustomHandler;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.logstash.LogstashFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@Singleton
public class LogstashCustomizer implements Customizer {

    @Inject
    public LogstashFraction logstash;

    @Inject
    public LoggingFraction logging;

    @Override
    public void customize() {

        if (logstash.enabled()) {
            Properties handlerProps = new Properties();

            handlerProps.put("hostname", this.logstash.hostname() );
            handlerProps.put("port", "" + this.logstash.port() );

            final CustomHandler<?> logstashHandler = new CustomHandler<>("logstash-handler")
                    .module("org.jboss.logmanager.ext")
                    .attributeClass("org.jboss.logmanager.ext.handlers.SocketHandler")
                    .namedFormatter("logstash")
                    .properties(handlerProps);

            this.logging
                    .customFormatter("logstash", "org.jboss.logmanager.ext", "org.jboss.logmanager.ext.formatters.LogstashFormatter",
                            this.logstash.formatterProperties())
                    .customHandler(logstashHandler)
                    .rootLogger(this.logstash.level(), logstashHandler.getKey());
        }
    }
}
