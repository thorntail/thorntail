/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.opentracing.tracer.runtime;

import javax.inject.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.opentracing.tracer.OpenTracingTracerResolverFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;


/**
 * @author Pavol Loffay
 */
@DeploymentScoped
public class OpenTracingTracerInstaller implements DeploymentProcessor {
  private static final String DEPLOYMENT_PACKAGE =
      OpenTracingTracerResolverFraction.class.getPackage().getName() + ".deployment";
  private static final String CONTEXT_LISTENER = DEPLOYMENT_PACKAGE + ".TracerResolverListener";

  private final Archive<?> archive;

  @Inject
  public OpenTracingTracerInstaller(Archive archive) {
    this.archive = archive;
  }

  @Override
  public void process() throws Exception {
    if (archive.getName().endsWith(".war")) {
      WARArchive webArchive = archive.as(WARArchive.class);
      WebXmlAsset webXml = webArchive.findWebXmlAsset();
      webXml.addListener(CONTEXT_LISTENER);
    }
  }
}
