package org.wildfly.swarm.bootstrap.env;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author Heiko Braun
 * @since 18/07/16
 */
public class SystemDependencyResolution implements DependencyResolution {


    public SystemDependencyResolution() {
        final String classpathProp = System.getProperty("java.class.path");
        final String javaHomProp = System.getProperty("java.home");
        final String userDirProp = System.getProperty("user.dir");
        final String testClasspatProp = System.getProperty("swarm.test.dependencies");

        //Dedect gradle cache
        this.useGradleRepo = classpathProp.contains(File.separator + ".gradle");

        this.classpath = Arrays.asList(classpathProp.split(File.pathSeparator));
        this.testClasspath = testClasspatProp != null ? Arrays.asList(testClasspatProp.split(File.pathSeparator)) : Collections.EMPTY_LIST;

        this.pwd = userDirProp;
        this.javaHome = javaHomProp.endsWith(JRE) ? javaHomProp.substring(0, javaHomProp.lastIndexOf(JRE)) : javaHomProp;
    }

    @Override
    public Set<String> resolve(List<String> exclusions) throws IOException {

        final Set<String> archivesPaths = new HashSet<>();

        exclusions.replaceAll(s -> s.replace('.', File.separatorChar));

        if (classpath != null) {
            ApplicationEnvironment env = ApplicationEnvironment.get();
            Set<String> classpathElements = new HashSet<>();
            Set<String> providedGAVs = new HashSet<>();
            List<String> testClasspathElements = testClasspath != null ? testClasspath : Collections.EMPTY_LIST;

            for (final String element : classpath) {
                if (!element.startsWith(javaHome) && !element.startsWith(pwd + File.separatorChar) && !element.endsWith(".pom")) {
                    // explicit exclusions
                    if (!excluded(exclusions, element)) {
                        classpathElements.add(element);
                    }
                }
            }

            //  prepare the list of provided dep's, these will be implicitly excluded
            providedGAVs.addAll(
                    env.getRemovableDependencies()
                            .stream()
                            .map(e -> e.split(":"))
                            .map(e -> e[0] + File.separatorChar + e[1] + File.separatorChar)
                            .map(m -> (useGradleRepo ? m : m.replace('.', File.separatorChar)))
                            .collect(Collectors.toList())
            );


            // implicit exclusions
            for (final String element : classpathElements) {
                boolean excludedByProvidedGAVs = excluded(providedGAVs, element);
                boolean excludedByTestClasspath = excluded(testClasspathElements, element);

                if (!excludedByProvidedGAVs && !excludedByTestClasspath) {
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

    private final boolean useGradleRepo;

}
