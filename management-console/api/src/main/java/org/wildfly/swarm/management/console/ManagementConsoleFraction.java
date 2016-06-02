package org.wildfly.swarm.management.console;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.undertow.UndertowProperties;

/**
 * Created by ggastald on 02/06/16.
 */
public class ManagementConsoleFraction implements Fraction {

    public static final String VERSION;

    public ManagementConsoleFraction() {
        context = System.getProperty(UndertowProperties.CONTEXT_PATH, DEFAULT_CONTEXT);
    }

    public ManagementConsoleFraction contextRoot(String context) {
        this.context = context;
        return this;
    }

    public String getContextRoot() {
        return context;
    }

    private final String DEFAULT_CONTEXT = "/console";

    private String context = DEFAULT_CONTEXT;

    static {
        InputStream in = ManagementConsoleFraction.class.getClassLoader().getResourceAsStream("management-console.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VERSION = props.getProperty("version", "unknown");
    }


}
