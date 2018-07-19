package io.thorntail.jaxrs.impl.opentracing.jaxrs;

import io.smallrye.opentracing.SmallRyeTracingDynamicFeature;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Pavol Loffay
 */
@Provider
@ApplicationScoped
public class SmallRyeDynamicFeatureWrapper implements DynamicFeature {

  private SmallRyeTracingDynamicFeature delegate = new SmallRyeTracingDynamicFeature();

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    this.delegate.configure(resourceInfo, context);
  }
}
