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
package org.wildfly.swarm.batch.jberet;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class Deployments {

    public static JARArchive createDefaultDeployment() {
        return ShrinkWrap.create(JARArchive.class)
                .addAsResource(BatchArquillianTest.class.getResource("/META-INF/batch-jobs/simple.xml"), "/META-INF/batch-jobs/simple.xml")
                .addClasses(Deployments.class, CountingItemReader.class, SimpleItemWriter.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
