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

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.api.util.FilterUtil;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer.Format;

/**
 * @author eric.wittmann@gmail.com
 */
public class FilterUtilTest {

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
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.api.util.FilterUtil#applyFilter(org.eclipse.microprofile.openapi.OASFilter, org.eclipse.microprofile.openapi.models.OpenAPI)}.
     * @throws Exception
     */
    @Test
    public void testApplyFilter() throws Exception {
        URL beforeUrl = FilterUtilTest.class.getResource("filter-before.json");
        URL afterUrl = FilterUtilTest.class.getResource("filter-after.json");

        OpenAPIImpl model = OpenApiParser.parse(beforeUrl);
        OASFilter filter = filter();

        model = (OpenAPIImpl) FilterUtil.applyFilter(filter, model);

        String actual = OpenApiSerializer.serialize(model, Format.JSON);
        String expected = loadResource(afterUrl);

        assertJsonEquals(expected, actual);
    }

    /**
     * Creates and returns the filter to use for the test.
     */
    private OASFilter filter() {
        return new OASFilter() {
            /**
             * @see org.eclipse.microprofile.openapi.OASFilter#filterOpenAPI(org.eclipse.microprofile.openapi.models.OpenAPI)
             */
            @Override
            public void filterOpenAPI(OpenAPI openAPI) {
                openAPI.getInfo().setLicense(null);
                openAPI.getInfo().setTitle("Updated API Title");
            }
            /**
             * @see org.eclipse.microprofile.openapi.OASFilter#filterPathItem(org.eclipse.microprofile.openapi.models.PathItem)
             */
            @Override
            public PathItem filterPathItem(PathItem pathItem) {
                if (pathItem.getRef() != null) {
                    return null;
                } else {
                    return pathItem;
                }
            }
        };
    }

}
