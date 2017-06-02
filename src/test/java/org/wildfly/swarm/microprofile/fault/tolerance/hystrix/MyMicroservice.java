/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.fault.tolerance.inject.Asynchronous;
import org.eclipse.microprofile.fault.tolerance.inject.Fallback;
import org.eclipse.microprofile.fault.tolerance.inject.Timeout;


/**
 * @author Antoine Sabot-Durand
 */
@ApplicationScoped
public class MyMicroservice {

    @Asynchronous
    @Timeout(value = 7, unit = ChronoUnit.SECONDS)
    //@Fallback(handler = MyFallbackHandler.class)
    public Object sayHello() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello";
    }

    @Asynchronous
    @Timeout(value = 7, unit = ChronoUnit.SECONDS)
    @Fallback(MyFallbackHandler.class)
    public Object sayHelloWithFailback() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello";
    }
}
