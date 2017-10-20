/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package test;

import java.io.File;
import java.net.URL;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.log.StreamModuleLogger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * module usage tests
 */
@Ignore("Experiments, not functional tests")
public class ModuleArtifactTest {
    @Test
    public void simpleClassFromArtifact() throws Exception {
        Module.setModuleLogger(new StreamModuleLogger(System.out));
        URL modules = getClass().getResource("/modules");
        String path = modules.getPath();
        System.setProperty("module.path", path);
        ModuleIdentifier id = ModuleIdentifier.create("test.jaxrs");
        Module testModule = Module.getBootModuleLoader().loadModule(id);
        URL c = testModule.getClassLoader().getResource("test/jaxrs/NoDeps.class");
        System.out.printf("NoDeps.class: %s\n", c);
        URL c1 = testModule.getClassLoader().getResource("javax/ws/rs/GET.class");
        System.out.printf("GET.class: %s\n", c1);
        Class<?> get = testModule.getClassLoader().loadClass("javax.ws.rs.GET");
        System.out.printf("Loaded GET: %s\n", get);

        URL c2 = testModule.getClassLoader().getResource("test/jaxrs/NoDeps.class");
        System.out.printf("NoDeps.class: %s\n", c2);
        Class<?> noDeps = testModule.getClassLoader().loadClass("test.jaxrs.NoDeps");
        System.out.printf("Loaded NoDeps: %s\n", noDeps);
        Class<?> extClass = testModule.getClassLoader().loadClass("test.jaxrs.Endpoint");
        System.out.printf("Loaded Endpoint: %s\n", extClass);
        extClass.newInstance();
    }
    @Test
    public void testClassFromArtifact() throws Exception {
        Module.setModuleLogger(new StreamModuleLogger(System.out));
        File root = new File("target/microprofile-jwt-auth-fraction-1.0.0-SNAPSHOT.jar");
        URL modules = getClass().getResource("/modules");
        String path = modules.getPath();
        System.setProperty("module.path", path);
        ModuleIdentifier id = ModuleIdentifier.create("org.wildfly.swarm.mpjwtauth", "runtime");
        Module mpjwtauthModule = Module.getBootModuleLoader().loadModule(id);
        Class<?> extClass = mpjwtauthModule.getClassLoader().loadClass("org.wildfly.swarm.mpjwtauth.deployment.auth.JWTAuthMethodExtension");
        System.out.printf("Loaded JWTAuthMethodExtension: %s\n", extClass);
        extClass.newInstance();
    }
}
