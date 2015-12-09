package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaders;

/**
 * @author Bob McWhirter
 */
public class NestedJarResourceLoader {

    private static Map<String, File> exploded = new HashMap<>();

    public static ResourceLoader loaderFor(URL base, String rootPath, String loaderPath, String loaderName) throws IOException {

        //System.err.println( "** " + base + ", " + rootPath + ", " + loaderPath + ", " + loaderName );

        if (base.toExternalForm().startsWith("jar:file:")) {
            int endLoc = base.toExternalForm().indexOf(".jar!");
            if (endLoc > 0) {
                String jarPath = base.toExternalForm().substring(9, endLoc + 4);

                File exp = exploded.get(jarPath);

                if (exp == null) {
                    exp = File.createTempFile("module-jar", ".jar_d");
                    exp.delete();
                    exp.mkdirs();
                    exp.deleteOnExit();

                    JarFile jarFile = new JarFile(jarPath);

                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry each = entries.nextElement();

                        if (!each.isDirectory()) {
                            File out = new File(exp, each.getName());
                            out.getParentFile().mkdirs();
                            Files.copy(jarFile.getInputStream(each), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }

                String relativeRoot = base.toExternalForm().substring(endLoc + 5);
                File resourceRoot = new File( new File( exp, relativeRoot ), loaderPath );
                /*
                if ( resourceRoot.listFiles() != null ) {
                    System.err.println("@ " + resourceRoot + " --> " + Arrays.asList(resourceRoot.listFiles()));
                }
                */
                return ResourceLoaders.createFileResourceLoader(loaderName, resourceRoot );
            }
        }

        return ResourceLoaders.createFileResourceLoader(loaderPath, new File(rootPath));
    }

}
