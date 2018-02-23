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

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.Test;
import org.wildfly.swarm.microprofile.openapi.runtime.entity.SpecialCaseTestContainer;

import java.io.IOException;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class SpecialCaseTests extends OpenApiDataObjectScannerTestBase {

    @Test
    public void testCollection_SimpleTerminalType() throws IOException, JSONException {
        String name = SpecialCaseTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "listOfString").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "special.simple.expected.json", result);
    }

    @Test
    public void testCollection_DataObjectList() throws IOException, JSONException {
        String name = SpecialCaseTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "ccList").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "special.dataObjectList.expected.json", result);
    }

    @Test
    public void testCollection_WildcardWithSuperBound() throws IOException, JSONException {
        String name = SpecialCaseTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "listSuperFlight").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "special.wildcardWithSuperBound.expected.json", result);
    }

    @Test
    public void testCollection_WildcardWithExtendBound() throws IOException, JSONException {
        String name = SpecialCaseTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "listExtendsFoo").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "special.wildcardWithExtendBound.expected.json", result);
    }


    @Test
    public void testCollection_Wildcard() throws IOException, JSONException {
        String name = SpecialCaseTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, "listOfAnything").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "special.wildcard.expected.json", result);
    }

}
