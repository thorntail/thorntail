package org.wildfly.swarm.it;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class AbstractIntegrationTest {

    protected Log getStdOutLog() throws Exception {
        return getLog( "target/stdout.log" );
    }

    protected Log getStdErrLog() throws Exception {
        return getLog( "target/stderr.log" );
    }

    protected Log getLog(String path) throws IOException {

        FileInputStream in = new FileInputStream( new File( path ) );

        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

        String line = null;

        List<String> lines = new ArrayList<>();

        while ( ( line = reader.readLine() ) != null ) {
            lines.add( line );
        }

        return new Log(lines);
    }

    public LogAssert assertThatLog(Log log) {
        return new LogAssert( log );
    }
}
