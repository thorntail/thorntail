package io.thorntail.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.sun.nio.file.SensitivityWatchEventModifier;
import io.thorntail.impl.CoreMessages;
import io.thorntail.DevMode;

/**
 * Created by bob on 4/3/18.
 */
public class RestartRunner extends AbstractForkedRunner {

    private static final String JAVA_CLASS_PATH_PROPERTY_NAME = "java.class.path";

    @Override
    public void run() throws Exception {
        CoreMessages.MESSAGES.usingDevMode(DevMode.RESTART);
        CoreMessages.MESSAGES.restartEnabled();

        ProcessBuilder builder = new ProcessBuilder();

        builder.environment().remove(DevMode.ENVIRONMENT_VAR_NAME);
        builder.command(command());
        builder.inheritIO();
        startWatchdog(builder);
    }

    private void startWatchdog(ProcessBuilder builder) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        boolean sensitiveAvailable = false;
        try {
            Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
            sensitiveAvailable = true;
            CoreMessages.MESSAGES.highSensitiveFileWatching();
        } catch (ClassNotFoundException e) {
            // ignore
        }

        boolean finalSensitiveAvailable = sensitiveAvailable;

        pathsToWatch().forEach(p -> {
            try {
                if (finalSensitiveAvailable) {
                    p.register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
                } else {
                    p.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                }
                CoreMessages.MESSAGES.watchingDirectory(p.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        new Thread(() -> {
            while (true) {
                try {
                    CoreMessages.MESSAGES.launchingChildProcess();
                    Process process = builder.start();
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        watchEvent.count();
                    }
                    CoreMessages.MESSAGES.changeDetected(key.watchable().toString());
                    CoreMessages.MESSAGES.destroyingChildProcess();
                    process.destroy();
                    if (!process.waitFor(5, TimeUnit.SECONDS)) {
                        CoreMessages.MESSAGES.destroyingChildProcessForcibly();
                        process.destroyForcibly();
                    }
                    if (!process.waitFor(5, TimeUnit.SECONDS)) {
                        CoreMessages.MESSAGES.childProcessDidNotExit();
                        break;
                    }
                    key.reset();
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private static List<Path> pathsToWatch() {
        return Arrays.asList(System.getProperty(JAVA_CLASS_PATH_PROPERTY_NAME).split(File.pathSeparator)).stream()
                .map(e -> Paths.get(e))
                .map(e -> Files.isDirectory(e) ? e : e.getParent())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> extraJvmArgs() {
        return Collections.singletonList(debug());
    }

}
