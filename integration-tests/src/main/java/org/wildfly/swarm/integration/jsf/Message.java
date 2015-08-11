package org.wildfly.swarm.integration.jsf;

import javax.inject.Named;

/**
 * @author Ken Finnigan
 */
@Named
public class Message {
    public String say() {
        return "Hello from JSF";
    }
}
