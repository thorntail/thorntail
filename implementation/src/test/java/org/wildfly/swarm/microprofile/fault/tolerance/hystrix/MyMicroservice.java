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

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;


/**
 * @author Antoine Sabot-Durand
 */
@ApplicationScoped
public class MyMicroservice {

    @Timeout(500)
    public String sayHello() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    @Timeout(200)
    @Fallback(MyFallbackHandler.class)
    public String sayHelloWithFallback() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    @Timeout(200)
    public String sayHelloTimeoutNoFallback() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    @Asynchronous
    public Object sayHelloAsync() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    @Asynchronous
    @Timeout(200)
    @Fallback(MyFallbackHandler.class)
    public Object sayHelloAsyncTimeoutFallback() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    static final String HELLO = "Hello";
}
