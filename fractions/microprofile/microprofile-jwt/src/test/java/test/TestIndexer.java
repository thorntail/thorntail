package test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

public class TestIndexer {
    public static final String INDEX_NAME = "jandex.idx";

    protected IndexView loadIndex(InputStream in) throws IOException {
        IndexReader reader = new IndexReader(in);
        return reader.read();
    }
    protected IndexView loadDependentIndexFromArchive(File archive) throws IOException {
        try (JarFile jar = new JarFile(archive)) {

            ZipEntry entry = jar.getEntry("META-INF/" + INDEX_NAME);
            if (entry != null) {
                return loadIndex(jar.getInputStream(entry));
            }
            Indexer indexer = new Indexer();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry each = entries.nextElement();
                if (each.getName().endsWith(".class")) {
                    try (InputStream in = jar.getInputStream(each)) {
                        indexer.index(in);
                    }
                }
            }
            return indexer.complete();
        }
    }
    public IndexView load() throws IOException {
        //File jsonbFile = new File("/Users/starksm/repository/javax/json/javax.json-api/1.1/javax.json-api-1.1.jar");
        File jsonbFile = new File("/Users/starksm/.m2/repository/javax/json/javax.json-api/1.1/javax.json-api-1.1.jar");

        return loadDependentIndexFromArchive(jsonbFile);
    }
    public static void main(String[] args) throws IOException {
        TestIndexer test = new TestIndexer();
        IndexView indexView = test.load();
        System.out.println(indexView);
    }
}
