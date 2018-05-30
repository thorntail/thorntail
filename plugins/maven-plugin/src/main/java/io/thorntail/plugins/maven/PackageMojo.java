package io.thorntail.plugins.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.thorntail.plugins.common.MainFinder;
import io.thorntail.plugins.common.Plan;
import io.thorntail.plugins.common.PlanExporter;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import static io.thorntail.plugins.common.CommonPlanFactory.bootClass;
import static io.thorntail.plugins.common.CommonPlanFactory.confDir;
import static io.thorntail.plugins.common.CommonPlanFactory.manifest;
import static io.thorntail.plugins.common.CommonPlanFactory.scripts;

/**
 * Created by bob on 2/12/18.
 */

@Mojo(
        name = "package",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class PackageMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!this.project.getPackaging().equals("jar") && !this.project.getPackaging().equals("war")) {
            getLog().info("Skipping " + this.project.getArtifactId() + " as packaging is not jar/war");
            return;
        }

        getLog().info("Processing: " + this.finalName);
        validateMode();
        validateFormat();
        determineMainClass();

        new Jandexer(getLog(), new File(this.project.getBuild().getOutputDirectory()));

        getLog().info("mode: " + this.mode);
        getLog().info("format: " + this.format);

        createBasePlans();

        Plan plan = null;
        if (isDirFormat() || isZipFormat()) {
            plan = this.dirPlan;
        } else {
            plan = this.jarPlan;
        }

        if (isFat()) {
            plan = MavenPlanFactory.application(plan, this.project);
        }

        try {
            PlanExporter.export(plan, getDestination());
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        if (this.attach && !isDirFormat()) {
            DefaultArtifact artifact = new DefaultArtifact(
                    this.project.getGroupId(),
                    this.project.getArtifactId(),
                    this.project.getVersion(),
                    this.project.getArtifact().getScope(),
                    this.format,
                    this.classifier,
                    new DefaultArtifactHandler(this.format)
            );
            artifact.setFile(getDestination().toFile());
            this.project.addAttachedArtifact(artifact);
        }
    }

    private Path getDestination() {
        Path target = Paths.get(this.project.getBuild().getDirectory());
        if (isDirFormat()) {
            return target.resolve(this.finalName + "-" + this.classifier);
        }
        if (isZipFormat()) {
            return target.resolve(this.finalName + "-" + this.classifier + ".zip");
        }
        return target.resolve(this.finalName + "-" + this.classifier + ".jar");
    }

    private void createBasePlans() {
        this.dependenciesPlan = MavenPlanFactory.dependencies(this.project);
        this.dirPlan = confDir(scripts(this.dependenciesPlan, this.mainClass));
        this.jarPlan = manifest(bootClass(this.dependenciesPlan), this.mainClass);
    }

    private void validateMode() throws MojoExecutionException {
        this.mode = this.mode.toLowerCase();
        if (isFat() || isThin()) {
            return;
        }
        throw new MojoExecutionException("mode must be 'fat' or 'thin'");
    }

    private void validateFormat() throws MojoExecutionException {
        this.format = this.format.toLowerCase();
        if (isDirFormat() || isJarFormat() || isZipFormat()) {
            return;
        }

        throw new MojoExecutionException("format must be 'dir', 'zip' or 'jar'");
    }

    private boolean isFat() {
        return this.mode.equals("fat");
    }

    private boolean isThin() {
        return this.mode.equals("thin");
    }

    private boolean isDirFormat() {
        return this.format.equals("dir");
    }

    private boolean isZipFormat() {
        return this.format.equals("zip");
    }

    private boolean isJarFormat() {
        return this.format.equals("jar");
    }

    private void determineMainClass() throws MojoFailureException {
        if (this.mainClass != null) {
            return;
        }

        try {
            List<String> candidates = new MainFinder(this.project.getArtifact().getFile()).search();
            if (candidates.isEmpty()) {
                getLog().info("Using default Thorntail main()");
                this.mainClass = "io.thorntail.Main";
            } else if (candidates.size() > 1) {
                throw new MojoFailureException("No 'mainClass' specified and several candidates detected");
            } else {
                this.mainClass = candidates.get(0);
                getLog().info("Using detected main() from " + this.mainClass);
            }
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "fat", property = "thorntail.mode")
    protected String mode;

    @Parameter(defaultValue = "dir", property = "thorntail.format")
    protected String format;

    @Parameter(defaultValue = "bin", property = "thorntail.classifier")
    protected String classifier;

    @Parameter(defaultValue = "${project.build.finalName}", property = "thorntail.finalName")
    protected String finalName;

    @Parameter(defaultValue = "true", property = "thorntail.attach")
    protected boolean attach;

    @Parameter(property = "thorntail.mainClass")
    protected String mainClass;

    private Path workDir;

    private Path target;

    private Plan dependenciesPlan;

    private Plan dirPlan;

    private Plan jarPlan;
}
