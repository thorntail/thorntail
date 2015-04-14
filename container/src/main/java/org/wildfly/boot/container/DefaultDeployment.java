package org.wildfly.boot.container;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class DefaultDeployment implements Deployment {

    @Override
    public File getContent() throws IOException {
        return new File( System.getProperty( "wildfly.boot.app" ) );
    }
}
