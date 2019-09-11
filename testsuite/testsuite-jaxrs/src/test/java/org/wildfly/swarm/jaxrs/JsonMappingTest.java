package org.wildfly.swarm.jaxrs;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class JsonMappingTest {

   @Deployment(testable = false)
   public static Archive createDeployment() throws Exception {
      URL url = Thread.currentThread().getContextClassLoader().getResource("project-test-defaults-path.yml");
      assertThat(url).isNotNull();
      File projectDefaults = new File(url.toURI());
      JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
      deployment.addClass(CustomJsonProvider.class);
      deployment.addResource( JsonMappingResource.class);
      deployment.addAsResource(projectDefaults, "/project-defaults.yml");
      deployment.addAllDependencies();
      return deployment;
   }

   @Test
   @RunAsClient
   public void testSnakeCase() throws Exception {
      String content = IOUtils.toString(new URL("http://localhost:8080/jsonMapping"), Charset.forName("UTF-8"));
      assertThat(content).contains("snake_case");
   }

   @Test
   @RunAsClient
   public void testNullObjectsNotIncluded() throws Exception {
      String content = IOUtils.toString(new URL("http://localhost:8080/jsonMapping"), Charset.forName("UTF-8"));
      assertThat(content).doesNotContain("null_object");
   }
}
