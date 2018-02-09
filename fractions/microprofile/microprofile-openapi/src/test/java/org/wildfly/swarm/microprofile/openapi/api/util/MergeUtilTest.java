/**
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

package org.wildfly.swarm.microprofile.openapi.api.util;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer.Format;

/**
 * @author eric.wittmann@gmail.com
 */
public class MergeUtilTest {

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
     * Performs a single full merge test.  Two documents are loaded (as resources) and then
     * merged.  The expected merge result is then loaded and compared with the actual result.
     * @param resource1
     * @param resource2
     * @param expected
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    private static void doTest(String resource1, String resource2, String expected) throws IOException, ParseException, JSONException {
        URL resource1Url = MergeUtilTest.class.getResource(resource1);
        URL resource2Url = MergeUtilTest.class.getResource(resource2);
        URL expectedUrl = MergeUtilTest.class.getResource(expected);

        String expectedContent = loadResource(expectedUrl);

        OpenAPIImpl resource1Model = OpenApiParser.parse(resource1Url);
        OpenAPIImpl resource2Model = OpenApiParser.parse(resource2Url);

        OpenAPIImpl actualModel = MergeUtil.merge(resource1Model, resource2Model);

        String actual = OpenApiSerializer.serialize(actualModel, Format.JSON);

        assertJsonEquals(expectedContent, actual);
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_Info() throws IOException, ParseException, JSONException {
        doTest("_info/info1.json", "_info/info2.json", "_info/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_Extensions() throws IOException, ParseException, JSONException {
        doTest("_extensions/extensions1.json", "_extensions/extensions2.json", "_extensions/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_Tags() throws IOException, ParseException, JSONException {
        doTest("_tags/tags1.json", "_tags/tags2.json", "_tags/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_Servers() throws IOException, ParseException, JSONException {
        doTest("_servers/servers1.json", "_servers/servers2.json", "_servers/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_PathDocs() throws IOException, ParseException, JSONException {
        doTest("_pathDocs/path1.json", "_pathDocs/path2.json", "_pathDocs/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_PathDefault() throws IOException, ParseException, JSONException {
        doTest("_pathDefault/pathDefault1.json", "_pathDefault/pathDefault2.json", "_pathDefault/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_Callbacks() throws IOException, ParseException, JSONException {
        doTest("_callbacks/callbacks1.json", "_callbacks/callbacks2.json", "_callbacks/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_Security() throws IOException, ParseException, JSONException {
        doTest("_security/security1.json", "_security/security2.json", "_security/merged.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil#merge(org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl, org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl)}.
     */
    @Test
    public void testMerge_OperationTags() throws IOException, ParseException, JSONException {
        doTest("_opTags/opTags1.json", "_opTags/opTags2.json", "_opTags/merged.json");
    }

}
