/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.topology.runtime;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.topology.MyClass;
import org.wildfly.swarm.topology.MyRepeatingClass;
import org.wildfly.swarm.topology.TopologyArchive;

import static org.fest.assertions.Assertions.*;

/**
 * @author Bob McWhirter
 */
public class AdvertisingMetadataProcessorTest {

    @Test
    public void testNoAnnotations() throws IOException {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);

        AdvertisingMetadataProcessor processor = new AdvertisingMetadataProcessor(archive, createIndex(archive));
        processor.process();

        List<String> advertisements = archive.as(TopologyArchive.class).advertisements();

        assertThat(advertisements).isEmpty();
    }

    @Test
    public void testWithSingleAnnotation() throws IOException {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);

        archive.addClass(MyClass.class);

        AdvertisingMetadataProcessor processor = new AdvertisingMetadataProcessor(archive, createIndex(archive));
        processor.process();

        List<String> advertisements = archive.as(TopologyArchive.class).advertisements();

        assertThat(advertisements).hasSize(1);
        assertThat(advertisements).contains("foo");
    }

    @Test
    public void testWithRepeatingAnnotation() throws IOException {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);

        archive.addClass(MyRepeatingClass.class);

        AdvertisingMetadataProcessor processor = new AdvertisingMetadataProcessor(archive, createIndex(archive));
        processor.process();

        List<String> advertisements = archive.as(TopologyArchive.class).advertisements();

        assertThat(advertisements).hasSize(2);
        assertThat(advertisements).contains("cheddar");
        assertThat(advertisements).contains("gouda");
    }

    Index createIndex(Archive<?> archive) throws IOException {
        Indexer indexer = new Indexer();

        Map<ArchivePath, Node> c = archive.getContent();
        for (Map.Entry<ArchivePath, Node> each : c.entrySet()) {
            if (each.getKey().get().endsWith(".class")) {
                indexer.index(each.getValue().getAsset().openStream());
            }
        }


        return indexer.complete();
    }
}
