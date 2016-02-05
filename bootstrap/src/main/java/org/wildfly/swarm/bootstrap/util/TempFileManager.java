/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
