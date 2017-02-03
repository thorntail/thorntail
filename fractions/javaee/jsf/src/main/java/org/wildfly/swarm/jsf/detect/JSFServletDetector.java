package org.wildfly.swarm.jsf.detect;

import org.wildfly.swarm.spi.meta.WebXmlFractionDetector;

/**
 * @author Ken Finnigan
 */
public class JSFServletDetector extends WebXmlFractionDetector {

    public JSFServletDetector() {
        super();
        hasServlet("javax.faces.webapp.FacesServlet");
    }

    @Override
    public String artifactId() {
        return "jsf";
    }
}
