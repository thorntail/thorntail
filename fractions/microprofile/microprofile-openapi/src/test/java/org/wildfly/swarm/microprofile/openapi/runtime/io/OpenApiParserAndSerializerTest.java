/**
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

package org.wildfly.swarm.microprofile.openapi.runtime.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer.Format;

/**
 * @author eric.wittmann@gmail.com
 */
public class OpenApiParserAndSerializerTest {

    /**
     * Loads a resource as a string (reads the content at the URL).
     * @param testResource
     * @throws IOException
     */
    private static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, "UTF-8");
    }

    /**
     * Compares two JSON strings.
     * @param expected
     * @param actual
     * @throws JSONException
     */
    private static void assertJsonEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(expected, actual, true);
    }

    /**
     * @param original
     * @param roundTrip
     */
    private static void assertYamlEquals(String original, String roundTrip) {
        Assert.assertEquals(normalizeYaml(original), normalizeYaml(roundTrip));
    }

    /**
     * Normalizes the YAML by removing any comments.
     * @param yaml
     */
    private static String normalizeYaml(String yaml) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(yaml));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("--")) {
                    continue;
                }
                builder.append(line);
                builder.append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a full round-trip parse+serialize test on a single resource.
     * @param resource
     * @param format
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    private static void doTest(String resource, Format format) throws IOException, ParseException, JSONException {
        URL testResource = OpenApiParserAndSerializerTest.class.getResource(resource);
        String original = loadResource(testResource);
        OpenAPIImpl impl = OpenApiParser.parse(testResource);
        String roundTrip = OpenApiSerializer.serialize(impl, format);

        try {
            if (format == Format.JSON) {
                assertJsonEquals(original, roundTrip);
            } else {
                assertYamlEquals(original, roundTrip);
            }
        } catch (AssertionError e) {
            System.out.println("================");
            System.out.println(roundTrip);
            System.out.println("================");
            throw e;
        }
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseSimplest() throws IOException, ParseException, JSONException {
        doTest("simplest.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseSimplestYaml() throws IOException, ParseException, JSONException {
        doTest("simplest.yaml", Format.YAML);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseInfo() throws IOException, ParseException, JSONException {
        doTest("info.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseInfoYaml() throws IOException, ParseException, JSONException {
        doTest("info.yaml", Format.YAML);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testExternalDocs() throws IOException, ParseException, JSONException {
        doTest("externalDocs.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testExtensions() throws IOException, ParseException, JSONException {
        doTest("extensions.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSecurity() throws IOException, ParseException, JSONException {
        doTest("security.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testServers() throws IOException, ParseException, JSONException {
        doTest("servers.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testServersYaml() throws IOException, ParseException, JSONException {
        doTest("servers.yaml", Format.YAML);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testTags() throws IOException, ParseException, JSONException {
        doTest("tags.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Callbacks() throws IOException, ParseException, JSONException {
        doTest("components-callbacks.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Empty() throws IOException, ParseException, JSONException {
        doTest("components-empty.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Examples() throws IOException, ParseException, JSONException {
        doTest("components-examples.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Headers() throws IOException, ParseException, JSONException {
        doTest("components-headers.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Links() throws IOException, ParseException, JSONException {
        doTest("components-links.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Parameters() throws IOException, ParseException, JSONException {
        doTest("components-parameters.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_RequestBodies() throws IOException, ParseException, JSONException {
        doTest("components-requestBodies.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Responses() throws IOException, ParseException, JSONException {
        doTest("components-responses.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Schemas() throws IOException, ParseException, JSONException {
        doTest("components-schemas.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_SecuritySchemes() throws IOException, ParseException, JSONException {
        doTest("components-securitySchemes.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_AllOperations() throws IOException, ParseException, JSONException {
        doTest("paths-all-operations.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_Empty() throws IOException, ParseException, JSONException {
        doTest("paths-empty.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetCallbacks() throws IOException, ParseException, JSONException {
        doTest("paths-get-callbacks.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetParameters() throws IOException, ParseException, JSONException {
        doTest("paths-get-parameters.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetRequestBodyContent() throws IOException, ParseException, JSONException {
        doTest("paths-get-requestBody-content.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetRequestBodyExample() throws IOException, ParseException, JSONException {
        doTest("paths-get-requestBody-example.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetRequestBody() throws IOException, ParseException, JSONException {
        doTest("paths-get-requestBody.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetResponseContent() throws IOException, ParseException, JSONException {
        doTest("paths-get-response-content.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetResponseHeaders() throws IOException, ParseException, JSONException {
        doTest("paths-get-response-headers.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetResponseLinks() throws IOException, ParseException, JSONException {
        doTest("paths-get-response-links.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetResponses() throws IOException, ParseException, JSONException {
        doTest("paths-get-responses.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetSecurity() throws IOException, ParseException, JSONException {
        doTest("paths-get-security.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_GetServers() throws IOException, ParseException, JSONException {
        doTest("paths-get-servers.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_Get() throws IOException, ParseException, JSONException {
        doTest("paths-get.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_Parameters() throws IOException, ParseException, JSONException {
        doTest("paths-parameters.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_Ref() throws IOException, ParseException, JSONException {
        doTest("paths-ref.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_Servers() throws IOException, ParseException, JSONException {
        doTest("paths-servers.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testPaths_Extensions() throws IOException, ParseException, JSONException {
        doTest("paths-with-extensions.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_Discriminator() throws IOException, ParseException, JSONException {
        doTest("schemas-discriminator.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_AdditionalProperties() throws IOException, ParseException, JSONException {
        doTest("schemas-with-additionalProperties.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_AllOf() throws IOException, ParseException, JSONException {
        doTest("schemas-with-allOf.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_Composition() throws IOException, ParseException, JSONException {
        doTest("schemas-with-composition.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_Example() throws IOException, ParseException, JSONException {
        doTest("schemas-with-example.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_ExternalDocs() throws IOException, ParseException, JSONException {
        doTest("schemas-with-externalDocs.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_MetaData() throws IOException, ParseException, JSONException {
        doTest("schemas-with-metaData.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSchemas_XML() throws IOException, ParseException, JSONException {
        doTest("schemas-with-xml.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testEverything() throws IOException, ParseException, JSONException {
        doTest("_everything.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testEverythingYaml() throws IOException, ParseException, JSONException {
        doTest("_everything.yaml", Format.YAML);
    }

}
