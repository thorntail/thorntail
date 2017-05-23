package org.wildfly.swarm.security.detect;

import org.wildfly.swarm.spi.meta.WebXmlFractionDetector;

/**
 * Created by bob on 4/12/17.
 */
public class SecurityWebXmlDetector extends WebXmlFractionDetector {

    public SecurityWebXmlDetector() {
        super();
    }

    @Override
    public String artifactId() {
        return "security";
    }

    @Override
    protected boolean doDetect() {
        super.doDetect();
        return this.webXMl.getAllSecurityConstraint().size() > 0 ||
                this.webXMl.getAllSecurityRole().size() > 0 ||
                this.webXMl.getAllLoginConfig().size() > 0;
    }
}
