package org.wildfly.swarm.jaxrs.ejb.test;

import javax.ejb.Singleton;

@Singleton
public class SimpleService {
    public String yay() {
        return "Yay! ";
    }
}
