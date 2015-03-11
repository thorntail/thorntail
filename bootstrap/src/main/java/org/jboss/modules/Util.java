package org.jboss.modules;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * @author Ken Finnigan
 */
final public class Util {
    static public JarFile rootJar() throws IOException {
        URL url = Util.class.getResource(Util.class.getSimpleName() + ".class");
        String path = url.getPath();
        path = path.replace("file:", "");
        path = path.substring(0, path.indexOf(".jar") + 4);
        return new JarFile(path);
    }
}
