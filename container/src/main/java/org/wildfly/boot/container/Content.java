package org.wildfly.boot.container;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class Content {

    public static File CONTENT;

    public static void setup() throws IOException {
        CONTENT = new File( System.getProperty( "wildfly.boot.app" ) );
    }
}
