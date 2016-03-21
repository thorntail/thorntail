package org.wildfly.swarm.monitor.runtime;

/**
 * @author Heiko Braun
 * @since 21/03/16
 */
public class HealthMetaData {
    private final boolean isSecure;
    private final String webContext;

    public HealthMetaData(String webContext, boolean isSecure) {
        this.webContext = webContext;
        this.isSecure = isSecure;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public String getWebContext() {
        return webContext;
    }
}
