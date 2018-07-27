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
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Pavol Loffay
 */
@DeploymentScoped
public class TracingInstaller implements DeploymentProcessor {
  private static final String DEPLOYMENT_PACKAGE = "org.wildfly.swarm.mpopentracing.deployment";
  private static final String RESTEASY_PROVIDERS = "io.smallrye.opentracing.SmallRyeTracingDynamicFeature";
  private static final String CONTEXT_LISTENER = DEPLOYMENT_PACKAGE + ".OpenTracingContextInitializer";

  private final Archive<?> archive;

  @Inject
  public TracingInstaller(Archive archive) {
    this.archive = archive;
  }

  @Override
  public void process() throws Exception {
    if (archive.getName().endsWith(".war")) {
      WARArchive warArchive = archive.as(WARArchive.class);
      warArchive.findWebXmlAsset()
          .addListener(CONTEXT_LISTENER);

      JAXRSArchive jaxrsArchive = archive.as(JAXRSArchive.class);
      jaxrsArchive.findWebXmlAsset()
          .setContextParam("resteasy.providers", RESTEASY_PROVIDERS);
    }
  }
}
