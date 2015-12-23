package org.wildfly.swarm.integration.staticcontent.war;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.arquillian.adapter.ArtifactDependencies;
import org.wildfly.swarm.integration.base.TestConstants;
import org.wildfly.swarm.undertow.WARArchive;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class StaticContentWarSubdirTest {
    @Drone
    WebDriver browser;

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.staticContent("foo");
        // Make sure we're testing from contents inside the jar only
        deployment.delete("WEB-INF/undertow-external-mounts.conf");
        return deployment;
    }

    @ArtifactDependencies
    public static List<String> appDependencies() {
        return Arrays.asList(
                "org.wildfly.swarm:undertow"
        );
    }

    @RunAsClient
    @Test
    public void testStaticContent() throws Exception {
        assertContains("", "This is foo/index.html.");
        assertContains("index.html", "This is foo/index.html.");
    }

    public void assertContains(String path, String content) throws Exception {
        browser.navigate().to(TestConstants.DEFAULT_URL + path);
        assertThat(browser.getPageSource()).contains(content);
    }
}
