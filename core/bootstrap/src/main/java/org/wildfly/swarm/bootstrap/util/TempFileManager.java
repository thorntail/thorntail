/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.bootstrap.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Bob McWhirter
 */
public class TempFileManager {

    public static final String TMPDIR_PROPERTY = "swarm.io.tmpdir";

    public static final String WFSWARM_TMP_PREFIX = "wfswarm";

    public static final TempFileManager INSTANCE = new TempFileManager();

    private TempFileManager() {
        String tmpDir = System.getProperty(TMPDIR_PROPERTY);
        if (tmpDir != null) {
            this.tmpDir = new File(tmpDir);
            if (!Files.exists(this.tmpDir.toPath())) {
                try {
                    Files.createDirectories(this.tmpDir.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public File newTempDirectory(String base, String ext) throws IOException {
        File tmp = File.createTempFile(WFSWARM_TMP_PREFIX + base, ext, this.tmpDir);
        tmp.delete();
        tmp.mkdirs();
        register(tmp);
        return tmp;
    }

    public File newTempFile(String base, String ext) throws IOException {
        File tmp = File.createTempFile(WFSWARM_TMP_PREFIX + base, ext, this.tmpDir);
        tmp.delete();
        register(tmp);
        return tmp;
    }

    private void register(File file) {
        this.registered.add(file);
    }

    public synchronized void close() {
        registered.forEach(TempFileManager::deleteRecursively);
        registered.clear();
    }

    public static boolean deleteRecursively(File f) {
        if (!f.exists()) {
            return false;
        }

        try {
            Files.walkFileTree(f.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                        throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Set<File> registered = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private File tmpDir;

}
