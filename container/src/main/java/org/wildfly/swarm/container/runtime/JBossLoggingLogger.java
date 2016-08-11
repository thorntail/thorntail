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
package org.wildfly.swarm.container.runtime;

import javax.enterprise.inject.Vetoed;

import org.jboss.logging.Logger;
import org.wildfly.swarm.bootstrap.logging.BackingLogger;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class JBossLoggingLogger implements BackingLogger {
    public JBossLoggingLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Object getLevel() {
        if (this.logger.isEnabled(Logger.Level.TRACE)) {
            return "TRACE";
        }
        if (this.logger.isEnabled(Logger.Level.DEBUG)) {
            return "DEBUG";
        }
        if (this.logger.isEnabled(Logger.Level.INFO)) {
            return "INFO";
        }
        if (this.logger.isEnabled(Logger.Level.WARN)) {
            return "WARN";
        }
        if (this.logger.isEnabled(Logger.Level.ERROR)) {
            return "ERROR";
        }

        return "UNKNOWN";
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    @Override
    public void trace(Object message) {
        this.logger.trace(message);
    }

    @Override
    public void debug(Object message) {
        this.logger.debug(message);
    }

    @Override
    public void info(Object message) {
        this.logger.info(message);
    }

    @Override
    public void warn(Object message) {
        this.logger.warn(message);
    }

    @Override
    public void error(Object message) {
        this.logger.error(message);
    }

    @Override
    public void error(Object message, Throwable t) {
        this.logger.error(message, t);
    }

    private final Logger logger;
}
