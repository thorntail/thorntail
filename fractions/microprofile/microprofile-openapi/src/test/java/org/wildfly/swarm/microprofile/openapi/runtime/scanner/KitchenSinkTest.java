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
package org.wildfly.swarm.microprofile.openapi.runtime.scanner;

import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.wildfly.swarm.microprofile.openapi.runtime.entity.KitchenSink;

import java.io.IOException;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class KitchenSinkTest extends OpenApiDataObjectScannerTestBase {

    private static final Logger LOG = Logger.getLogger(KitchenSinkTest.class);

    /**
     * Test to ensure scanner doesn't choke on various declaration types and patterns.
     *
     * This doesn't have any explicit assertions: it is designed to discover
     * any permutations or configurations that cause exceptions.
     *
     * It is to validate the scanner doesn't break rather than strictly assessing correctness.
     */
    @Test
    public void testKitchenSink() throws IOException {
        DotName kitchenSink = DotName.createSimple(KitchenSink.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index,
                ClassType.create(kitchenSink, Type.Kind.CLASS));

        LOG.debugv("Scanning top-level entity: {0}", KitchenSink.class.getName());
        printToConsole(kitchenSink.local(), scanner.process());
    }

    /**
     * Test parameterised type as a top-level entity (i.e. not just a bare class).
     *
     * @see org.jboss.jandex.ParameterizedType
     */
    @Test
    public void testTopLevelParameterisedType() throws IOException {
        // Look up the kitchen sink and get the field named "simpleParameterizedType"
        Type pType = getFieldFromKlazz(KitchenSink.class.getName(), "simpleParameterizedType").type();

        LOG.debugv("Scanning top-level entity: {0}", pType);
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);
        printToConsole("KustomPair", scanner.process());
    }

}
