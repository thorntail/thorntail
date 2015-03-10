package org.wildfly.selfcontained;

import org.jboss.modules.ModuleClassLoader;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bob McWhirter
 */
public class Content {

    public static File CONTENT;

    public static void setup(ClassLoader cl) throws IOException {

        InputStream app = cl.getResourceAsStream("app/app.jar");

        File appFile = File.createTempFile( "app", ".jar" );

        byte[] buf = new byte[1024];
        int len = -1;

        FileOutputStream out = new FileOutputStream( appFile );

        while (  ( len = app.read( buf ) ) >= 0 ) {
            out.write( buf, 0, len );
        }

        app.close();
        out.close();

        CONTENT = appFile;
        System.err.println( "Set content to " + CONTENT );
    }
}
