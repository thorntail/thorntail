package org.wildfly.swarm.integration.ejb;

import javax.ejb.Stateless;

/**
 * @author Ken Finnigan
 */
@Stateless
public class GreeterEJB {

    public String message() {
        return "Howdy from EJB";
    }
}
