/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.timeout;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.RequestScoped;

import org.eclipse.microprofile.faulttolerance.Timeout;

@RequestScoped
public class TimingOutService {

    static final AtomicBoolean INTERRUPTED = new AtomicBoolean(false);

    @Timeout(500)
    public String someSlowMethod(int timeToSleep) {
        try {
            Thread.sleep(timeToSleep);
            throw new RuntimeException("Timeout did not interrupt");
        } catch (InterruptedException e) {
            INTERRUPTED.set(true);
        }
        return null;
    }

}
