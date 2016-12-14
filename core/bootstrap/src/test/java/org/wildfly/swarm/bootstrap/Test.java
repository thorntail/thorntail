package org.wildfly.swarm.bootstrap;

import org.jboss.modules.maven.ArtifactCoordinates;

/**
 * @author Heiko Braun
 * @since 14/12/2016
 */
public class Test {
    public static void main(String[] args) throws Exception {
        String dep = "org.wildfly.swarm:request-controller:2017.1.0-20161214.105737-22";
        String[] parts = dep.split(":");
        ArtifactCoordinates coords = null;

        if ( parts.length == 4 ) {
            coords = new ArtifactCoordinates( parts[0], parts[1], parts[3] );
        } else if ( parts.length == 5 ) {
            coords = new ArtifactCoordinates( parts[0], parts[1], parts[3], parts[4] );
        }

        System.out.println(coords);
    }
}
