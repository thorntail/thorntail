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

package org.wildfly.swarm.microprofile.openapi.io;

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
import org.wildfly.swarm.microprofile.openapi.io.OpenApiParser;
import org.wildfly.swarm.microprofile.openapi.io.OpenApiSerializer;
import org.wildfly.swarm.microprofile.openapi.io.OpenApiSerializer.Format;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;

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
        return IOUtils.toString(testResource);
    }

    /**
     * Compares two JSON strings.
     * @param expected
     * @param actual
     * @throws JSONException
     */
    private static void assertJsonEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(expected, actual, false);
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

        if (format == Format.JSON) {
            assertJsonEquals(original, roundTrip);
        } else {
            assertYamlEquals(original, roundTrip);
        }
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseSimplest() throws IOException, ParseException, JSONException {
        doTest("simplest.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseSimplestYaml() throws IOException, ParseException, JSONException {
        doTest("simplest.yaml", Format.YAML);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseInfo() throws IOException, ParseException, JSONException {
        doTest("info.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testParseInfoYaml() throws IOException, ParseException, JSONException {
        doTest("info.yaml", Format.YAML);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testExternalDocs() throws IOException, ParseException, JSONException {
        doTest("externalDocs.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testExtensions() throws IOException, ParseException, JSONException {
        doTest("extensions.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testSecurity() throws IOException, ParseException, JSONException {
        doTest("security.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testServers() throws IOException, ParseException, JSONException {
        doTest("servers.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testServersYaml() throws IOException, ParseException, JSONException {
        doTest("servers.yaml", Format.YAML);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testTags() throws IOException, ParseException, JSONException {
        doTest("tags.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Callbacks() throws IOException, ParseException, JSONException {
        doTest("components-callbacks.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Empty() throws IOException, ParseException, JSONException {
        doTest("components-empty.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Examples() throws IOException, ParseException, JSONException {
        doTest("components-examples.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Headers() throws IOException, ParseException, JSONException {
        doTest("components-headers.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Links() throws IOException, ParseException, JSONException {
        doTest("components-links.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Parameters() throws IOException, ParseException, JSONException {
        doTest("components-parameters.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_RequestBodies() throws IOException, ParseException, JSONException {
        doTest("components-requestBodies.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Responses() throws IOException, ParseException, JSONException {
        doTest("components-responses.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_Schemas() throws IOException, ParseException, JSONException {
        doTest("components-schemas.json", Format.JSON);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.io.OpenApiParser#parse(java.net.URL)}.
     */
    @Test
    public void testComponents_SecuritySchemes() throws IOException, ParseException, JSONException {
        doTest("components-securitySchemes.json", Format.JSON);
    }



}
