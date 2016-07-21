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

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class DeploymentException extends Exception {

    public DeploymentException(String message) {
        this.archive = null;
    }

    public DeploymentException(Throwable rootCause) {
        super(rootCause);
        this.archive = null;
    }

    public DeploymentException(Archive<?> archive, Throwable rootCause) {
        super(rootCause);
        this.archive = archive;
    }

    public DeploymentException(Archive<?> archive, String message) {
        super(message);
        this.archive = archive;
    }

    public Archive<?> getArchive() {
        return this.archive;
    }

    private final Archive<?> archive;

}
