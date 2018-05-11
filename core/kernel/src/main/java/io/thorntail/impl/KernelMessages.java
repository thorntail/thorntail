package io.thorntail.impl;

import java.io.IOException;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;

import static io.thorntail.logging.impl.LoggingUtil.CODE;
import static io.thorntail.Info.NAME;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface KernelMessages extends BasicLogger {
    KernelMessages MESSAGES = Logger.getMessageLogger(KernelMessages.class, LoggingUtil.loggerCategory("kernel"));

    int OFFSET = MessageOffsets.KERNEL_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = NAME + " - version %s")
    void versionInfo(String version);

    // --

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2 + OFFSET, value = "Unable to process YAML configuration %s. Add snakeyaml to your dependencies to enable")
    void unableToProcessYaml(String url);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3 + OFFSET, value = "Configuration location: %s")
    void configLocation(String path);

    // --

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10 + OFFSET, value = NAME + " starting")
    void starting();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 11 + OFFSET, value = NAME + " stopping")
    void stopping();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 12 + OFFSET, value = NAME + " stopped")
    void stopped();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 13 + OFFSET, value = "phase [%s] completed in %s")
    void timing(String phase, String time);



    // --

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 30 + OFFSET, value = "No valid OpenTracing Tracer resolved")
    void noValidTracer();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 31 + OFFSET, value = "Registered OpenTracing Tracer '%s'")
    void registeredTracer(String tracerClass);

    // --

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 40 + OFFSET, value = "Loading attached index from %s")
    void loadIndex(String indexLocation);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 41 + OFFSET, value = "Failed to load index from %s")
    void loadingIndexFileFailed(String indexLocation, @Cause IOException ioe);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 42 + OFFSET, value = "No index found at %s")
    void indexNotFound(String indexLocation);

    // --

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 50 + OFFSET, value = "Using dev-mode: %s")
    void usingDevMode(String mode);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 51 + OFFSET, value = "Debug listener at port: %s")
    void debugPort(int port);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 52 + OFFSET, value = "Process restart enabled")
    void restartEnabled();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 53 + OFFSET, value = "Enabled high-sensitive file watching")
    void highSensitiveFileWatching();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 54 + OFFSET, value = "Destroying child process")
    void destroyingChildProcess();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 55 + OFFSET, value = "Destroying child process forcibly")
    void destroyingChildProcessForcibly();


    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 56 + OFFSET, value = "Launching child process")
    void launchingChildProcess();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 57 + OFFSET, value = "Child process did not exit, giving up")
    void childProcessDidNotExit();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 58 + OFFSET, value = "Watching for changes in '%s'")
    void watchingDirectory(String dir);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 59 + OFFSET, value = "Change detected in '%s'")
    void changeDetected(String dir);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 60 + OFFSET, value = "Class reloading requested but is not available; falling back to restart mode.\nEnsure :devtools dependency is included.")
    void reloadRequestedButUnavailable();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 61 + OFFSET, value = "Class reloading enabled")
    void reloadEnabled();

    // --



    // --

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 999 + OFFSET, value = NAME + " started in %s")
    void started(String startTime);

}
