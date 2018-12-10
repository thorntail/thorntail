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
package org.wildfly.swarm.spi.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class JBossDeploymentStructureAssetTest {

    @Test
    public void testEmpty() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        asset.addModule("com.mycorp", "main");

        List<Module> modules = asset.deploymentModules();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        Module module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
    }

    @Test
    public void testAdditive() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        asset.addModule("com.mycorp", "main");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        List<Module> modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        Module module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");

        asset2.addModule("com.mycorp.another", "api");

        assertThat(asset2.deploymentExclusions()).isNullOrEmpty();

        modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(2);
        assertThat(modules.get(0)).isNotNull();
        assertThat(modules.get(1)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");

        module = modules.get(1);
        assertThat(module.name()).isEqualTo("com.mycorp.another");
        assertThat(module.slot()).isEqualTo("api");
    }

    @Test
    public void testExclusion() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        asset.excludeModule("com.mycorp", "main");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        List<Module> modules = asset2.deploymentExclusions();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        Module module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");

        asset2.excludeModule("com.mycorp.more", "special");

        assertThat(asset2.deploymentModules()).isNullOrEmpty();

        modules = asset2.deploymentExclusions();
        assertThat(modules.size()).isEqualTo(2);
        assertThat(modules.get(0)).isNotNull();
        assertThat(modules.get(1)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");

        module = modules.get(1);
        assertThat(module.name()).isEqualTo("com.mycorp.more");
        assertThat(module.slot()).isEqualTo("special");
    }

    @Test
    public void testExportAndServices() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        Module module = asset.addModule("com.mycorp");
        module.withExport(true);
        module.withServices(Module.ServiceHandling.IMPORT);

        module = asset.addModule("com.mycorp.special", "sloty");
        module.withExport(false);

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        List<Module> modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(2);
        assertThat(modules.get(0)).isNotNull();
        assertThat(modules.get(1)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.IMPORT);

        module = modules.get(1);
        assertThat(module.name()).isEqualTo("com.mycorp.special");
        assertThat(module.slot()).isEqualTo("sloty");
        assertThat(module.export()).isFalse();
        assertThat(module.metaInf()).isNull();

        module = asset2.addModule("com.somecorp");
        module.withExport(false);

        modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(3);
        assertThat(modules.get(0)).isNotNull();
        assertThat(modules.get(1)).isNotNull();
        assertThat(modules.get(2)).isNotNull();

        module = modules.get(2);
        assertThat(module.name()).isEqualTo("com.somecorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isFalse();
    }

    @Test
    public void testExportAndMetaInf() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        Module module = asset.addModule("com.mycorp");
        module.withExport(true);
        module.withServices(Module.ServiceHandling.IMPORT);

        module = asset.addModule("com.mycorp.special", "sloty");
        module.withExport(false);
        module.withMetaInf("import");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        List<Module> modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(2);
        assertThat(modules.get(0)).isNotNull();
        assertThat(modules.get(1)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.IMPORT);

        module = modules.get(1);
        assertThat(module.name()).isEqualTo("com.mycorp.special");
        assertThat(module.slot()).isEqualTo("sloty");
        assertThat(module.export()).isFalse();
        assertThat(module.metaInf()).isEqualTo("import");
    }

    @Test
    public void testRejectAddingDuplicate() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        Module module = asset.addModule("com.mycorp", "main");
        module.withExport(true);

        List<Module> modules = asset.deploymentModules();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isTrue();

        asset.addModule("com.mycorp");

        modules = asset.deploymentModules();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isTrue();
    }

    @Test
    public void testRejectExcludingDuplicate() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();
        asset.excludeModule("com.mycorp", "main");

        List<Module> modules = asset.deploymentExclusions();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        Module module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");

        asset.excludeModule("com.mycorp");

        modules = asset.deploymentExclusions();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
    }

    @Test
    public void testDependencyAndExclusion() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();

        assertThat(asset.deploymentModules()).isNullOrEmpty();
        assertThat(asset.deploymentExclusions()).isNullOrEmpty();

        asset.excludeModule("com.mycorp", "main");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        List<Module> modules = asset2.deploymentExclusions();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        Module module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");

        asset2.excludeModule("com.mycorp.more", "special");

        assertThat(asset2.deploymentModules()).isNullOrEmpty();

        modules = asset2.deploymentExclusions();
        assertThat(modules.size()).isEqualTo(2);
        assertThat(modules.get(0)).isNotNull();
        assertThat(modules.get(1)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");

        module = modules.get(1);
        assertThat(module.name()).isEqualTo("com.mycorp.more");
        assertThat(module.slot()).isEqualTo("special");
    }

    @Test
    public void testImportPaths() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();

        assertThat(asset.deploymentModules()).isNullOrEmpty();
        assertThat(asset.deploymentExclusions()).isNullOrEmpty();

        Module module = asset.addModule("com.mycorp");
        module.withExport(true);
        module.withServices(Module.ServiceHandling.IMPORT);
        module.withImportIncludePath("**");
        module.withImportExcludePath("some/path");
        module.withImportExcludePath("another/path");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        List<Module> modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.IMPORT);
        assertThat(module.importIncludePaths().size()).isEqualTo(1);
        assertThat(module.importIncludePaths().get(0)).isEqualTo("**");
        assertThat(module.importExcludePaths().size()).isEqualTo(2);
        assertThat(module.importExcludePaths().get(0)).isEqualTo("some/path");
        assertThat(module.importExcludePaths().get(1)).isEqualTo("another/path");
    }

    @Test
    public void testExportPaths() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();

        assertThat(asset.deploymentModules()).isNullOrEmpty();
        assertThat(asset.deploymentExclusions()).isNullOrEmpty();

        Module module = asset.addModule("com.mycorp");
        module.withExport(true);
        module.withServices(Module.ServiceHandling.EXPORT);
        module.withExportIncludePath("a/path");
        module.withExportIncludePath("some/path");
        module.withExportExcludePath("**");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        List<Module> modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(1);
        assertThat(modules.get(0)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.EXPORT);
        assertThat(module.exportIncludePaths().size()).isEqualTo(2);
        assertThat(module.exportIncludePaths().get(0)).isEqualTo("a/path");
        assertThat(module.exportIncludePaths().get(1)).isEqualTo("some/path");
        assertThat(module.exportExcludePaths().size()).isEqualTo(1);
        assertThat(module.exportExcludePaths().get(0)).isEqualTo("**");
    }

    @Test
    public void testExportImportPaths() throws Exception {
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset();

        assertThat(asset.deploymentModules()).isNullOrEmpty();
        assertThat(asset.deploymentExclusions()).isNullOrEmpty();

        Module module = asset.addModule("com.mycorp");
        module.withExport(true);
        module.withServices(Module.ServiceHandling.IMPORT);
        module.withImportIncludePath("**");
        module.withImportExcludePath("some/path");
        module.withImportExcludePath("another/path");

        module = asset.addModule("com.mycorp", "slot");
        module.withExport(true);
        module.withServices(Module.ServiceHandling.EXPORT);
        module.withExportIncludePath("a/path");
        module.withExportIncludePath("some/path");
        module.withExportExcludePath("**");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        module = asset2.addModule("com.mycorp.more");
        module.withExportIncludePath("a/path");
        module.withExportIncludePath("some/path");
        module.withExportExcludePath("**");

        List<Module> modules = asset2.deploymentModules();
        assertThat(modules.size()).isEqualTo(3);
        assertThat(modules.get(0)).isNotNull();
        assertThat(modules.get(1)).isNotNull();
        assertThat(modules.get(2)).isNotNull();

        module = modules.get(0);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.IMPORT);
        assertThat(module.importIncludePaths().size()).isEqualTo(1);
        assertThat(module.importIncludePaths().get(0)).isEqualTo("**");
        assertThat(module.importExcludePaths().size()).isEqualTo(2);
        assertThat(module.importExcludePaths().get(0)).isEqualTo("some/path");
        assertThat(module.importExcludePaths().get(1)).isEqualTo("another/path");

        module = modules.get(1);
        assertThat(module.name()).isEqualTo("com.mycorp");
        assertThat(module.slot()).isEqualTo("slot");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.EXPORT);
        assertThat(module.exportIncludePaths().size()).isEqualTo(2);
        assertThat(module.exportIncludePaths().get(0)).isEqualTo("a/path");
        assertThat(module.exportIncludePaths().get(1)).isEqualTo("some/path");
        assertThat(module.exportExcludePaths().size()).isEqualTo(1);
        assertThat(module.exportExcludePaths().get(0)).isEqualTo("**");

        module = modules.get(2);
        assertThat(module.name()).isEqualTo("com.mycorp.more");
        assertThat(module.slot()).isEqualTo("main");
        assertThat(module.export()).isNull();
        assertThat(module.services()).isNull();
        assertThat(module.exportIncludePaths().size()).isEqualTo(2);
        assertThat(module.exportIncludePaths().get(0)).isEqualTo("a/path");
        assertThat(module.exportIncludePaths().get(1)).isEqualTo("some/path");
        assertThat(module.exportExcludePaths().size()).isEqualTo(1);
        assertThat(module.exportExcludePaths().get(0)).isEqualTo("**");
    }

    @Test
    public void testExistingFileInArchive() throws Exception {
        JBossDeploymentStructureAsset asset =
                new JBossDeploymentStructureAsset(this.getClass().getClassLoader().getResourceAsStream("jboss-deployment-structure.xml"));

        assertThat(asset.deploymentModules().size()).isEqualTo(1);
        Module module = asset.deploymentModules().get(0);
        assertThat(module.name()).isEqualTo("org.apache.httpcomponents");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.EXPORT);

        assertThat(asset.deploymentExclusions().size()).isEqualTo(1);
        Module exclusion = asset.deploymentExclusions().get(0);
        assertThat(exclusion.name()).isEqualTo("com.example.existing.module");
        assertThat(exclusion.slot()).isEqualTo("extra");

        JBossDeploymentStructureAsset asset2 = new JBossDeploymentStructureAsset(asset.openStream());

        module = asset2.deploymentModules().get(0);
        assertThat(module.name()).isEqualTo("org.apache.httpcomponents");
        assertThat(module.export()).isTrue();
        assertThat(module.services()).isEqualTo(Module.ServiceHandling.EXPORT);

        exclusion = asset2.deploymentExclusions().get(0);
        assertThat(exclusion.name()).isEqualTo("com.example.existing.module");
        assertThat(exclusion.slot()).isEqualTo("extra");
    }

    @Test
    public void testNoRepeatedModules() throws IOException {
        ClassLoaderAsset existing = new ClassLoaderAsset("jboss-deployment-structure.xml");
        JBossDeploymentStructureAsset asset = new JBossDeploymentStructureAsset(existing.openStream());

        asset.addModule("com.example.module");
        asset.excludeModule("com.example.another.module");

        for (int i = 0; i < 10; i++) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(asset.openStream()))) {
                String xml = reader.lines().collect(Collectors.joining("\n"));

                assertThat(countOccurences("<module.*? name=\"com.example.module\"", xml))
                        .as("Expected only 1 occurence of <module name=\"com.example.module\"")
                        .isEqualTo(1);
                assertThat(countOccurences("<module.*? name=\"com.example.another.module\"", xml))
                        .as("Expected only 1 occurence of <module name=\"com.example.another.module\"")
                        .isEqualTo(1);

                assertThat(countOccurences("<module.*? name=\"org.apache.httpcomponents\"", xml))
                        .as("Expected only 1 occurence of <module name=\"org.apache.httpcomponents\"")
                        .isEqualTo(1);
                assertThat(countOccurences("<module.*? name=\"com.example.existing.module\"", xml))
                        .as("Expected only 1 occurence of <module name=\"com.example.existing.module\"")
                        .isEqualTo(1);
            }
        }
    }

    private static int countOccurences(String regexp, String string) {
        Pattern pattern = Pattern.compile(regexp);
        int counter = 0;
        int index = 0;
        Matcher matcher = pattern.matcher(string);
        while (matcher.find(index)) {
            counter++;
            index = matcher.start() + 1;
        }
        return counter;
    }
}
