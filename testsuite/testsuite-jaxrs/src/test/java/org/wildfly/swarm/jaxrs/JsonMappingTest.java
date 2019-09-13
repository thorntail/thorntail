package org.wildfly.swarm.jaxrs;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class JsonMappingTest {
   @Deployment(testable = false)
   public static Archive createDeployment() throws Exception {
      return ShrinkWrap.create(JAXRSArchive.class, "myapp.war")
              .addClasses(CustomJsonProvider.class, JsonMappingResource.class)
              .addAsResource("project-test-defaults-path.yml", "/project-defaults.yml")
              .addAllDependencies();
   }

   @Test
   @RunAsClient
   public void testSnakeCase() throws Exception {
      String content = Request.Get("http://localhost:8080/jsonMapping").execute().returnContent().asString();
      assertThat(content).contains("snake_case");
   }

   @Test
   @RunAsClient
   public void testNullObjectsNotIncluded() throws Exception {
      String content = Request.Get("http://localhost:8080/jsonMapping").execute().returnContent().asString();
      assertThat(content).doesNotContain("null_object");
   }
}
