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
package org.wildfly.swarm.microprofile.restclient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.client.ResponseProcessingException;

/**
 * Created by hbraun on 22.01.18.
 */
class ProxyInvocationHandler implements InvocationHandler {

    private Object target;

    public ProxyInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
           return  method.invoke(target,args);
        } catch (InvocationTargetException e) {

            if (e.getCause() instanceof ResponseProcessingException) {
                ResponseProcessingException rpe = (ResponseProcessingException) e.getCause();
                Throwable cause = rpe.getCause();
                if (cause instanceof RuntimeException) {
                    throw cause;
                }
            }

            throw e;
        }
    }
}
