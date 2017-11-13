package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.asynchronous.future;

import javax.enterprise.context.Dependent;

import org.eclipse.microprofile.faulttolerance.Asynchronous;

@Dependent
public class WrongReturnType {

    @Asynchronous
    public int ping() {
        return 1;
    }

}
