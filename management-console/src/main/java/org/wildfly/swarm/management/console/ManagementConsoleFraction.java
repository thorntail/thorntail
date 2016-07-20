package org.wildfly.swarm.management.console;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.wildfly.swarm.management.ManagementFraction;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configuration;

/**
 * Created by ggastald on 02/06/16.
 */
@Configuration
public class ManagementConsoleFraction implements Fraction {

    public ManagementConsoleFraction() {
    }

    public ManagementConsoleFraction contextRoot(String context) {
        this.context = context;
        return this;
    }

    public ManagementConsoleFraction inhibitStandaloneDeployment() {
        this.inhibitStandaloneDeployment = true;
    }

    public boolean isInhibitStandaloneDeployment() {
        return inhibitStandaloneDeployment;
    }

    @Override
    public void postInitialize(PostInitContext initContext) {
        ManagementFraction fraction = (ManagementFraction) initContext.fraction("Management");
        if (fraction != null) {
            fraction.httpInterfaceManagementInterface(iface -> iface.allowedOrigin("*").consoleEnabled(true));
        }
    }

    public String getContextRoot() {
        return context;
    }

    private final String DEFAULT_CONTEXT = "/console";

    private String context = DEFAULT_CONTEXT;
    private boolean inhibitStandaloneDeployment;
}
