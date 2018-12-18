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

package org.eclipse.microprofile.opentracing.wfswarm;

import io.opentracing.contrib.tracerresolver.TracerResolver;
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
    JavaArchive extensionsJar = ShrinkWrap.create(JavaArchive.class,"mp-ot-mocktracer-resolver.jar")
                 .addAsServiceProvider(TracerResolver.class, MockTracerResolver.class);
    extensionsJar.addAsServiceProvider(Providers.class, ExceptionMapper.class);
    extensionsJar.addClass(MockTracerResolver.class);
    extensionsJar.addClass(ExceptionMapper.class);
    extensionsJar.addPackages(true, "io.opentracing");

    WebArchive war = WebArchive.class.cast(archive);
    war.addAsLibraries(extensionsJar);
    war.setWebXML("web.xml");
  }
}
