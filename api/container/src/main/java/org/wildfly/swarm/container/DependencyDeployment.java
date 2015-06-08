package org.wildfly.swarm.container;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.modules.MavenArtifactUtil;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.importer.zip.ZipImporterImpl;

/** A deployment that references a Maven dependency.
 *
 * <p>Allows deployment of project dependencies using simpley {@code groupId:artifactId}</p>
 *
 * @author Bob McWhirter
 */
public class DependencyDeployment implements Deployment {

    private final Archive archive;

    private final String name;

    /** Construct
     *
     * @param container The container.
     * @param gav The simplified groupId:artifactId of the dependency.
     * @throws Exception
     */
    public DependencyDeployment(Container container, String gav) throws Exception {
        this(container, gav, name(gav));
    }

    /** Construct
     *
     * @param container The container.
     * @param gav The simplified groupId:artifactId of the dependency.
     * @param name The explicit name for the deployment.
     * @throws Exception
     */
    public DependencyDeployment(Container container, String gav, String name) throws Exception {
        //String versionedGav = ProjectDependencies.getProjectDependencies().getVersionedGAV(gav);
        //this.file = ArtifactLoaderFactory.INSTANCE.getFile(versionedGav);
        this.archive = container.create(name, JavaArchive.class);
        new ZipImporterImpl(this.archive).importFrom(findFile(gav));
        this.name = name;
    }

    protected static String name(String gav) {
        String[] parts = gav.split(":");
        if (parts.length >= 2) {
            return parts[1] + ".jar";
        }

        return gav;
    }

    private static File findFile(String gav) throws IOException, ModuleLoadException {
        String[] parts = gav.split(":");

        if (parts.length < 2) {
            throw new RuntimeException("GAV must includes at least 2 segments");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = null;
        String classifier = "";

        if (parts.length > 3) {
            classifier = parts[3];
        }

        if (parts.length > 2) {
            version = parts[2];
        } else {
            version = determineVersionViaModule(groupId, artifactId, classifier);
        }

        if (version == null) {
            version = determineVersionViaClasspath(groupId, artifactId, classifier);
        }

        if (version == null) {
            throw new RuntimeException("Unable to determine version number from 2-part GAV.  Try three!");
        }

        return MavenArtifactUtil.resolveJarArtifact(groupId + ":" + artifactId + ":" + version + (classifier == null ? "" : ":" + classifier));
    }

    private static String determineVersionViaModule(String groupId, String artifactId, String classifier) throws ModuleLoadException {
        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("APP", "dependencies"));
        return module.getProperty("version." + groupId + ":" + artifactId + "::" + classifier);
    }

    private static String determineVersionViaClasspath(String groupId, String artifactId, String classifier) {

        String regexp = ".*" + artifactId + "-(.+)" + (classifier.length() == 0 ? "" : "-" + classifier) + ".jar";
        Pattern pattern = Pattern.compile(regexp);

        String classpath = System.getProperty("java.class.path");
        String[] elements = classpath.split(File.pathSeparator);

        for (int i = 0; i < elements.length; ++i) {
            Matcher matcher = pattern.matcher(elements[i]);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    @Override
    public Archive getArchive() {
        return this.archive;
    }
}
