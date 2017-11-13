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
package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class BulkheadConfig extends GenericConfig<Bulkhead> {

    public BulkheadConfig(Bulkhead annotation, Method method) {
        super(annotation, method);
    }

    public BulkheadConfig(Annotated a) {
        super(a.getAnnotation(Bulkhead.class),a);
    }

    @Override
    public void validate() {
        if(get(VALUE, Integer.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Bulkhead on "+ annotated.toString() +" : value shouldn't be lower than 0");
        }

        if(get(WAITING_TASK_QUEUE, Integer.class) < 1) {
            throw new FaultToleranceDefinitionException("Invalid Bulkhead on "+ annotated.toString() +" : waitingTaskQueue shouldn't be lower than 1");
        }

    }

    @Override
    protected String getConfigType() {
        return "Bulkhead";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }


    public static final String VALUE = "value";

    public static final String WAITING_TASK_QUEUE = "waitingTaskQueue";

    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {
        {
            put(VALUE, Integer.class);
            put(WAITING_TASK_QUEUE, Integer.class);
        }
    });

}
