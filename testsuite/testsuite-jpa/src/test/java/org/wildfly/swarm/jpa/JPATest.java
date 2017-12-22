package org.wildfly.swarm.jpa;

import java.io.File;
import java.nio.file.Files;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.fractions.FractionUsageAnalyzer;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

public class JPATest {

    @Test
    public void testFractionMatching() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource("META-INF/persistence.xml");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        File out = Files.createTempFile(archive.getName(), ".war").toFile();
        archive.as(ZipExporter.class).exportTo(out, true);

        analyzer.source(out);
        assertThat(analyzer.detectNeededFractions()
                       .stream()
                       .filter(fd -> fd.getArtifactId().equals("jpa"))
                       .count()).isEqualTo(1);

        out.delete();
    }

    @Test
    public void testFractionMatchingExploded() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource("META-INF/persistence.xml");
        FractionUsageAnalyzer analyzer = new FractionUsageAnalyzer();

        File dirFile = TempFileManager.INSTANCE.newTempDirectory("jpatest", null);
        archive.as(ExplodedExporter.class).exportExplodedInto(dirFile);

        analyzer.source(dirFile);
        assertThat(analyzer.detectNeededFractions()
                       .stream()
                       .filter(fd -> fd.getArtifactId().equals("jpa"))
                       .count()).isEqualTo(1);

        TempFileManager.deleteRecursively(dirFile);
    }
}