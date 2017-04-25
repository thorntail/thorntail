package org.wildfly.swarm.servlet.ejb.test;

import javax.ejb.Singleton;

@Singleton
public class SimpleService {
    public String yay() {
        return "Yay! ";
    }
}
