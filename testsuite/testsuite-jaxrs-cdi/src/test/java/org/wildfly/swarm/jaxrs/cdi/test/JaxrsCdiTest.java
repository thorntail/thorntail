/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.jaxrs.cdi.test;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.BasicCookieStore;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
@DefaultDeployment
public class JaxrsCdiTest {
    private Executor executor1 = Executor.newInstance().use(new BasicCookieStore());
    private Executor executor2 = Executor.newInstance().use(new BasicCookieStore());
    private Request request = Request.Get("http://localhost:8080/hello?name=Ladicek");

    @Test
    @RunAsClient
    public void hello() throws IOException {
        sendRequestAndAssertCounter(executor1, 1);
        sendRequestAndAssertCounter(executor1, 2);
        sendRequestAndAssertCounter(executor2, 1);
        sendRequestAndAssertCounter(executor1, 3);
        sendRequestAndAssertCounter(executor2, 2);

        executor1.clearCookies();

        sendRequestAndAssertCounter(executor1, 1);
        sendRequestAndAssertCounter(executor2, 3);
    }

    private void sendRequestAndAssertCounter(Executor executor, int expectedCounter) throws IOException {
        String result = executor.execute(request).returnContent().asString();
        assertThat(result, containsString("Hello, Ladicek! Counter: " + expectedCounter));
    }
}
