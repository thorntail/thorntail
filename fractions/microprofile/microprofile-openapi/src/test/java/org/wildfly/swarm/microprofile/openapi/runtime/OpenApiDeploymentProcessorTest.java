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

package org.wildfly.swarm.microprofile.openapi.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.microprofile.openapi.deployment.OpenApiServletContextListener;
import org.wildfly.swarm.microprofile.openapi.runtime.app.HelloResource;
import org.wildfly.swarm.microprofile.openapi.runtime.app.TestApplication;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
public class OpenApiDeploymentProcessorTest {

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
     * Common test method.
     * @throws Exception
     */
    protected void doTest(
            Class modelReaderClass,
            String staticResource,
            boolean disableAnnotationScanning,
            Class filterClass,
            String expectedResource) throws Exception {

        System.setProperty(OASConfig.SCAN_DISABLE, "" + disableAnnotationScanning);
        System.setProperty(OASConfig.MODEL_READER, modelReaderClass != null ? modelReaderClass.getName() : "");
        System.setProperty(OASConfig.FILTER, filterClass != null ? filterClass.getName() : "");

        TestConfig cfg = new TestConfig();
        OpenApiConfig oaiConfig = new OpenApiConfigImpl(cfg);
        Archive archive = archive(staticResource);
        OpenApiDocument.INSTANCE.reset();
        OpenApiDeploymentProcessor processor = new OpenApiDeploymentProcessor(oaiConfig, archive);
        processor.process();
        new OpenApiServletContextListener(cfg).contextInitialized(null);

        String actual = OpenApiSerializer.serialize(OpenApiDocument.INSTANCE.get(), Format.JSON);
        String expected = loadResource(getClass().getResource(expectedResource));

        assertJsonEquals(expected, actual);
    }

    /**
     * Creates and returns the shrinkwrap archive for this test.
     */
    private Archive archive(String staticResource) {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class, "app.war");
        archive.addClass(TestApplication.class);
        archive.addClass(HelloResource.class);
        if (staticResource != null) {
            archive.addAsManifestResource(new UrlAsset(getClass().getResource(staticResource)), "openapi.json");
        }
        return archive;
    }


    /**
     * An implementation of the MP Config class that resolves config properties via System Properties.  Useful
     * for testing.
     * @author eric.wittmann@gmail.com
     */
    private static class TestConfig implements Config {

        /**
         * @see org.eclipse.microprofile.config.Config#getValue(java.lang.String, java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T> T getValue(String propertyName, Class<T> propertyType) {
            String value = System.getProperty(propertyName);
            if (value != null && value.trim().length() == 0) {
                value = null;
            }
            if (value == null) {
                throw new NoSuchElementException("Property value not found: " + propertyName);
            }
            if (propertyType.equals(String.class)) {
                return (T) value;
            }
            if (propertyType.equals(Boolean.class)) {
                return (T) new Boolean(value);
            }
            throw new IllegalArgumentException();
        }

        /**
         * @see org.eclipse.microprofile.config.Config#getOptionalValue(java.lang.String, java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
            String value = System.getProperty(propertyName);
            if (value != null && value.trim().length() == 0) {
                value = null;
            }
            if (propertyType.equals(String.class)) {
                return (Optional<T>) Optional.ofNullable(value);
            }
            if (propertyType.equals(Boolean.class)) {
                return (Optional<T>) Optional.ofNullable(new Boolean(value));
            }
            throw new IllegalArgumentException();
        }

        /**
         * @see org.eclipse.microprofile.config.Config#getPropertyNames()
         */
        @Override
        public Iterable<String> getPropertyNames() {
            return new HashSet<String>();
        }

        /**
         * @see org.eclipse.microprofile.config.Config#getConfigSources()
         */
        @Override
        public Iterable<ConfigSource> getConfigSources() {
            return new HashSet<ConfigSource>();
        }

    }

    public static class TestFilter implements OASFilter {
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

    public static class TestModelReader implements OASModelReader {

        /**
         * @see org.eclipse.microprofile.openapi.OASModelReader#buildModel()
         */
        @Override
        public OpenAPI buildModel() {
            OpenAPIImpl api = new OpenAPIImpl();
            api.setOpenapi("3.0.1");
            api.setInfo(new InfoImpl());
            api.getInfo().setTitle("Model Reader API");
            api.getInfo().setDescription("This is an API created by the Model Reader.");
            api.getInfo().setContact(new ContactImpl());
            api.getInfo().getContact().name("API Support").url("http://www.example.com/support").email("support@example.com");
            api.getInfo().setVersion("1.17");
            return api;
        }

    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.OpenApiDeploymentProcessor#process()}.
     */
    @Test
    public void testProcess_Filtered() throws Exception {
        doTest(null, "_filtered/static.json", true, TestFilter.class, "_filtered/_expected.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.OpenApiDeploymentProcessor#process()}.
     */
    @Test
    public void testProcess_ModelReader() throws Exception {
        doTest(TestModelReader.class, null, true, null, "_modelReader/_expected.json");
    }

    /**
     * Test method for {@link org.wildfly.swarm.microprofile.openapi.runtime.OpenApiDeploymentProcessor#process()}.
     */
    @Test
    public void testProcess_Static() throws Exception {
        doTest(null, "_static/static.json", true, null, "_static/_expected.json");
    }

}
