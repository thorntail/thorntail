package org.wildfly.swarm.bootstrap.m2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Bob McWhirter
 */
public class JarRepositoryResolver extends RepositoryResolver {

    @Override
    public File resolve(String gav) throws IOException {

        StringBuilder path = new StringBuilder();
        path.append( "m2repo" );
        path.append( SEPARATOR );
        path.append( gavToPath( gav ) );

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(path.toString());
        if ( in == null ) {
            return null;
        }

        try {
            File tmp = File.createTempFile(gav.replace(':', '~'), ".jar");
            tmp.deleteOnExit();

            FileOutputStream out = new FileOutputStream(tmp);

            try {
                byte[] buf = new byte[1024];
                int len = -1;

                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
            return tmp;
        } finally {
            in.close();
        }
    }
}
