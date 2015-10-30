/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.bootstrap.modules.BootModuleLoader;
import org.wildfly.swarm.bootstrap.util.Layout;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Bob McWhirter
 */
public class Main {

    public static void main(String[] args) throws Throwable {
        System.setProperty("boot.module.loader", BootModuleLoader.class.getName());

        String mainClassName = null;
        Manifest manifest = Layout.getManifest();

        if (manifest != null) {
            mainClassName = (String) manifest.getMainAttributes().get(new Attributes.Name("Wildfly-Swarm-Main-Class"));
        }

        if (mainClassName == null) {
            mainClassName = "org.wildfly.swarm.Swarm";
        }

        Module app = Module.getBootModuleLoader().loadModule( ModuleIdentifier.create("swarm.application" ));

        Class<?> mainClass = app.getClassLoader().loadClass(mainClassName);

        final Method mainMethod = mainClass.getMethod("main", String[].class);

        final int modifiers = mainMethod.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new NoSuchMethodException("Main method is not static for " + mainClass);
        }

        try {
            mainMethod.invoke(null, new Object[]{args});
        } catch (Throwable e) {
            while ( e != null ) {
                e.printStackTrace();
                e = e.getCause();
            }
        }

    }
}
