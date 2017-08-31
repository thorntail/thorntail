/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.management;

import org.wildfly.swarm.spi.api.annotations.Configurable;

/**
 * Created by bob on 5/22/17.
 */
@Configurable
public class InMemoryUserAuthentication {
    public InMemoryUserAuthentication() {
    }

    public InMemoryUserAuthentication password(String password) {
        this.password = password;
        return this;
    }

    public String password() {
        return this.password;
    }

    public InMemoryUserAuthentication hash(String hash) {
        this.hash = hash;
        return this;
    }

    public String hash() {
        return this.hash;
    }

    private String password;

    private String hash;
}
