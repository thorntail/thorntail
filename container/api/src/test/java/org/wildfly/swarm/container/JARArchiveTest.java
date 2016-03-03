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
package org.wildfly.swarm.container;

import java.util.List;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.wildfly.swarm.spi.api.JARArchive;
import org.junit.Test;
import org.wildfly.swarm.spi.api.JBossDeploymentStructureContainer;

import static org.fest.assertions.Assertions.assertThat;
import static org.wildfly.swarm.container.InputStreamHelper.read;

/**
 * @author Bob McWhirter
 */
public class JARArchiveTest {

    @Test
    public void testVirginJBossDeploymentStructure() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);

        archive.addModule("com.foo");
        archive.addModule("com.bar", "api");

        Node node = archive.get(JBossDeploymentStructureContainer.PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);

        assertThat(node).isNotNull();
        assertThat(node.getAsset()).isNotNull();

        List<String> lines = read(node.getAsset().openStream());

        assertThat(lines).contains("<module name=\"com.foo\" slot=\"main\"/>");
        assertThat(lines).contains("<module name=\"com.bar\" slot=\"api\"/>");

        archive = archive.as(JARArchive.class);

        node = archive.get(JBossDeploymentStructureContainer.PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);

        assertThat(node).isNotNull();
        assertThat(node.getAsset()).isNotNull();

        lines = read(node.getAsset().openStream());

        assertThat(lines).contains("<module name=\"com.foo\" slot=\"main\"/>");
        assertThat(lines).contains("<module name=\"com.bar\" slot=\"api\"/>");

    }


    @Test
    public void testExistingJBossDeploymentStructure() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);

        archive.add(new StringAsset(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<jboss-deployment-structure xmlns=\"urn:jboss:deployment-structure:1.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:jboss:deployment-structure:1.2 jboss-deployment-structure-1_2.xsd\">\n" +
                        "  <deployment>\n" +
                        "    <dependencies>\n" +
                        "      <module name=\"com.foo\" slot=\"main\"/>\n" +
                        "      <module name=\"com.bar\" slot=\"api\"/>\n" +
                        "    </dependencies>\n" +
                        "  </deployment>\n" +
                        "</jboss-deployment-structure>"
        ), JBossDeploymentStructureContainer.PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);

        archive.addModule("com.baz", "api");

        Node node = archive.get(JBossDeploymentStructureContainer.PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);

        assertThat(node).isNotNull();
        assertThat(node.getAsset()).isNotNull();

        List<String> lines = read(node.getAsset().openStream());

        assertThat(lines).contains("<module name=\"com.foo\" slot=\"main\"/>");
        assertThat(lines).contains("<module name=\"com.bar\" slot=\"api\"/>");
        assertThat(lines).contains("<module name=\"com.baz\" slot=\"api\"/>");

        archive = archive.as(JARArchive.class);

        node = archive.get(JBossDeploymentStructureContainer.PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);

        assertThat(node).isNotNull();
        assertThat(node.getAsset()).isNotNull();

        lines = read(node.getAsset().openStream());

        assertThat(lines).contains("<module name=\"com.foo\" slot=\"main\"/>");
        assertThat(lines).contains("<module name=\"com.bar\" slot=\"api\"/>");

    }

}
