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
import java.nio.file.Files;
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

    public void close() {
        for (File file : registered) {
            deleteRecursively(file);
        }
    }

    public static boolean deleteRecursively(File f) {
        if (!f.exists()) {
            return false;
        }
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            for (int i = 0; i < children.length; ++i) {
                if (!deleteRecursively(children[i])) {
                    return false;
                }
            }
        }

        return f.delete();
    }

    private Set<File> registered = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private File tmpDir;

}
