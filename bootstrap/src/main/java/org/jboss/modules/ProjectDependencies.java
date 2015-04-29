package org.jboss.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class ProjectDependencies {

    public static ProjectDependencies PROJECT_DEPENDENCIES;

    private Set<String> gavs = new HashSet<>();

    public static ProjectDependencies initialize(InputStream txt) throws IOException {
        ProjectDependencies deps = new ProjectDependencies(txt);
        PROJECT_DEPENDENCIES = deps;
        return deps;
    }

    public static ProjectDependencies getProjectDependencies() {
        return PROJECT_DEPENDENCIES;
    }

    ProjectDependencies(InputStream txt) throws IOException {
        try {
            BufferedReader depsIn = new BufferedReader(new InputStreamReader(txt));

            String line = null;

            while ((line = depsIn.readLine()) != null) {
                gavs.add( line.trim() );
            }
        } finally {
            txt.close();
        }
    }

    public Set<String> getGAVs() {
        return this.gavs;
    }

    public String getVersionedGAV(String gav) {
        String[] parts = gav.split(":");
        if ( parts.length > 2 ) {
            return gav;
        }

        for ( String each : gavs ) {
            if ( each.startsWith( gav ) ) {
                return each;
            }
        }

        return null;
    }

}
