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

import static org.jboss.jandex.DotName.createSimple;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.microprofile.openapi.runtime.entity.Bar;
import org.wildfly.swarm.microprofile.openapi.runtime.entity.BuzzLinkedList;
import org.wildfly.swarm.microprofile.openapi.runtime.entity.EnumContainer;
import org.wildfly.swarm.microprofile.openapi.runtime.entity.GenericTypeTestContainer;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class ExpectationTests extends OpenApiDataObjectScannerTestBase {

    /**
     * Unresolvable type parameter.
     */
    @Test
    @Ignore
    public void testUnresolvable() throws IOException, JSONException {
        DotName bar = createSimple(Bar.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, ClassType.create(bar, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(bar.local(), result);
        assertJsonEquals(bar.local(), "unresolvable.expected.json", result);
    }

    /**
     * Unresolvable type parameter.
     */
    @Test
    public void testCycle() throws IOException, JSONException {
        DotName buzz = createSimple(BuzzLinkedList.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, ClassType.create(buzz, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(buzz.local(), result);
        assertJsonEquals(buzz.local(), "cycle.expected.json", result);
    }

    @Test
    public void testBareEnum() throws IOException, JSONException {
        DotName baz = createSimple(EnumContainer.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, ClassType.create(baz, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(baz.local(), result);
        assertJsonEquals(baz.local(), "enum.expected.json", result);
    }

    @Test
    public void testNestedGenerics() throws IOException, JSONException {
        String name = GenericTypeTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "nesting").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "generic.nested.expected.json", result);
    }

    @Test
    public void testComplexNestedGenerics() throws IOException, JSONException {
        String name = GenericTypeTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "complexNesting").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "generic.complexNesting.expected.json", result);
    }

    @Test
    public void testComplexInheritanceGenerics() throws IOException, JSONException {
        String name = GenericTypeTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "complexInheritance").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "generic.complexInheritance.expected.json", result);
    }

    @Test
    public void testGenericsWithBounds() throws IOException, JSONException {
        String name = GenericTypeTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "genericWithBounds").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "generic.withBounds.expected.json", result);
    }


    @Test
    public void genericFieldTest() throws IOException, JSONException {
        String name = GenericTypeTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "genericContainer").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "generic.fields.expected.json", result);
    }

}
