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
package org.wildfly.swarm.fractions;

import java.io.File;
import java.nio.file.Files;
import java.time.temporal.TemporalField;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

public class FractionUsageAnalyzerTest {

    @After
    public void tearDown() {
        TempFileManager.INSTANCE.close();
    }

    @Test
    public void testFractionMatching() throws Exception {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addClass(MyResource.class);
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        final File out = Files.createTempFile(archive.getName(), ".war").toFile();
        archive.as(ZipExporter.class).exportTo(out, true);

        analyzer.source(out);
        assertThat(analyzer.detectNeededFractions()
                           .stream()
                           .filter(fd -> fd.getArtifactId().equals("jaxrs"))
                           .count())
                .isEqualTo(1);

        out.delete();
    }

    @Test
    public void testExplodedFractionMatching() throws Exception {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addClass(MyResource.class);
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        File dirFile = TempFileManager.INSTANCE.newTempDirectory("fractionusagetest", null);
        archive.as(ExplodedExporter.class).exportExplodedInto(dirFile);

        analyzer.source(dirFile);
        assertThat(analyzer.detectNeededFractions()
                           .stream()
                           .filter(fd -> fd.getArtifactId().equals("jaxrs"))
                           .count())
                .isEqualTo(1);
    }

    @Test
    public void testDetectEmptyWarAsUndertow() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.war");
        archive.add(EmptyAsset.INSTANCE, "nothing");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        final File out = Files.createTempFile(archive.getName(), ".war").toFile();
        archive.as(ZipExporter.class).exportTo(out, true);

        analyzer.source(out);
        assertThat(analyzer.detectNeededFractions()
                           .stream()
                           .filter(fd -> fd.getArtifactId().equals("undertow"))
                           .count())
                .isEqualTo(1);

        out.delete();
    }
}
