package org.wildfly.swarm.jaxrs;

/**
 * @author Bob McWhirter
 */
public class CustomException extends Exception {

    public CustomException(String message) {
        super(message);
    }
}
