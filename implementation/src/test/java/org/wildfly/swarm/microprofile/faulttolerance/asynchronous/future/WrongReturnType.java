package org.wildfly.swarm.microprofile.faulttolerance.asynchronous.future;

import javax.enterprise.context.Dependent;

import org.eclipse.microprofile.faulttolerance.Asynchronous;

@Dependent
public class WrongReturnType {

    @Asynchronous
    public int ping() {
        return 1;
    }

}
