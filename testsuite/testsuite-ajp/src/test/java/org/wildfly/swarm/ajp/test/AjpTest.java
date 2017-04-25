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
package org.wildfly.swarm.ajp.test;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.ajp.client.SimpleAjpClient;
import org.kohsuke.ajp.client.TesterAjpMessage;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
@DefaultDeployment
public class AjpTest {
    @Test
    @RunAsClient
    public void hello() throws IOException {
        SimpleAjpClient client = new SimpleAjpClient();
        client.connect("localhost", 8009);

        TesterAjpMessage message = client.createForwardMessage("/");
        message.end();

        client.sendMessage(message);

        assertThat(client.readMessage().readString()).contains("Hello on port 8009");

        client.disconnect();
    }
}
