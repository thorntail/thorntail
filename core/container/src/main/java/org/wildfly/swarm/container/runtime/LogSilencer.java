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
