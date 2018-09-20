package org.wildfly.swarm.arquillian.adapter;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.URLPackageScanner;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.wildfly.swarm.spi.api.DependenciesContainer;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.MarkerContainer;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

/**
 * @author Bob McWhirter
 */

public class DefaultDeploymentScenarioGenerator extends AnnotationDeploymentScenarioGenerator {

    @Override
    public List<DeploymentDescription> generate(TestClass testClass) {
        DefaultDeployment anno = testClass.getAnnotation(DefaultDeployment.class);

        if (anno == null) {
            return super.generate(testClass);
        }

        String classPrefix = (
                anno.type() == DefaultDeployment.Type.JAR
                        ? ""
                        : "WEB-INF/classes"
        );

        Archive<?> archive;
        if (DefaultDeployment.Type.WAR.equals(anno.type())) {
            WebArchive webArchive = ShrinkWrap.create(WebArchive.class, testClass.getJavaClass().getSimpleName() + ".war");
            // Add the marker to also include the project dependencies
            MarkerContainer.addMarker(webArchive, DependenciesContainer.ALL_DEPENDENCIES_MARKER);
            archive = webArchive;
        } else {
            archive = ShrinkWrap.create(JavaArchive.class, testClass.getJavaClass().getSimpleName() + ".jar");
        }

        ClassLoader cl = testClass.getJavaClass().getClassLoader();

        URLPackageScanner.Callback callback = (className, asset) -> {
            ArchivePath classNamePath = AssetUtil.getFullPathForClassResource(className);
            ArchivePath location = new BasicPath(classPrefix, classNamePath);
            archive.add(asset, location);
        };

        URLPackageScanner scanner = URLPackageScanner.newInstance(
                true,
                cl,
                callback,
                testClass.getJavaClass().getPackage().getName());

        scanner.scanPackage();

        try {
            List<URL> resources = Collections.list(cl.getResources(""));

            resources.stream()
                    .filter(e -> e.getProtocol().equals("file"))
                    .map(e -> getPlatformPath(e.getPath()))
                    .map(e -> Paths.get(e))
                    .filter(e -> Files.isDirectory(e))
                    .forEach(e -> {
                        try {
                            Files.walkFileTree(e, new SimpleFileVisitor<Path>() {
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    if (!file.toString().endsWith(".class")) {
                                        String location = javaSlashize(handleExceptionalCases(archive, e.relativize(file)));
                                        archive.add(new FileAsset(file.toFile()), location);
                                    }
                                    return super.visitFile(file, attrs);
                                }
                            });
                        } catch (IOException e1) {
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
        Map<ArchivePath, Node> content = archive.getContent();
        for (ArchivePath each : content.keySet()) {
            System.err.println(" --> " + each);
        }
        */

        DeploymentModules deploymentModules = testClass.getAnnotation(DeploymentModules.class);
        if (deploymentModules != null) {
            for (DeploymentModule each : deploymentModules.value()) {
                archive.as(JARArchive.class).addModule(each.name(), each.slot());
            }
        }

        DeploymentModule deploymentModule = testClass.getAnnotation(DeploymentModule.class);
        if (deploymentModule != null) {
            archive.as(JARArchive.class).addModule(deploymentModule.name(), deploymentModule.slot());
        }

        DeploymentDescription description = new DeploymentDescription(testClass.getName(), archive);

        Class<?> mainClass = anno.main();
        if (mainClass != Void.class) {
            archive.add(new StringAsset(mainClass.getName()), "META-INF/arquillian-main-class");
        }

        description.shouldBeTestable(anno.testable());

        return Collections.singletonList(description);
    }

    protected Path handleExceptionalCases(Archive archive, Path input) {
        if (archive.getName().endsWith(".war")) {
            String fileName = input.getFileName().toString();
            if (META_INF_SPECIAL_CASES.contains(fileName)) {
                return input;
            }
            if (fileName.startsWith("project-") && fileName.endsWith(".yml")) {
                return input;
            }
            if (input.getName(0).toString().equals("WEB-INF")) {
                return input;
            }
            return Paths.get("WEB-INF", "classes").resolve(input);
        }

        return input;
    }

    protected String getPlatformPath(String path) {
        if (!isWindows()) {
            return path;
        }

        URI uri = URI.create("file://" + path);
        return Paths.get(uri).toString();
    }

    protected boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public String javaSlashize(Path path) {

        List<String> parts = new ArrayList<>();

        int numParts = path.getNameCount();

        for (int i = 0; i < numParts; ++i) {
            parts.add(path.getName(i).toString());
        }


        return String.join("/", parts);

    }

    private static Set<String> META_INF_SPECIAL_CASES = new HashSet<String>() {{
        add("jboss-deployment-structure.xml");
        add("swarm.swagger.conf");
    }};
}
