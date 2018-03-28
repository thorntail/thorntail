package org.jboss.unimbus.test.arquillian.impl.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Created by bob on 1/25/18.
 */
public class ClassLoaderUtil {

    private ClassLoaderUtil() {

    }

    public static ClassLoader of(Archive<?> archive) {
        List<Archive<?>> list = flatten(archive);
        return of(list);
    }

    public static ClassLoader of(List<Archive<?>> archive) {
        List<URL> urls = archive.stream().map(e -> {
            try {
                if (e.getContent().isEmpty()) {
                    return null;
                }
                Path tmp = Files.createTempFile(e.getName(), "tmp");
                tmp.toFile().deleteOnExit();
                e.as(ZipExporter.class).exportTo(tmp.toFile(), true);
                return tmp.toUri().toURL();
            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    static List<Archive<?>> flatten(Archive<?> archive) {
        Set<String> seenClasses = new HashSet<>();
        if (archive.getName().endsWith(".jar")) {
            return Collections.singletonList(archive);
        }

        List<Archive<?>> archives = new ArrayList<>();

        Map<ArchivePath, Node> webInfLibContents = archive.getContent((path) -> {
            String str = path.get();
            if (str.startsWith("/WEB-INF/lib/") && str.endsWith(".jar")) {
                return true;
            }
            return false;
        });

        webInfLibContents.entrySet().forEach(e -> {
            ArchivePath path = e.getKey();

            int lastSlashLoc = path.get().lastIndexOf('/');
            String name = path.get().substring(lastSlashLoc + 1);
            Asset asset = e.getValue().getAsset();

            JavaArchive lib = ShrinkWrap.create(JavaArchive.class, name);
            lib.as(ZipImporter.class).importFrom(asset.openStream());
            archives.add(lib);
            for (ArchivePath archivePath : lib.getContent().keySet()) {
                seenClasses.add(archivePath.get());
            }

        });

        Map<ArchivePath, Node> webInfClassesContents = archive.getContent((path) -> {
            String str = path.get();
            return str.startsWith("/WEB-INF/classes");
        });

        JavaArchive classes = ShrinkWrap.create(JavaArchive.class, "test-classes.jar");
        webInfClassesContents.entrySet().forEach(e -> {
            if (e.getValue().getAsset() != null) {
                String path = e.getKey().get().substring("/WEB-INF/classes".length());
                if (seenClasses.contains(path)) {
                    return;
                }
                seenClasses.add(path);
                classes.add(e.getValue().getAsset(), path);
            }
        });

        classes.add(EmptyAsset.INSTANCE, "/META-INF/beans.xml");

        Map<ArchivePath, Node> metaInfContents = archive.getContent((path) -> {
            String str = path.get();
            return str.startsWith("/META-INF/");
        });

        metaInfContents.entrySet().forEach(e -> {
            if (e.getValue().getAsset() != null) {
                classes.add(e.getValue().getAsset(), e.getKey());
            }

        });
        archives.add(classes);

        //System.err.println( "------------------DEPLOYMENT");
        //System.err.println(classes.toString(true));
        //System.err.println( "------------------DEPLOYMENT");

        return archives;
    }
}
