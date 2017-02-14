package org.wildfly.swarm.bootstrap.env;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test {@link SystemDependencyResolution}
 *
 * @author Michael Fraefel
 */
public class SystemDependencyResolutionTest {


    @Test
    public void testResolve_withGradle() throws IOException {
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact1 = "test";
        String artifact2 = "test2";
        String version = "1.0";

        String classpathFile1 = buildGradleClassPathEntry(group, artifact1, version, packaging);
        String classpathFile2 = buildGradleClassPathEntry(group, artifact2, version, packaging);
        String classpath = classpathFile1 + File.pathSeparator + classpathFile2;
        System.setProperty("java.class.path", classpath);

        ApplicationEnvironment env = ApplicationEnvironment.get();
        env.getRemovableDependencies().add(group+":"+artifact1+":"+packaging+":"+version);

        //WHEN
        SystemDependencyResolution resolution = new SystemDependencyResolution();
        Set<String> result = resolution.resolve(Collections.emptyList());

        //THEN
        assertEquals(1,result.size());
        assertEquals(classpathFile2, result.iterator().next());

    }

    @Test
    public void testResolve_withMaven() throws IOException {
        String group = "org.wildfly.swarm";
        String packaging = "jar";
        String artifact1 = "test";
        String artifact2 = "test2";
        String version = "1.0";

        String classpathFile1 = buildMavenClassPathEntry(group, artifact1, version, packaging);
        String classpathFile2 = buildMavenClassPathEntry(group, artifact2, version, packaging);
        String classpath = classpathFile1 + File.pathSeparator + classpathFile2;
        System.setProperty("java.class.path", classpath);

        ApplicationEnvironment env = ApplicationEnvironment.get();
        env.getRemovableDependencies().add(group+":"+artifact1+":"+packaging+":"+version);

        //WHEN
        SystemDependencyResolution resolution = new SystemDependencyResolution();
        Set<String> result = resolution.resolve(Collections.emptyList());

        //THEN
        assertEquals(1,result.size());
        assertEquals(classpathFile2, result.iterator().next());

    }


    private String buildGradleClassPathEntry(String group, String artifact, String version, String packaging) {
        return "path" + File.separator + ".gradle" + File.separator + group + File.separator + artifact +
        File.separator + version + File.separator + "hash" + File.separator +artifact + "-" + version + "." + packaging;
    }

    private String buildMavenClassPathEntry(String group, String artifact, String version, String packaging) {
        return "path" + File.separator + ".m2" + File.separator + "repository" + File.separator + group.replace('.', File.separatorChar) + File.separator + artifact +
                File.separator + version + File.separator +artifact + "-" + version + "." + packaging;
    }

}
