package io.thorntail.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by bob on 5/30/18.
 */
public class WarClassLoader extends URLClassLoader {

    public static WarClassLoader of(Path path, ClassLoader parent) throws IOException {
        Path dir = Files.createTempDirectory(path.getFileName().toString());

        try (JarFile jar = new JarFile(path.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry each = entries.nextElement();

                String fsPath = each.getName().replace('/', File.separatorChar);
                path = dir.resolve(fsPath);

                if (each.isDirectory()) {
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                } else {
                    Files.copy(jar.getInputStream(each), path);
                }
            }
        }

        Path webInf = dir.resolve("WEB-INF");

        Path webInfClasses = webInf.resolve("classes");
        Path webInfLib = webInf.resolve("lib");

        List<URL> urls = new ArrayList<>();

        urls.add(webInfClasses.toUri().toURL());

        Files.walkFileTree(webInfLib, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".jar")) {
                    urls.add(file.toUri().toURL());
                }
                return super.visitFile(file, attrs);
            }
        });

        return new WarClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    public WarClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
