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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.wildfly.swarm.fractions.scanner.ClassAndPackageScanner;
import org.wildfly.swarm.fractions.scanner.FilePresenceScanner;
import org.wildfly.swarm.fractions.scanner.JarScanner;
import org.wildfly.swarm.fractions.scanner.Scanner;
import org.wildfly.swarm.fractions.scanner.WarScanner;
import org.wildfly.swarm.fractions.scanner.WebXmlDescriptorScanner;
import org.wildfly.swarm.spi.meta.PathSource;
import org.wildfly.swarm.spi.meta.FilePathSource;
import org.wildfly.swarm.spi.meta.FractionDetector;
import org.wildfly.swarm.spi.meta.SimpleLogger;
import org.wildfly.swarm.spi.meta.ZipPathSource;

/**
 * @author Bob McWhirter
 * @author Toby Crawley
 * @author Ken Finnigan
 */
public class FractionUsageAnalyzer {
    public FractionUsageAnalyzer() {
        this(FractionList.get());
    }

    public FractionUsageAnalyzer(final FractionList fractionList) {
        this.fractionList = fractionList;
    }

    public FractionUsageAnalyzer source(final Path source) {
        source(source.toFile());

        return this;
    }

    public FractionUsageAnalyzer source(final File source) {
        this.sources.add(source);

        return this;
    }

    public FractionUsageAnalyzer logger(final SimpleLogger log) {
        this.log = log;
        return this;
    }

    public FractionUsageAnalyzer testClass(String testClass) {
        this.testClass = testClass;

        try {
            ClassReader reader = new ClassReader(testClass);
            ArquillianClientAnnotationSeekingClassVisitor visitor = new ArquillianClientAnnotationSeekingClassVisitor();
            reader.accept(visitor, 0);

            if (visitor.isClient()) {
                removeTestClassFromScanning = true;
            }
        } catch (Exception e) {
        }

        return this;
    }

    public Collection<FractionDescriptor> detectNeededFractions() throws IOException {
        if (this.fractionList == null) {
            return Collections.emptySet();
        }

        Set<FractionDescriptor> detectedFractions;

        loadDetectorsAndScanners();

        sources.forEach(this::scanFile);

        Set<String> detectedFractionNames = detectors.stream()
                .filter(FractionDetector::wasDetected)
                .map(FractionDetector::artifactId)
                .collect(Collectors.toSet());

        if (sources.stream().anyMatch(e -> e.getName().endsWith(".war"))) {
            detectedFractionNames.add("undertow");
        }

        detectedFractions = this.fractionList.getFractionDescriptors()
                .stream()
                .filter(fd -> detectedFractionNames.contains(fd.getArtifactId()))
                .collect(Collectors.toSet());

        // Remove fractions that have an explicitDependency on each other
        Iterator<FractionDescriptor> it = detectedFractions.iterator();
        while (it.hasNext()) {
            FractionDescriptor descriptor = it.next();
            // Is set as a explicitDependency to any other descriptor? If so, remove it
            if (detectedFractions.stream().anyMatch(fd -> fd.getDependencies().contains(descriptor))) {
                it.remove();
            }
        }


        // Add container only if no fractions are detected, as they have a transitive explicitDependency to container
        if (detectedFractions.isEmpty()) {
            detectedFractions.add(this.fractionList.getFractionDescriptor(FractionDescriptor.THORNTAIL_GROUP_ID, "container"));
        }

        return detectedFractions;
    }

    private boolean isZipFile(File source) {
        if (source.isDirectory()) {
            return false;
        }

        return source.getName().endsWith(".jar") || source.getName().endsWith(".war") || source.getName().endsWith(".zip");
    }

    private void scanFile(File source) {
        if (isZipFile(source)) {
            fireScanner(suffix(source.getName()), zipFileScannerConsumer(source));
        } else {
            File basePathFile = source.getAbsoluteFile();
            Path basePath = null;
            if (basePathFile.isDirectory()) {
               basePath = basePathFile.toPath();
            }

            if (source.isDirectory()) {
                if (source.getName().endsWith(".war") || new File(source, "WEB-INF").exists()) {
                    fireScanner("war", explodedScannerConsumer(basePath, source));
                } else {
                    fireScanner("jar", explodedScannerConsumer(basePath, source));
                }
            } else {
                fireScanner(suffix(source.getName()), explodedScannerConsumer(basePath, source));
            }
        }
    }

    private Consumer<Scanner<?>> zipFileScannerConsumer(File source) {
        return s -> {
            try (ZipFile zip = new ZipFile(source)) {
                scanEntries(zip);
            } catch (IOException e) {
                log.error("", e);
            }
        };
    }

    private Consumer<Scanner<?>> explodedScannerConsumer(Path basePath, File source) {
        return s -> {
            try {
                scanEntries(source.toPath(), basePath);
            } catch (IOException e) {
                log.error("", e);
            }
        };
    }

    private void scanSource(final PathSource source) {
        if (this.removeTestClassFromScanning &&
                transformClassSource(source.getRelativePath()).equals(testClass)) {
            return;
        }

        final String suffix = suffix(source.getSource().getFileName().toString());

        Collection<FractionDetector<?>> validDetectors =
                detectors.stream()
                        .filter(d -> d.extensionToDetect().equals(suffix))
                        .filter(d -> !d.detectionComplete())
                        .collect(Collectors.toList());

        if (validDetectors.size() > 0) {
            fireScanner(suffix, s -> {
                try {
                    s.scan(source, convertDetectors(validDetectors), this::scanFile);
                } catch (IOException e) {
                    log.error("", e);
                }
            });
        }

    }

    private void scanSource(ZipEntry entry, ZipFile source) {
        if (this.removeTestClassFromScanning &&
                transformClassSource(entry.getName()).equals(testClass)) {
            return;
        }

        final String suffix = suffix(entry.getName());

        Collection<FractionDetector<?>> validDetectors =
                detectors.stream()
                        .filter(d -> d.extensionToDetect().equals(suffix))
                        .filter(d -> !d.detectionComplete())
                        .collect(Collectors.toList());

        if (validDetectors.size() > 0) {
            fireScanner(suffix, s -> {
                try {
                   s.scan(new ZipPathSource(source, entry), convertDetectors(validDetectors), this::scanFile);
                } catch (IOException e) {
                    log.error("", e);
                }
            });
        }
    }

    private void scanEntries(ZipFile source) throws IOException {
        final Enumeration<? extends ZipEntry> entries = source.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                scanSource(entry, source);
            }
        }
    }

    private void scanEntries(Path source, Path basePath) throws IOException {
        if (Files.isDirectory(source)) {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    scanSource(new FilePathSource(basePath, file.toFile()));
                    return super.visitFile(file, attrs);
                }
            });
        } else {
            scanSource(new FilePathSource(basePath, source.toFile()));
        }
    }

    private <T> Collection<FractionDetector<T>> convertDetectors(Collection<FractionDetector<?>> untypedDetectors) {
        Collection<FractionDetector<T>> detectors = new HashSet<>();

        untypedDetectors.forEach(d -> detectors.add(convert(d)));

        return detectors;
    }

    @SuppressWarnings("unchecked")
    private <T> FractionDetector<T> convert(FractionDetector<?> detector) {
        return (FractionDetector<T>) detector;
    }

    private void fireScanner(String suffix, Consumer<Scanner<?>> scannerConsumer) {
        List<Scanner<?>> scanners = this.scanners.stream()
                .filter(s -> s.extension().equals(suffix))
                .collect(Collectors.toList());

        scanners.forEach(scannerConsumer);
    }

    private String suffix(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private void loadDetectorsAndScanners() {
        if (detectorsLoaded) {
            return;
        }

        ServiceLoader<FractionDetector> detectorLoader = ServiceLoader.load(FractionDetector.class);
        detectorLoader.forEach(d -> detectors.add(d));

        scanners.add(new WarScanner());
        scanners.add(new JarScanner());
        scanners.add(new ClassAndPackageScanner());
        scanners.add(new WebXmlDescriptorScanner());
        scanners.add(new FilePresenceScanner());

        ClassAndPackageScanner.classesPackagesAlreadyDetected.clear();

        detectorsLoaded = true;
    }

    private String transformClassSource(String entry) {
        entry = entry.replace("WEB-INF/classes/", "");
        entry = entry.replace(".class", "");
        entry = entry.replace("/", ".");
        return entry;
    }

    private final List<File> sources = new ArrayList<>();

    private final FractionList fractionList;

    private Collection<FractionDetector<?>> detectors = new HashSet<>();

    private Collection<Scanner<?>> scanners = new HashSet<>();

    private boolean detectorsLoaded = false;

    private SimpleLogger log = new SimpleLogger() {
    };

    private String testClass;

    private boolean removeTestClassFromScanning;
}
