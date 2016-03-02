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
package org.wildfly.swarm.bootstrap.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class BootstrapLogger {

    private BootstrapLogger(String name) {
        this.name = name;
    }

    public static BootstrapLogger logger(String name) {
        synchronized (LOGGERS) {
            BootstrapLogger logger = LOGGERS.get(name);
            if (logger == null) {
                logger = new BootstrapLogger(name);
                LOGGERS.put(name, logger);
            }
            return logger;
        }
    }

    public static void setBackingLoggerManager(BackingLoggerManager manager) {
        synchronized (LOCK) {
            MANAGER = manager;
            LOGGERS.values().forEach(BootstrapLogger::resetBackingLogger);
        }
    }

    public void trace(Object message) {
        getBackingLogger().trace(message);
    }

    public void debug(Object message) {
        getBackingLogger().debug(message);
    }

    public void info(Object message) {
        getBackingLogger().info(message);
    }

    public void warn(Object message) {
        getBackingLogger().warn(message);
    }

    public void error(Object message) {
        getBackingLogger().error(message);
    }

    public void error(Object message, Throwable t) {
        getBackingLogger().error(message, t);
    }

    public Object getLevel() {
        return getBackingLogger().getLevel();
    }

    public boolean isDebugEnabled() {
        return getBackingLogger().isDebugEnabled();
    }

    public boolean isTraceEnabled() {
        return getBackingLogger().isTraceEnabled();
    }

    public String toString() {
        return "[" + this.name + ": " + this.getLevel() + "]";
    }

    private BackingLogger getBackingLogger() {
        if (this.backingLogger == null) {
            synchronized (LOCK) {
                this.backingLogger = MANAGER.getBackingLogger(this.name);
            }
        }
        return this.backingLogger;
    }

    void resetBackingLogger() {
        this.backingLogger = null;
    }

    private static Map<String, BootstrapLogger> LOGGERS = new HashMap<>();

    private static BackingLoggerManager MANAGER = InitialLoggerManager.INSTANCE;

    private static Object LOCK = new Object();

    private final String name;

    private BackingLogger backingLogger;

    public enum Level {
        NONE(java.util.logging.Level.OFF),
        ERROR(java.util.logging.Level.SEVERE),
        WARN(java.util.logging.Level.WARNING),
        INFO(java.util.logging.Level.INFO),
        DEBUG(java.util.logging.Level.FINER),
        TRACE(java.util.logging.Level.FINEST),
        ALL(java.util.logging.Level.ALL);

        Level(java.util.logging.Level jul) {
            this.jul = jul;
        }

        public java.util.logging.Level toJUL() {
            return this.jul;
        }

        private final java.util.logging.Level jul;
    }
}
