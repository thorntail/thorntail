/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.datasources.test;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
@DefaultDeployment()
public class DatasourcesArquillianTest {

    @ArquillianResource
    InitialContext context;

    @Test
    public void testDatasource() throws NamingException {
        assert (context.lookup("java:jboss/datasources/MyDS") != null);
    }
}
