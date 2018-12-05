package org.wildfly.swarm.cdi.test.basic;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.junit.Test;
import org.wildfly.swarm.fractions.FractionUsageAnalyzer;
import org.wildfly.swarm.spi.api.JARArchive;

import javax.enterprise.inject.Vetoed;

@Vetoed
public class CDITest {

    @Test
    public void testFractionMatching() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource(EmptyAsset.INSTANCE,"WEB-INF/beans.xml");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        final File out = Files.createTempFile(archive.getName(), ".war").toFile();
        out.deleteOnExit();
        archive.as(ZipExporter.class).exportTo(out, true);
        analyzer.source(out);

        assertThat(analyzer.detectNeededFractions()
                       .stream()
                       .filter(fd -> fd.getArtifactId().equals("cdi"))
                       .count()).isEqualTo(1);
    }

    @Test
    public void testFractionMatchingWEBINFClasses() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource(EmptyAsset.INSTANCE,"WEB-INF/classes/META-INF/beans.xml");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        final File out = Files.createTempFile(archive.getName(), ".war").toFile();
        out.deleteOnExit();
        archive.as(ZipExporter.class).exportTo(out, true);
        analyzer.source(out);

        assertThat(analyzer.detectNeededFractions()
                       .stream()
                       .filter(fd -> fd.getArtifactId().equals("cdi"))
                       .count()).isEqualTo(1);
    }

    @Test
    public void testFractionMatchingMETAINF() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource(EmptyAsset.INSTANCE,"META-INF/beans.xml");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        final File out = Files.createTempFile(archive.getName(), ".war").toFile();
        out.deleteOnExit();
        archive.as(ZipExporter.class).exportTo(out, true);
        analyzer.source(out);

        assertThat(analyzer.detectNeededFractions()
                       .stream()
                       .filter(fd -> fd.getArtifactId().equals("cdi"))
                       .count()).isEqualTo(1);
    }
}
