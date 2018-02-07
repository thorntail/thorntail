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
package org.wildfly.swarm.microprofile.openapi.runtime;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.junit.Test;
import org.wildfly.swarm.microprofile.openapi.api.models.ComponentsImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class OpenApiDataObjectScannerTest {

    @Test
    public void test() throws IOException {
        Indexer indexer = new Indexer();

        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/Foo.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/Bar.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/Fuzz.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/BazEnum.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/KustomPair.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/Booking2.class");
        index(indexer, "org/wildfly/swarm/microprofile/openapi/runtime/CreditCard2.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/CreditCard.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Flight.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Airline.class");

        Index index = indexer.complete();
        DotName name = DotName.createSimple(Booking2.class.getName());
        OpenApiDataObjectScanner foo = new OpenApiDataObjectScanner(index,
                (ClassType) ClassType.create(name, Type.Kind.CLASS));

        System.out.println("Calling top-level schema: " + Booking2.class.getName());
        printToConsole(Booking2.class.getSimpleName(), foo.process());
        // TODO add some expectations?
    }

    private void index(Indexer indexer, String resName) throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resName);
        indexer.index(stream);
    }

    private void printToConsole(String entityName, Schema schema) throws IOException {
        Map<String, Schema> map = new HashMap<>();
        map.put(entityName, schema);
        OpenAPIImpl oai = new OpenAPIImpl();
        ComponentsImpl comp = new ComponentsImpl();
        comp.setSchemas(map);
        oai.setComponents(comp);

//        Schema arrayThing = new SchemaImpl();
//        arrayThing.setType(Schema.SchemaType.ARRAY);
//        arrayThing.items(new SchemaImpl());
//        schema.addProperty("arrayThingie", arrayThing);
        System.out.println(OpenApiSerializer.serialize(oai, OpenApiSerializer.Format.JSON));
    }

}
