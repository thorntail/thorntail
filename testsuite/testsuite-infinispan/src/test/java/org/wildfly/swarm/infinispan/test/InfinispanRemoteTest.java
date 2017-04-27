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
package org.wildfly.swarm.infinispan.test;

import java.util.concurrent.CountDownLatch;

import javax.naming.InitialContext;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.msc.service.ServiceRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
public class InfinispanRemoteTest {

    @ArquillianResource
    ServiceRegistry registry;

    @Test
    public void testBasic() throws Exception {
        //new CountDownLatch(1).await();
        CacheContainer cacheContainer =
                (CacheContainer) new InitialContext().lookup("java:jboss/infinispan/container/server");
        Cache<String,String> cache = cacheContainer.getCache("default");
        cache.put("ham", "biscuit");
        assertEquals("biscuit", cache.get("ham"));
    }
}
