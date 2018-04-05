package org.jboss.unimbus.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jboss.unimbus.DevMode;
import org.jboss.unimbus.impl.CoreMessages;

/**
 * Created by bob on 4/3/18.
 */
public class ReloadRunner extends AbstractForkedRunner {

    private static final String FAKEREPLACE = "fakereplace";
    private static final String FAKEREPLACE_ENVIRONMENT_VAR_NAME = FAKEREPLACE.toUpperCase();
    private static final String FAKEREPLACE_JAR = FAKEREPLACE + ".jar";

    @Override
    public void run() throws Exception {
        if (!isEnabled()) {
            CoreMessages.MESSAGES.reloadRequestedButUnavailable();
            new RestartRunner().run();
        } else {
            CoreMessages.MESSAGES.usingDevMode(DevMode.RELOAD);
            CoreMessages.MESSAGES.reloadEnabled();
            ProcessBuilder builder = new ProcessBuilder();
            builder.environment().remove(DevMode.ENVIRONMENT_VAR_NAME);
            builder.command(command());
            builder.inheritIO();
            Process process = builder.start();
            process.waitFor();
        }
    }

    @Override
    protected List<String> extraJvmArgs() {
        List<String> args = new ArrayList<>();
        args.add(debug());
        args.addAll(fakereplace());
        return args;
    }

    @Override
    protected boolean accept(String classpathEntry) {
        String fakereplace = fakereplaceJarPath();
        if (classpathEntry.equals(fakereplace)) {
            return false;
        }
        return super.accept(classpathEntry);
    }

    protected List<String> fakereplace() {
        String fakereplace = fakereplaceJarPath();

        List<String> args = new ArrayList<>();

        if (!fakereplace.endsWith(FAKEREPLACE_JAR)) {
            args.add("-Xbootclasspath/a:" + fakereplace);

        }
        args.add("-javaagent:" + fakereplace + "=log=info");
        return args;
    }

    protected boolean isEnabled() {
        return fakereplaceJarPath() != null;
    }

    protected String fakereplaceJarPath() {
        String path = System.getenv(FAKEREPLACE_ENVIRONMENT_VAR_NAME);
        if (path == null) {
            Optional<String> found = allClassPathEntries().stream()
                    .filter(e -> e.contains(FAKEREPLACE))
                    .findFirst();

            if (!found.isPresent()) {
                return null;
            }

            path = found.get();
        }

        return path;
    }


}
