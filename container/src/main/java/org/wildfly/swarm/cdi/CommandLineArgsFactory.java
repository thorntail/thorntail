package org.wildfly.swarm.cdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.wildfly.swarm.CommandLineArgs;
import org.wildfly.swarm.Swarm;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class CommandLineArgsFactory {

    private String[] args;
    private List<String> argsList;

    public CommandLineArgsFactory() {
        String[] args = Swarm.COMMAND_LINE_ARGS;
        if (args == null) {
            args = new String[]{};
        }
        this.args = args;
        this.argsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(this.args)));
    }

    @Produces
    @CommandLineArgs
    public String[] getArgsAsArray() {
        return this.args;
    }

    @Produces
    @CommandLineArgs
    public List<String> getArgsAsList() {
        return this.argsList;
    }
}
