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
package org.wildfly.swarm.microprofile.faulttolerance.config;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class BulkheadConfig extends GenericConfig<Bulkhead> {

    public static final String VALUE = "value";

    public static final String WAITING_TASK_QUEUE = "waitingTaskQueue";

    public BulkheadConfig(Method method) {
        super(Bulkhead.class, method);
    }

    public BulkheadConfig(AnnotatedMethod<?> annotatedMethod) {
        super(Bulkhead.class, annotatedMethod);
    }

    @Override
    public void validate() {
        if (get(VALUE, Integer.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Bulkhead on " + getMethodInfo() + " : value shouldn't be lower than 0");
        }
        if (get(WAITING_TASK_QUEUE, Integer.class) < 1) {
            throw new FaultToleranceDefinitionException("Invalid Bulkhead on " + getMethodInfo() + " : waitingTaskQueue shouldn't be lower than 1");
        }
    }

    @Override
    protected Class<Bulkhead> getConfigType() {
        return Bulkhead.class;
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = initKeys();

    private static Map<String, Class<?>> initKeys() {
        Map<String, Class<?>> keys = new HashMap<>();
        keys.put(VALUE, Integer.class);
        keys.put(WAITING_TASK_QUEUE, Integer.class);
        return Collections.unmodifiableMap(keys);
    }

}
