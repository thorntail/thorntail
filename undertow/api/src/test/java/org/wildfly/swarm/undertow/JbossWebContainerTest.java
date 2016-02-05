package org.wildfly.swarm.undertow;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class JbossWebContainerTest {

    @Test
    public void testSettingContextRoot() throws Exception {
        WARArchive archive = DefaultWarDeploymentFactory.archiveFromCurrentApp();

        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("/");

        archive.setContextRoot("myRoot");
        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("myRoot");

        archive.setContextRoot("/someRoot");
        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("/someRoot");
    }

    @Test
    public void testDefaultContextRootWontOverride() throws Exception {
        WARArchive archive = DefaultWarDeploymentFactory.archiveFromCurrentApp();

        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("/");

        archive.setContextRoot("myRoot");
        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("myRoot");

        archive.setDefaultContextRoot();
        assertThat(archive.getContextRoot()).isNotNull();
        assertThat(archive.getContextRoot()).isEqualTo("myRoot");
    }
}
