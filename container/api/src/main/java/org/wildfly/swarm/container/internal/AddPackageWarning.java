package org.wildfly.swarm.container.internal;

import org.wildfly.swarm.bootstrap.util.BootstrapProperties;

/**
 * @author Bob McWhirter
 */
public class AddPackageWarning {

    public static void addPackage() {
        String type = System.getProperty(BootstrapProperties.DEFAULT_DEPLOYMENT_TYPE );
        if ( type == null ) {
            return;
        }

        if ( type.equals( "jar" ) ) {
            return;
        }

        throw new RuntimeException( "Archive.addPackage() variants do not work as expected for artifacts with .war packaging" );
    }
}
