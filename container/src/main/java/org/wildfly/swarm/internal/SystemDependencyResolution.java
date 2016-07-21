package org.wildfly.swarm.internal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.bootstrap.util.WildFlySwarmClasspathConf;

/**
 * @author Heiko Braun
 * @since 18/07/16
 */
@Vetoed
public class SystemDependencyResolution implements DependencyResolution {


    public SystemDependencyResolution() {
        final String classpathProp = System.getProperty("java.class.path");
        final String javaHomProp = System.getProperty("java.home");
        final String userDirProp = System.getProperty("user.dir");
        final String testClasspatProp = System.getProperty("swarm.test.dependencies");

        this.classpath = Arrays.asList(classpathProp.split(File.pathSeparator));
        this.testClasspath = testClasspatProp!=null ? Arrays.asList(testClasspatProp.split(File.pathSeparator)) : Collections.EMPTY_LIST;

        this.pwd = userDirProp;
        this.javaHome = javaHomProp.endsWith(JRE) ? javaHomProp.substring(0, javaHomProp.lastIndexOf(JRE)) : javaHomProp;
    }

    SystemDependencyResolution(List<String> classpath, String javaHome, String pwd, List<String> testClasspath) {
        this.classpath = classpath;
        this.pwd = pwd;
        this.javaHome = javaHome.endsWith(JRE) ? javaHome.substring(0, javaHome.lastIndexOf(JRE)) : javaHome;
        this.testClasspath = testClasspath;
    }

    @Override
    public Set<String> resolve(List<String> exclusions) throws IOException {

        final Set<String> archivesPaths = new HashSet<>();

        exclusions.replaceAll(s -> s.replace('.', File.separatorChar));

        if (classpath != null) {
            WildFlySwarmClasspathConf classpathConf = new WildFlySwarmClasspathConf();
            Set<String> classpathElements = new HashSet<>();
            Set<String> providedGAVs = new HashSet<>();
            List<String> testClasspathElements = testClasspath!=null ?
                    testClasspath : Collections.EMPTY_LIST;

            for (final String element : classpath) {
                if (!element.startsWith(javaHome) && !element.startsWith(pwd + File.separatorChar) && !element.endsWith(".pom")) {

                    // Read wildfly-swarm-classpath.conf entries
                    if(element.endsWith(JAR)) {
                        try (JarFile jar = new JarFile(new File(element))) {
                            ZipEntry entry = jar.getEntry(WildFlySwarmClasspathConf.CLASSPATH_LOCATION);
                            if (entry != null) {
                                classpathConf.read(jar.getInputStream(entry));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // explicit exclusions
                    if (!excluded(exclusions, element)) {
                        classpathElements.add(element);
                    }

                }
            }

            //  prepare the list of provided dep's, these will be implicitly excluded
            providedGAVs.addAll(
                    classpathConf.getMatchesForActionType(WildFlySwarmClasspathConf.MavenMatcher.class, WildFlySwarmClasspathConf.RemoveAction.class).stream()
                            .map(m -> (WildFlySwarmClasspathConf.MavenMatcher) m)
                            .map(m -> m.groupId + "." + m.artifactId)
                            .map(m -> m.replace('.', File.separatorChar))
                            .collect(Collectors.toList())
            );

            // implicit exclusions
            for (final String element : classpathElements) {
                if (!excluded(providedGAVs, element) && !excluded(testClasspathElements, element)) {
                    archivesPaths.add(element);
                }
            }
        }

        return archivesPaths;
    }

    private static final String JAR = ".jar";

    private static final String JRE = "jre";

    final List<String> classpath;
    final String javaHome;
    final String pwd;
    final List<String> testClasspath;
}
