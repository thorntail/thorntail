package org.wildfly.swarm.jsf.test;

import javax.enterprise.inject.Model;

@Model
public class Hello {
    public String hello() {
        return "Hello from JSF";
    }
}
