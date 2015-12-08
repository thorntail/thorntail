/*
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.infinispan;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.container.JARArchive;

import javax.naming.InitialContext;

import static org.junit.Assert.assertEquals;

public class InfinispanTests {
    @Deployment
    public static Archive createDeployment() {
        return ShrinkWrap.create(JARArchive.class)
                .addModule("org.infinispan")
                .addClass(InfinispanTests.class);
    }

    @Test
    public void testBasic() throws Exception {
        CacheContainer cacheContainer =
                (CacheContainer) new InitialContext().lookup("java:jboss/infinispan/container/server");
        Cache cache = cacheContainer.getCache("default");
        cache.put("ham", "biscuit");
        assertEquals("biscuit", cache.get("ham"));
    }
}
