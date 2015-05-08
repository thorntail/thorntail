package org.wildfly.swarm.bootstrap.m2;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public abstract class RepositoryResolver {

    public abstract File resolve(String gav) throws IOException;

    private static final String JANDEX_SUFFIX = "?jandex";

    protected Path gavToPath(String gav, String separator) {
        String[] parts = gav.split(":");
        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];
        String classifier = null;
        if (parts.length >= 4) {
            classifier = parts[3];
        }

        if (artifact.endsWith(JANDEX_SUFFIX)) {
            artifact = artifact.substring(0, artifact.length() - JANDEX_SUFFIX.length());
        }

        StringBuilder path = new StringBuilder();


        path.append( group.replaceAll( "\\.", separator ) );
        path.append( separator );
        path.append( artifact );
        path.append( separator );
        path.append( version );
        path.append( separator );
        path.append( artifact );
        path.append( "-" );
        path.append( version );

        if ( classifier != null && ! classifier.equals( "" ) ) {
            path.append( "-" ).append( classifier );
        }

        path.append(".jar");
        return Paths.get(path.toString());
    }

}
