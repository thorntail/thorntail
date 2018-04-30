package org.wildfly.swarm.container.runtime;

import java.lang.reflect.Field;

import javax.enterprise.inject.Instance;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.wildfly.swarm.container.config.ConfigViewImpl;
import org.wildfly.swarm.spi.api.ArtifactLookup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArtifactDeployerTest {

    @Test
    public void testDeploy() throws Exception {
        // Given
        System.setProperty("swarm.deployment.[com.foo:app.war]", "");
        ArtifactDeployer artifactDeployer = new ArtifactDeployer();
        artifactDeployer.configView = new ConfigViewImpl();

        // Stub ArtifactLookup
        JavaArchive archive = stubArtifactLookup("com.foo:app:war:*", "app.war");

        // Stub RuntimeDeployer
        RuntimeDeployer runtimeDeployer = stubRuntimeDeployer(artifactDeployer);

        // When
        artifactDeployer.deploy();

        // Then
        verify(runtimeDeployer).deploy(archive, "com.foo:app.war");
    }

    @Test
    public void testDeploy_artifactIdWithDot() throws Exception {
        // Given
        System.setProperty("swarm.deployment.[com.ibm:wsmq.jmsra.rar]", "");
        ArtifactDeployer artifactDeployer = new ArtifactDeployer();
        artifactDeployer.configView = new ConfigViewImpl();

        // Stub ArtifactLookup
        JavaArchive archive = stubArtifactLookup("com.ibm:wsmq.jmsra:rar:*", "wsmq.jmsra.rar");

        // Stub RuntimeDeployer
        RuntimeDeployer runtimeDeployer = stubRuntimeDeployer(artifactDeployer);

        // When
        artifactDeployer.deploy();

        // Then
        verify(runtimeDeployer).deploy(archive, "com.ibm:wsmq.jmsra.rar");
    }

    @Test
    public void testDeploy_artifactIdWithNoExtension() throws Exception {
        // Given
        System.setProperty("swarm.deployment.[com.foo:artifact]", "");
        ArtifactDeployer artifactDeployer = new ArtifactDeployer();
        artifactDeployer.configView = new ConfigViewImpl();

        // Stub ArtifactLookup
        JavaArchive archive = stubArtifactLookup("com.foo:artifact:jar:*", "artifact.jar");

        // Stub RuntimeDeployer
        RuntimeDeployer runtimeDeployer = stubRuntimeDeployer(artifactDeployer);

        // When
        artifactDeployer.deploy();

        // Then
        verify(runtimeDeployer).deploy(archive, "com.foo:artifact");
    }

    private JavaArchive stubArtifactLookup(String gav, String asName) throws Exception {
        ArtifactLookup artifactLookup = mock(ArtifactLookup.class);
        ArtifactLookup.INSTANCE.set(artifactLookup);
        JavaArchive archive = mock(JavaArchive.class);
        when(artifactLookup.artifact(gav, asName)).thenReturn(archive);
        return archive;
    }

    private RuntimeDeployer stubRuntimeDeployer(ArtifactDeployer artifactDeployer) throws NoSuchFieldException, IllegalAccessException {
        RuntimeDeployer runtimeDeployer = mock(RuntimeDeployer.class);
        Instance<RuntimeDeployer> runtimeDeployerInstance = mock(Instance.class);
        when(runtimeDeployerInstance.get()).thenReturn(runtimeDeployer);
        Field deployer = ArtifactDeployer.class.getDeclaredField("deployer");
        deployer.setAccessible(true);
        deployer.set(artifactDeployer, runtimeDeployerInstance);
        return runtimeDeployer;
    }
}