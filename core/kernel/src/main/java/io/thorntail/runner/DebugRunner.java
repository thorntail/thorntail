package io.thorntail.runner;

import java.util.ArrayList;
import java.util.List;

import io.thorntail.DevMode;
import io.thorntail.impl.KernelMessages;

/**
 * Created by bob on 4/3/18.
 */
public class DebugRunner extends AbstractForkedRunner {

    @Override
    public void run() throws Exception {
        KernelMessages.MESSAGES.usingDevMode(DevMode.DEBUG);
        ProcessBuilder builder = new ProcessBuilder();
        builder.environment().remove(DevMode.ENVIRONMENT_VAR_NAME);
        builder.command(command());
        builder.inheritIO();
        Process process = builder.start();
        process.waitFor();
    }

    @Override
    protected List<String> extraJvmArgs() {
        List<String> args = new ArrayList<>();
        args.add(debug());
        return args;
    }

}
