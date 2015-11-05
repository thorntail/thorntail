/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package org.wildfly.swarm.webservices;

import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.undertow.UndertowFraction;

public class CreateSOAPInVmTest {
    @Test
    public void testSimple() throws Exception {
        new Container()
                .fraction(WebServicesFraction.createDefaultFraction())
                .fraction(UndertowFraction.createDefaultFraction())
                .start()
                .stop();
    }
}
