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

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.wildfly.swarm.bootstrap.Main;

/**
 * @author Bob McWhirter
 */
public class UberJarManifest {

    public static final Attributes.Name WILDFLY_SWARM_MAIN_CLASS_ATTRIBUTE = new Attributes.Name( "WildFly-Swarm-Main-Class" );

    private final Manifest manifest;


    public UberJarManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public UberJarManifest(String mainClass) {
        this.manifest = new Manifest();

        Attributes attrs = this.manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, Main.class.getName() );
        if (mainClass != null && !mainClass.equals("")) {
            attrs.put(WILDFLY_SWARM_MAIN_CLASS_ATTRIBUTE, mainClass);
        }
    }

    public String getMainClassName() {
        if ( this.manifest == null )  {
            return null;
        }

        return (String) this.manifest.getMainAttributes().get(WILDFLY_SWARM_MAIN_CLASS_ATTRIBUTE);
    }

    public void write(OutputStream out) throws IOException {
        this.manifest.write(out);
    }
}
