package org.wildfly.swarm.fluentd.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ErrorManager;
import java.util.logging.Logger;

import org.fluentd.logger.sender.RawSocketSender;
import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

/**
 * @author Heiko Braun
 * @since 14/11/2016
 */
public class FluentdHandler extends ExtHandler {

    public enum Key {
        EXCEPTION("exception"),
        LEVEL("level"),
        LOGGER_CLASS_NAME("loggerClassName"),
        LOGGER_NAME("loggerName"),
        MDC("mdc"),
        MESSAGE("message"),
        NDC("ndc"),
        RECORD("record"),
        SEQUENCE("sequence"),
        THREAD_ID("threadId"),
        THREAD_NAME("threadName"),
        TIMESTAMP("timestamp");

        private final String key;

        Key(final String key) {
            this.key = key;
        }

        /**
         * Returns the name of the key for the structure.
         *
         * @return the name of they key
         */
        public String getKey() {
            return key;
        }
    }

    public FluentdHandler() {
        setAutoFlush(false);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    protected void doPublish(ExtLogRecord record) {

        synchronized (this) {
            if(!initialized) {
                try {
                    initialize();
                } catch (Exception e) {
                    reportError("Error creating fluentd connection", e, ErrorManager.OPEN_FAILURE);
                    setEnabled(false);
                }
            }
        }

        if(initialized) {
            Map<String, Object> entries = new HashMap<>();

            entries.put(Key.SEQUENCE.getKey(), record.getSequenceNumber());
            entries.put(Key.LEVEL.getKey(), record.getLevel().getName());
            entries.put(Key.THREAD_NAME.getKey(), record.getThreadName());
            entries.put(Key.MESSAGE.getKey(), record.getFormattedMessage());
            entries.put(Key.THREAD_ID.getKey(), record.getThreadID());
            entries.put(Key.MDC.getKey(), record.getMdcCopy());
            entries.put(Key.NDC.getKey(), record.getNdc());

            this.sender.emit(this.tag, record.getMillis(), entries);
        }

    }

    private void initialize() {
        try {
            this.sender = new RawSocketSender(hostname, port);
            this.initialized = true;
            log.info("Connected to fluentd daemon");
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialise fluentd connection", t);
        }
    }

    @Override
    public void flush() {
        // should not happen
    }

    @Override
    public void close() {
        super.close();
        log.info("Disconnect from fluentd daemon ...");
        synchronized (this) {
            safeClose(this.sender);
            this.sender = null;
            this.initialized = false;
        }
    }

    private void safeClose(RawSocketSender c) {
        try {
            if (c != null) c.close();
        } catch (Exception e) {
            reportError("Error closing resource", e, ErrorManager.CLOSE_FAILURE);
        } catch (Throwable ignored) {
        }
    }

    private static final java.util.logging.Logger log = Logger.getLogger("org.wildfly.swarm.fluentd");

    private String hostname;

    private int port;

    private boolean initialized;

    private RawSocketSender sender;

    private String tag;

}
