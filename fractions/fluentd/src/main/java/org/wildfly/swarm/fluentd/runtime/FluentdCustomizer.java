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
package org.wildfly.swarm.fluentd.runtime;

import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.config.logging.CustomHandler;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.fluentd.FluentdFraction;
import org.wildfly.swarm.fluentd.FluentdProperties;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Heiko Braun
 */
@Post
@ApplicationScoped
public class FluentdCustomizer implements Customizer {

    @Inject
    @Any
    private FluentdFraction fluentdFraction;

    @Inject
    @Any
    private LoggingFraction loggingFraction;

    @Inject
    @ConfigurationValue(FluentdProperties.HOSTNAME)
    private Optional<String> hostname;

    @Inject
    @ConfigurationValue(FluentdProperties.PORT)
    private Optional<Integer> port;

    @Override
    public void customize() {
        try {
            String hostname = this.hostname.orElse(this.fluentdFraction.hostname());
            int port = this.port.orElse(this.fluentdFraction.port());

            if (hostname != null) {
                Properties handlerProps = new Properties();

                handlerProps.put("hostname", hostname);
                handlerProps.put("port", "" + port);
                handlerProps.put("tag", this.fluentdFraction.getTag());

                final CustomHandler<?> fluentd = new CustomHandler<>("fluentd-handler")
                        .module("org.wildfly.swarm.fluentd:runtime")
                        .attributeClass(FluentdHandler.class.getName())
                        .properties(handlerProps);

                final Level level = this.fluentdFraction.level();

                this.loggingFraction
                        //.consoleHandler(level, LoggingFraction.COLOR_PATTERN)
                        .customHandler(fluentd)
                        .rootLogger(level, LoggingFraction.CONSOLE, fluentd.getKey());
            } else {
                throw new IllegalArgumentException("Not enabling fluentd, no host set");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
