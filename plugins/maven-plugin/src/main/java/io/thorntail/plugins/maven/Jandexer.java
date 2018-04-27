package io.thorntail.plugins.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

/**
 * @author Ken Finnigan
 */
public class Jandexer {

    private static final String INDEX_NAME = "thorntail.idx";

    Jandexer(Log log, File classesDir) {
        this.log = log;
        this.classesDir = classesDir;
    }

    public void process() {
        final Indexer indexer = new Indexer();
        if (!this.classesDir.exists()) {
            return;
        }

        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(this.classesDir);
        scanner.scan();
        final String[] files = scanner.getIncludedFiles();

        for (final String file : files) {
            if (file.endsWith(".class")) {
                try (FileInputStream fis = new FileInputStream(new File(this.classesDir, file))) {
                    indexer.index(fis);
                } catch (IOException e) {
                    this.log.error(e.getMessage());
                }
            }
        }

        final File idx = new File(this.classesDir, "META-INF/" + INDEX_NAME);
        idx.getParentFile().mkdirs();

        FileOutputStream indexOut = null;
        try {
            indexOut = new FileOutputStream(idx);
            final IndexWriter writer = new IndexWriter(indexOut);
            final Index index = indexer.complete();
            writer.write(index);
        } catch (IOException e) {
            this.log.error(e.getMessage(), e);
        } finally {
            IOUtil.close(indexOut);
        }
    }

    private Log log;

    private File classesDir;

}
