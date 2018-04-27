package io.thorntail.plugins.maven;

import io.thorntail.plugins.common.Plan;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import io.thorntail.plugins.common.CommonPlanFactory;

/**
 * Created by bob on 2/13/18.
 */
public class MavenPlanFactory extends CommonPlanFactory {

    public static Plan dependencies(MavenProject project) {
        return CommonPlanFactory.dependencies( project.getArtifacts().stream().map(Artifact::getFile));
    }

    public static Plan application(Plan parent, MavenProject project) {
        return CommonPlanFactory.application(parent, project.getArtifact().getFile());
    }

}
