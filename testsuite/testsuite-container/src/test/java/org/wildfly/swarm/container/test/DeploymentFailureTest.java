package org.wildfly.swarm.container.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class DeploymentFailureTest {

  @Test
  public void testDeploymentFailure() throws Exception {
      Swarm swarm = new Swarm();
      swarm.start();
      JARArchive a = ShrinkWrap.create(JARArchive.class, "bad-deployment.jar");
      a.addModule("com.i.do.no.exist");
      try {
          swarm.deploy(a);
          fail("should have throw a DeploymentException");
      } catch (DeploymentException e) {
          // expected and correct
          assertThat(e.getArchive()).isSameAs(a);
          assertThat(e.getMessage()).contains("org.jboss.modules.ModuleNotFoundException: com.i.do.no.exist:main");
      } finally {
          swarm.stop();
      }
  }
}
