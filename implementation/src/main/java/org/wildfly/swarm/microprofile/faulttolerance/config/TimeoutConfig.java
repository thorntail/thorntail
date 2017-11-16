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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.Annotated;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class TimeoutConfig extends GenericConfig<Timeout> {

    public static final String VALUE = "value";

    public static final String UNIT = "unit";

    public TimeoutConfig(Timeout annotation,Method method) {
        super(annotation, method);
    }

    public TimeoutConfig(Annotated annotated) {
        super(annotated.getAnnotation(Timeout.class), annotated);
    }

    @Override
    public void validate() {
        if (get(VALUE, Long.class) < 0) {
            throw new FaultToleranceDefinitionException("Invalid Timeout on " + annotated.toString() + " : value shouldn't be lower than 0");
        }
    }

    @Override
    protected String getConfigType() {
        return "Timeout";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = initKeys();

    private static Map<String, Class<?>> initKeys() {
        Map<String, Class<?>> keys = new HashMap<>();
        keys.put(VALUE, Long.class);
        keys.put(UNIT, ChronoUnit.class);
        return Collections.unmodifiableMap(keys);
    }
}
