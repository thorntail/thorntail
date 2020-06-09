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
package org.wildfly.swarm.misc.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
public class CustomModuleTest {

    @Test
    public void testModuleDirectoryResources() throws ModuleLoadException, IOException {
        Module module = Module.getBootModuleLoader().loadModule("com.mycorp.mymodule");

        assertNotNull(module);

        ClassLoader cl = module.getClassLoader();
        assertNotNull(cl);

        URL resourceUrl = cl.getResource("subdir/stuff.txt");

        assertNotNull(resourceUrl);

        String content = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream()))) {
            content = String.join("\n", reader.lines().collect(Collectors.toList()));
        }

        assertEquals(content, "this is dir stuff.txt");
    }

    @Test
    public void testRootJarResources() throws ModuleLoadException, IOException {
        Module module = Module.getBootModuleLoader().loadModule("com.mycorp.mymodule");

        assertNotNull(module);

        ClassLoader cl = module.getClassLoader();
        assertNotNull(cl);

        URL resourceUrl = cl.getResource("root.txt");

        assertNotNull(resourceUrl);

        String content = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream()))) {
            content = String.join("\n", reader.lines().collect(Collectors.toList()));
        }

        assertEquals(content, "this is root.txt");
    }


    @Test
    public void testRootJarSubdirResources() throws ModuleLoadException, IOException {
        Module module = Module.getBootModuleLoader().loadModule("com.mycorp.mymodule");

        assertNotNull(module);

        ClassLoader cl = module.getClassLoader();
        assertNotNull(cl);

        URL resourceUrl = cl.getResource("another_subdir/stuff.txt");

        assertNotNull(resourceUrl);

        String content = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream()))) {
            content = String.join("\n", reader.lines().collect(Collectors.toList()));
        }

        assertEquals(content, "this is jar stuff.txt");
    }

}
