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

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.skyscreamer.jsonassert.JSONAssert;
import org.wildfly.swarm.microprofile.openapi.api.models.ComponentsImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class OpenApiDataObjectScannerTestBase {

    private static final Logger LOG = Logger.getLogger(OpenApiDataObjectScannerTestBase.class);
    protected static Index index;

    @BeforeClass
    public static void createIndex() throws IOException {
        Indexer indexer = new Indexer();

        // Stand-in stuff
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/scanner/CollectionStandin.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/scanner/MapStandin.class");

        // Test samples
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/Foo.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/Bar.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/Bazzy.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/Baz.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/Ultimate.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/BuzzLinkedList.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/Fuzz.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/BazEnum.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/KustomPair.class");

        // Microprofile TCK classes
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Airline.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Booking.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/CreditCard.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Flight.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Airline.class");

        // Test containers
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/KitchenSink.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/EnumContainer.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/GenericTypeTestContainer.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/SpecialCaseTestContainer.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/entity/GenericFieldTestContainer.class");

        index = indexer.complete();
    }

    private static void index(Indexer indexer, String resName) throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resName);
        indexer.index(stream);
    }

    public static void printToConsole(String entityName, Schema schema) throws IOException {
        // Remember to set debug level logging.
        LOG.debug(schemaToString(entityName, schema));
        //System.out.println(schemaToString(entityName, schema));
    }

    public static String schemaToString(String entityName, Schema schema) throws IOException {
        Map<String, Schema> map = new HashMap<>();
        map.put(entityName, schema);
        OpenAPIImpl oai = new OpenAPIImpl();
        ComponentsImpl comp = new ComponentsImpl();
        comp.setSchemas(map);
        oai.setComponents(comp);
        return OpenApiSerializer.serialize(oai, OpenApiSerializer.Format.JSON);
    }

    public static void assertJsonEquals(String entityName, String expectedResource, Schema actual) throws JSONException, IOException {
        URL resourceUrl = OpenApiDataObjectScannerTestBase.class.getResource(expectedResource);
        JSONAssert.assertEquals(loadResource(resourceUrl), schemaToString(entityName, actual),  true);
    }

    public static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, "UTF-8");
    }

    public FieldInfo getFieldFromKlazz(String containerName, String fieldName) {
        ClassInfo container = index.getClassByName(DotName.createSimple(containerName));
        return container.field(fieldName);
    }

}
