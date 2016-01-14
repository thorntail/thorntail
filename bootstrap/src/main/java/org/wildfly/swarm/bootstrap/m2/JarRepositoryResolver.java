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
package org.wildfly.swarm.bootstrap.m2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bob McWhirter
 */
public class JarRepositoryResolver extends RepositoryResolver {

    @Override
    public File resolve(String gav) throws IOException {

        StringBuilder path = new StringBuilder();
        path.append("m2repo");
        path.append(SEPARATOR);
        path.append(gavToPath(gav));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(path.toString());
        if (in == null) {
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
