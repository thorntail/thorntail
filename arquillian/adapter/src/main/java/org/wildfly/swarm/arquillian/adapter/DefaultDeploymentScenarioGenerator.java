package org.wildfly.swarm.arquillian.adapter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.impl.base.URLPackageScanner;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Bob McWhirter
 */

public class DefaultDeploymentScenarioGenerator extends AnnotationDeploymentScenarioGenerator {

    @Override
    public List<DeploymentDescription> generate(TestClass testClass) {
        DefaultDeployment anno = testClass.getAnnotation(DefaultDeployment.class);

        if ( anno == null ) {
            return super.generate(testClass);
        }

        JARArchive archive = ShrinkWrap.create(JARArchive.class, testClass.getJavaClass().getSimpleName() + ".jar");

        ClassLoader cl = testClass.getJavaClass().getClassLoader();

        Set<CodeSource> codeSources = new HashSet<>();

        URLPackageScanner.Callback callback = (className, asset) -> {
            ArchivePath classNamePath = AssetUtil.getFullPathForClassResource(className);
            ArchivePath location = new BasicPath("", classNamePath);
            archive.add(asset, location);

            try {
                Class<?> cls = cl.loadClass(className);
                codeSources.add(cls.getProtectionDomain().getCodeSource());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        };

        URLPackageScanner scanner = URLPackageScanner.newInstance(
                true,
                cl,
                callback,
                testClass.getJavaClass().getPackage().getName());

        scanner.scanPackage();

        Set<String> prefixes = codeSources.stream().map(e -> e.getLocation().toExternalForm()).collect(Collectors.toSet());

        try {
            List<URL> resources = Collections.list(cl.getResources(""));

            resources.stream()
                    .filter(e -> {
                        for (String prefix : prefixes) {
                            if (e.toExternalForm().startsWith(prefix)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .filter(e -> e.getProtocol().equals("file"))
                    .map(e -> e.getPath())
                    .map(e -> Paths.get(e))
                    .filter( e->Files.isDirectory( e ) )
                    .forEach(e -> {
                        try {
                            Files.walkFileTree(e, new SimpleFileVisitor<Path>() {
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    Path location = e.relativize(file);
                                    archive.add(new FileAsset(file.toFile()), location.toString());
                                    return super.visitFile(file, attrs);
                                }
                            });
                        } catch (IOException e1) {
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException( e );
        }

        DeploymentDescription description = new DeploymentDescription(testClass.getName(), archive);

        description.shouldBeTestable(anno.testable());

        return Collections.singletonList(description);
    }
}
