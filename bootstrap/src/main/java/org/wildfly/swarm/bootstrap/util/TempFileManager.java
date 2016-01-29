package org.wildfly.swarm.bootstrap.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Bob McWhirter
 */
public class TempFileManager {

    public static final TempFileManager INSTANCE = new TempFileManager();

    private Set<File> registered = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private TempFileManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            INSTANCE.close();
        }));
    }

    public File newTempDirectory(String base, String ext) throws IOException {
        File tmp = File.createTempFile(base, ext);
        tmp.delete();
        tmp.mkdirs();
        tmp.deleteOnExit();
        register(tmp);
        return tmp;
    }

    public File newTempFile(String base, String ext) throws IOException {
        File tmp = File.createTempFile(base, ext);
        tmp.delete();
        tmp.deleteOnExit();
        register(tmp);
        return tmp;
    }

    private void register(File file) {
        this.registered.add(file);
    }

    private void close() {
        for (File file : registered) {
            deleteRecursively( file );
        }
    }

    private void deleteRecursively(File file) {
        if ( ! file.exists() ) {
            return;
        }
        if ( file.isDirectory() ) {
            for (File child : file.listFiles()) {
                deleteRecursively( child );
            }
        }

        file.delete();
    }

}
