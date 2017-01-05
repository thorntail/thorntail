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
package org.wildfly.swarm.tools.exec;

import java.util.ArrayList;
import java.util.List;

import org.wildfly.swarm.bootstrap.MainInvoker;

/**
 * @author Bob McWhirter
 */
public class MainClass implements Executable {

    public MainClass(String className) {
        this.className = className;
    }

    @Override
    public List<? extends String> toArguments() {
        List<String> args = new ArrayList<>();
        args.add(MainInvoker.class.getName());
        args.add(this.className);
        return args;
    }

    private final String className;
}
