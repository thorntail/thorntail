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
package org.wildfly.swarm.arquillian.adapter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.arquillian.daemon.DaemonServiceActivator;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Toby Crawley
 * @author alexsoto
 * @author Ken Finnigan
 */
public class InVMSimpleContainer implements SimpleContainer {

    public InVMSimpleContainer(Class<?> testClass) {
        this.testClass = testClass;
    }

    public InVMSimpleContainer setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
        return this;
    }

    @Override
    public void start(Archive<?> archive) throws Exception {
        archive.as(ServiceActivatorArchive.class)
                .addServiceActivator(DaemonServiceActivator.class)
                .as(JARArchive.class)
                .addModule("org.wildfly.swarm.arquillian.daemon");

        System.setProperty(BootstrapProperties.APP_ARTIFACT, archive.getName());

        Method swarmMethod = getAnnotatedMethodWithAnnotation(this.testClass,
                                                              CreateSwarm.class);

        // If there is a method annotated with @CreateSwarm
        if (swarmMethod != null) {
            if (Modifier.isStatic(swarmMethod.getModifiers())) {
                final Object swarm = swarmMethod.invoke(null);

                if (swarm instanceof Swarm) {
                    this.swarm = (Swarm) swarm;
                } else {
                    throw new IllegalArgumentException(
                            String.format("Method annotated with %s does not return an instance of %s",
                                          CreateSwarm.class.getSimpleName(),
                                          Swarm.class.getSimpleName()));
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("Method annotated with %s is %s but it is not static",
                                      CreateSwarm.class.getSimpleName(),
                                      swarmMethod));
            }
        } else {
            this.swarm = new Swarm();
        }

        handleJavaVmArguments();
        this.swarm.start().deploy(archive);
    }

    private void handleJavaVmArguments() {
        if (this.javaVmArguments == null) {
            return;
        }

        StringTokenizer tokens = new StringTokenizer(this.javaVmArguments);

        while (tokens.hasMoreTokens()) {
            String each = tokens.nextToken();
            if (!each.startsWith("-D")) {
                System.err.println("ignoring non-property Java VM argument for InVM test: " + each);
                continue;
            }

            each = each.substring(2);

            int equalLoc = each.indexOf("=");
            if (equalLoc < 0) {
                System.setProperty(each, "true");
            } else {
                String key = each.substring(0, equalLoc);
                String value = each.substring(equalLoc + 1);
                System.setProperty(key, value);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (swarm != null) {
            swarm.stop();
        }
    }

    private final Class<?> testClass;

    private Swarm swarm;

    private String javaVmArguments;
}
