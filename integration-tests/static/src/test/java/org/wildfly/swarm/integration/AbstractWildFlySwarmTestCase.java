package org.wildfly.swarm.integration;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.logging.LoggingFraction;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractWildFlySwarmTestCase {

    protected Container newContainer() throws Exception {
        return newContainer("INFO");
    }

    protected Container newContainer(String logLevel) throws Exception {

        return new Container()
                .fraction(new LoggingFraction()
                        .formatter("PATTERN", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n")
                        .consoleHandler(logLevel, "PATTERN")
                        .rootLogger("CONSOLE", logLevel));
    }

    protected String fetch(String urlStr) throws IOException {
        URL url = new URL( urlStr );
        StringBuffer buffer = new StringBuffer();
        try ( InputStream in = url.openStream() ) {
            int numRead = 0;
            while ( numRead >= 0 ) {
                byte[] b = new byte[1024];
                numRead = in.read(b);
                if (numRead < 0) {
                    break;
                }
                buffer.append( new String( b, 0, numRead ) );
            }
        }

        return buffer.toString();
    }


}
