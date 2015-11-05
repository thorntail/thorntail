/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package org.wildfly.swarm.webservices;

import org.wildfly.swarm.container.RuntimeModuleProvider;

public class WebServicesRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.webservices";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
