/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.mpopentracing.runtime;

import javax.inject.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

/**
 * @author Pavol Loffay
 */
@DeploymentScoped
public class TracingInstaller implements DeploymentProcessor {
  private static final String RESTEASY_PROVIDERS = "resteasy.providers";

  private static final String DEPLOYMENT_PACKAGE = "org.wildfly.swarm.mpopentracing.deployment";
  private static final String TRACING_FEATURE = "io.smallrye.opentracing.SmallRyeTracingDynamicFeature";

  private static final String CONTEXT_LISTENER = DEPLOYMENT_PACKAGE + ".OpenTracingContextInitializer";

  private final Archive<?> archive;

  @Inject
  public TracingInstaller(Archive archive) {
    this.archive = archive;
  }

  @Override
  public void process() throws Exception {
    if (archive.getName().endsWith(".war")) {
      JAXRSArchive jaxrsArchive = archive.as(JAXRSArchive.class);

      WebXmlAsset webXmlAsset = jaxrsArchive.findWebXmlAsset();

      webXmlAsset.addListener(CONTEXT_LISTENER);

      String userProviders = webXmlAsset.getContextParam(RESTEASY_PROVIDERS);

      String providers =
              userProviders == null
                      ? TRACING_FEATURE
                      : userProviders + "," + TRACING_FEATURE;


      webXmlAsset.setContextParam(RESTEASY_PROVIDERS, providers);
    }
  }
}
