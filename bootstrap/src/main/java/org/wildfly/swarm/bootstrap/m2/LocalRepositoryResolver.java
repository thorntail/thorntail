package org.wildfly.swarm.bootstrap.m2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Bob McWhirter
 */
public class LocalRepositoryResolver extends RepositoryResolver {
    @Override
    public File resolve(String gav) throws IOException {

        Path m2repo = findM2Repo();
        Path artifactPath = m2repo.resolve(gavToPath(gav, File.separator));

        if ( Files.notExists( artifactPath ) ) {
            return null;
        }

        return artifactPath.toFile();
    }

    private Path findM2Repo() {
        Path m2repo = Paths.get(System.getProperty("user.home"), ".m2", "repository");
        if (Files.notExists(m2repo) ) {
            String mavenHome = System.getenv( "MAVEN_HOME" );
            if ( mavenHome != null ) {
                m2repo = Paths.get( mavenHome, "repository" );
                if ( Files.notExists( m2repo ) ) {
                    return null;
                }
            }
        }

        return m2repo;
    }
}
