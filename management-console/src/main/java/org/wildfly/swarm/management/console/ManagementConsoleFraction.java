/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.management.console;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * Created by ggastald on 02/06/16.
 */
@Singleton
@DefaultFraction
public class ManagementConsoleFraction implements Fraction {

    public ManagementConsoleFraction() {
        contextRoot(DEFAULT_CONTEXT);
    }

    public ManagementConsoleFraction contextRoot(String context) {
        this.context = context;
        return this;
    }

    public String contextRoot() {
        return context;
    }

    private final String DEFAULT_CONTEXT = "/console";

    private String context = DEFAULT_CONTEXT;
}
