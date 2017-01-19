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
package org.wildfly.swarm.container.test;

import org.wildfly.swarm.Swarm;

/**
 * @author Heiko Braun
 */
public class Main {

    protected Main() {
    }

    /**
     * Test loading of project-stages.yml in arq test cases
     * See https://issues.jboss.org/browse/SWARM-486
     */
    public static void main(String...args) throws Exception {
        Swarm swarm = new Swarm(args)
                .component(ConfigViewInjectable.class);

        swarm.start().deploy();
    }

}
