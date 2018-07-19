package io.thorntail.opentracing.tck;

import javax.ws.rs.ext.Providers;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Pavol Loffay
 */
public class DeploymentProcessor implements ApplicationArchiveProcessor {

  @Override
  public void process(Archive<?> archive, TestClass testClass) {
    JavaArchive extensionsJar = ShrinkWrap.create(JavaArchive.class,"extension-jar.jar");
    extensionsJar.addAsServiceProvider(Providers.class, ExceptionMapper.class);
    extensionsJar.addClass(ExceptionMapper.class);

    WebArchive war = WebArchive.class.cast(archive);
    war.addAsLibraries(extensionsJar);

  }
}
