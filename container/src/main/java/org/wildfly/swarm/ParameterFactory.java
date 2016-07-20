package org.wildfly.swarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Produces;

/**
 * @author Ken Finnigan
 */
public class ParameterFactory {
    public static String[] PARAMETERS;

    @Produces
    @Parameters
    public String[] getArgsAsArray() {
        return PARAMETERS;
    }

    @Produces
    @Parameters
    public List<String> getArgsAsList() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(PARAMETERS)));
    }
}
