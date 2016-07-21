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
package org.wildfly.swarm.container;

import javax.enterprise.inject.Vetoed;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class Interface {

    public Interface(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return this.name;
    }

    public String getExpression() {
        return this.expression;
    }

    private final String name;

    private final String expression;
}
