package io.thorntail.runner;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.thorntail.impl.KernelMessages;

/**
 * Created by bob on 4/3/18.
 */
public abstract class AbstractForkedRunner implements Runner {

    protected List<String> command() {
        List<String> command = new ArrayList<>();

        command.add(java());
        command.addAll(jvmArgs());
        command.addAll(extraJvmArgs());
        command.addAll(classpath());
        command.add(mainClass());

        command = command.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return command;
    }

    protected String java() {
        return Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString();
    }

    protected List<String> extraJvmArgs() {
        return Collections.emptyList();
    }

    protected List<String> classpath() {
        List<String> args = new ArrayList<>();
        args.add("-classpath");
        args.add(filteredClassPathEntries().stream().collect(Collectors.joining(File.pathSeparator)));
        return args;
    }

    protected List<String> allClassPathEntries() {
        String cp = System.getProperty("java.class.path");
        String[] entries = cp.split(File.pathSeparator);

        List<String> list = new ArrayList<>();

        for (String entry : entries) {
            list.add(entry);
        }

        return list;
    }

    protected List<String> filteredClassPathEntries() {
        return allClassPathEntries().stream()
                .filter(this::accept).collect(Collectors.toList());
    }

    protected boolean accept(String classpathEntry) {
        return true;
    }

    protected List<String> jvmArgs() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();
        return jvmArgs;
    }

    protected String mainClass() {
        return System.getProperty("sun.java.command");
    }

    protected String debug() {
        KernelMessages.MESSAGES.debugPort(8000);
        return "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000";
    }
}
