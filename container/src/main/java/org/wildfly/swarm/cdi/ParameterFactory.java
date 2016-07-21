package org.wildfly.swarm.cdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.wildfly.swarm.Parameters;
import org.wildfly.swarm.Swarm;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class ParameterFactory {

    private String[] args;
    private List<String> argsList;

    public ParameterFactory() {
        String[] args = Swarm.COMMAND_LINE_ARGS;
        if (args == null) {
            args = new String[]{};
        }
        this.args = args;
        this.argsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(this.args)));
    }

    @Produces
    @Parameters
    public String[] getArgsAsArray() {
        return this.args;
    }

    @Produces
    @Parameters
    public List<String> getArgsAsList() {
        return this.argsList;
    }
}
