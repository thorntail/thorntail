package org.wildfly.swarm.management.console;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * Created by ggastald on 02/06/16.
 */
@Singleton
@DefaultFraction
public class ManagementConsoleFraction implements Fraction {

    public ManagementConsoleFraction() {
        contextRoot(DEFAULT_CONTEXT);
    }

    public ManagementConsoleFraction contextRoot(String context) {
        this.context = context;
        return this;
    }

    public String contextRoot() {
        return context;
    }

    private final String DEFAULT_CONTEXT = "/console";

    private String context = DEFAULT_CONTEXT;
}
