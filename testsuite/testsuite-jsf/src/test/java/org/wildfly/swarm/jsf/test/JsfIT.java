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
package org.wildfly.swarm.jsf.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.http.client.fluent.Request;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.junit.Test;
import org.wildfly.swarm.fractions.FractionUsageAnalyzer;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * This can't be a simple Arquillian test because for the WAR to fully build, the Maven WAR plugin needs to run.
 */
public class JsfIT {
    @Test
    public void jsf() throws IOException, InterruptedException {
        String result = Request.Get("http://localhost:8080/index.jsf").execute().returnContent().asString();
        assertThat(result).contains("Action message");
        assertThat(result).contains("Hello from JSF");
        assertThat(result).contains("Message from faces-config.xml bean");
        assertThat(result).contains("Message from custom-library.faces-config.xml bean");
    }

    @Test
    public void testFractionMatching() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource("WEB-INF/faces-config.xml");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        final File out = Files.createTempFile(archive.getName(), ".war").toFile();
        out.deleteOnExit();
        archive.as(ZipExporter.class).exportTo(out, true);
        analyzer.source(out);

        assertThat(analyzer.detectNeededFractions()
                       .stream()
                       .filter(fd -> fd.getArtifactId().equals("jsf"))
                       .count()).isEqualTo(1);
    }

    @Test
    public void testFractionMatchingMETAINF() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource("META-INF/faces-config.xml");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        final File out = Files.createTempFile(archive.getName(), ".war").toFile();
        out.deleteOnExit();
        archive.as(ZipExporter.class).exportTo(out, true);
        analyzer.source(out);
        assertThat(analyzer.detectNeededFractions()
                       .stream()
                       .filter(fd -> fd.getArtifactId().equals("jsf"))
                       .count()).isEqualTo(1);
    }
}
