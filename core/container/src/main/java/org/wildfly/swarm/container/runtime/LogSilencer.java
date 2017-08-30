/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class LogSilencer {

    private final String name;

    private Logger logger;

    private Level originalLevel;

    private LogSilencer(String name) {
        this.name = name;
    }

    void silence() {
        try {
            LogManager lm = LogManager.getLogManager();
            this.logger = lm.getLogger(name);
            this.originalLevel = this.logger.getLevel();
            this.logger.setLevel(Level.SEVERE);
        } catch (Throwable t) {
            // ignore;
        }
    }

    void unsilence() {
        if (this.logger != null) {
            this.logger.setLevel(this.originalLevel);
        }
    }

    public static SilentExecutor silently(String... loggers) {
        return new SilentExecutor(
                Arrays.stream(loggers)
                        .map(LogSilencer::new)
                        .collect(Collectors.toList()));
    }

    public static class SilentExecutor {
        private final List<LogSilencer> silencers;

        SilentExecutor(List<LogSilencer> silencers) {
            this.silencers = silencers;
        }

        public <T> T execute(Callable<T> block) throws Exception {
            this.silencers.forEach(LogSilencer::silence);
            try {
                return block.call();
            } finally {
                this.silencers.forEach(LogSilencer::unsilence);
            }
        }

    }
}
