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

package test.org.wildfly.swarm.microprofile.openapi;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.eclipse.microprofile.openapi.tck.FilterTest;
import org.jboss.arquillian.testng.Arquillian;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.openapi.api.OpenApiDocument;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer.Format;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.restassured.response.ValidatableResponse;

/**
 * Base class for all Tck tests.
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("restriction")
@RunWith(TckTestRunner.class)
public abstract class BaseTckTest {

    protected static final String APPLICATION_JSON = "application/json";
    protected static final String TEXT_PLAIN = "text/plain";

    private static HttpServer server;

    @BeforeClass
    public static final void setUp() throws Exception {
        // Set up a little HTTP server so that Rest assured has something to pull /openapi from
        System.out.println("Starting TCK test server on port 8080.");
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/openapi", new MyHandler());
        server.setExecutor(null);
        server.start();
    }

    @AfterClass
    public static final void tearDown() throws Exception {
        server.stop(0);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = null;
            try {
                response = OpenApiSerializer.serialize(OpenApiDocument.INSTANCE.get(), Format.JSON);
            } catch (Throwable e) {
                e.printStackTrace();
                t.getResponseHeaders().add("Content-Type", APPLICATION_JSON);
                OutputStream os = t.getResponseBody();
                os.write("{}".getBytes("UTF-8"));
                os.flush();
                os.close();
                return;
            }

            t.getResponseHeaders().add("Content-Type", APPLICATION_JSON);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /**
     * Calls the endpoint.
     * @param format
     */
    protected ValidatableResponse doCallEndpoint(String format) {
        ValidatableResponse vr;
        vr = given().accept(APPLICATION_JSON).when().get("/openapi").then().statusCode(200);
        return vr;
    }

    /**
     * Returns an instance of the TCK test being run.  The subclass must implement
     * this so that the correct test delegate is created *and* its callEndpoint()
     * method can be properly overridden.
     */
    public abstract Arquillian getDelegate();

    /**
     * Arguments to pass to each of the test methods in the TCK test.  This is
     * typically null (no arguments) but at least one test ( {@link FilterTest} )
     * has arguments to its methods.
     */
    public Object[] getTestArguments() {
        return null;
    }

}
