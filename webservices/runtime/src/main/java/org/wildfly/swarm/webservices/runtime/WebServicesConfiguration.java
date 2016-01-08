/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package org.wildfly.swarm.webservices.runtime;

import org.wildfly.swarm.container.runtime.Configuration;
import org.wildfly.swarm.container.runtime.MarshallingServerConfiguration;
import org.wildfly.swarm.webservices.WebServicesFraction;

@Configuration
public class WebServicesConfiguration extends MarshallingServerConfiguration<WebServicesFraction> {

    public static final String EXTENSION_MODULE = "org.jboss.as.webservices";

    public WebServicesConfiguration() {
        super(WebServicesFraction.class, EXTENSION_MODULE);
    }

    @Override
    public WebServicesFraction defaultFraction() {
        return WebServicesFraction.createDefaultFraction();
    }

}
