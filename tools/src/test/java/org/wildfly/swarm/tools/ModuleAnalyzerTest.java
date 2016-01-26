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
package org.wildfly.swarm.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ModuleAnalyzerTest {

    @Test
    public void testAnalysis() throws Exception {
        InputStream moduleXml = getClass().getClassLoader().getResourceAsStream("module.xml");
        ModuleAnalyzer analyzer = new ModuleAnalyzer(moduleXml);
        assertThat(analyzer.getDependencies()).hasSize(2);
        List<String> gavs = analyzer.getDependencies().stream().map(e -> e.mscGav()).collect(Collectors.toList());
        assertThat(gavs).contains("org.wildfly:wildfly-webservices-server-integration:10.0.0.CR4");
        assertThat(gavs).contains("org.jboss.ws.cxf:jbossws-cxf-resources:5.1.0.Final:wildfly1000");

        assertThat(analyzer.getDependencies().stream().allMatch(e -> e.shouldGather));
    }

    @Test
    public void testAvoidAliases() throws IOException {
        InputStream moduleXml = getClass().getClassLoader().getResourceAsStream("alias-module.xml");
        ModuleAnalyzer analyzer = new ModuleAnalyzer(moduleXml);
        assertThat(analyzer.getDependencies()).hasSize(0);
    }
}
