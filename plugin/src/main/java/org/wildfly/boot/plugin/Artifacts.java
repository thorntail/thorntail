package org.wildfly.boot.plugin;

import org.apache.maven.artifact.Artifact;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class Artifacts {

    private static class ArtifactSpec {
        public String groupId;
        public String artifactId;

        public ArtifactSpec(String spec) {
            String[] parts = spec.split(":");
            this.groupId = parts[0];
            this.artifactId = parts[1];
        }

        public ArtifactSpec(Artifact artifact) {
            this.groupId = artifact.getGroupId();
            this.artifactId = artifact.getArtifactId();
        }

        public boolean equals(Object o) {
            if (!(o instanceof ArtifactSpec)) {
                return false;
            }

            return (this.groupId.equals(((ArtifactSpec) o).groupId) && this.artifactId.equals(((ArtifactSpec) o).artifactId));
        }

        @Override
        public int hashCode() {
            return this.groupId.hashCode()/2 + this.artifactId.hashCode()/2;
        }
    }

    private static Set<ArtifactSpec> includedArtifacts = new HashSet<>();
    private static Set<ArtifactSpec> excludedArtifacts = new HashSet<>();

    static {
        addInclusion( "org.wildfly.boot:wildfly-boot-container" );
        addInclusion( "org.wildfly.boot:wildfly-boot-core" );
        addInclusion( "org.wildfly.boot:wildfly-boot-web" );

        excludedArtifacts.add( new ArtifactSpec( "org.jboss.modules:jboss-modules" ) );
        excludedArtifacts.add( new ArtifactSpec( "org.wildfly.boot:wildfly-boot-bootstrap" ) );
    }

    public static void addInclusion(String spec) {
        System.err.println( "INCLUDE: " + spec );
        includedArtifacts.add( new ArtifactSpec(spec) );
    }

    public static boolean includeArtifact(Artifact artifact) {
        ArtifactSpec spec = new ArtifactSpec(artifact);
        return includedArtifacts.contains( spec ) && ! excludedArtifacts.contains(spec);
    }
}
