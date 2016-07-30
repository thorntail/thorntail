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
package org.wildfly.swarm.cdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.wildfly.swarm.CommandLineArgs;
import org.wildfly.swarm.Swarm;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class CommandLineArgsFactory {

    private String[] args;
    private List<String> argsList;

    public CommandLineArgsFactory() {
        String[] args = Swarm.COMMAND_LINE_ARGS;
        if (args == null) {
            args = new String[]{};
        }
        this.args = args;
        this.argsList = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(this.args)));
    }

    @Produces
    @CommandLineArgs
    public String[] getArgsAsArray() {
        return this.args;
    }

    @Produces
    @CommandLineArgs
    public List<String> getArgsAsList() {
        return this.argsList;
    }
}
