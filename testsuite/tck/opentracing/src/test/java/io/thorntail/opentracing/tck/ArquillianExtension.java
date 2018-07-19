package io.thorntail.opentracing.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author Pavol Loffay
 */
public class ArquillianExtension implements LoadableExtension {

  @Override
  public void register(ExtensionBuilder extensionBuilder) {
    extensionBuilder.service(ApplicationArchiveProcessor.class, DeploymentProcessor.class);
  }
}
