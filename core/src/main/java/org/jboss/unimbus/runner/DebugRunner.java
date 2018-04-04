package org.jboss.unimbus.runner;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.jboss.unimbus.Info;
import org.jboss.unimbus.impl.CoreMessages;

/**
 * Created by bob on 4/3/18.
 */
public class DebugRunner implements Runner {

    @Override
    public void run() throws Exception {

        CoreMessages.MESSAGES.usingDebugRunner();
        ProcessBuilder builder = new ProcessBuilder();

        builder.environment().remove(Info.KEY.toUpperCase() + "_DEBUG");
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
        return Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator)).stream()
                .map(e -> Paths.get(e))
                .map(e -> Files.isDirectory(e) ? e : e.getParent())
                .distinct()
                .collect(Collectors.toList());
    }

    private static List<String> command() {
        List<String> command = new ArrayList<>();

        command.add(java());
        command.addAll(jvmArgs());
        command.add(debug());
        command.addAll(classpath());
        command.add(mainClass());

        command = command.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return command;
    }

    private static String java() {
        return Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString();
    }

    private static String debug() {
        CoreMessages.MESSAGES.debugPort(8000);
        return "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000";
    }

    private static List<String> classpath() {
        List<String> args = new ArrayList<>();
        args.add("-classpath");
        args.add(System.getProperty("java.class.path"));
        return args;
    }

    private static List<String> jvmArgs() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();
        return jvmArgs;
    }

    private static String mainClass() {
        return System.getProperty("sun.java.command");
    }
}
