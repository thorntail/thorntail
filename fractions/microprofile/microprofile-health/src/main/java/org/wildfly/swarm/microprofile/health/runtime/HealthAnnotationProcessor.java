/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.health.runtime;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.naming.NamingException;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.microprofile.health.HealthMetaData;
import org.wildfly.swarm.microprofile.health.api.Monitor;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.JBossWebContainer;

/**
 * @author Ken Finnigan
 */
@DeploymentScoped
public class HealthAnnotationProcessor implements DeploymentProcessor {

    public static final DotName HEALTH = DotName.createSimple("org.wildfly.swarm.health.Health");

    public static final DotName MP_HEALTH = DotName.createSimple("org.eclipse.microprofile.health.Health");

    public static final DotName PATH = DotName.createSimple("javax.ws.rs.Path");

    public static final DotName APP_PATH = DotName.createSimple("javax.ws.rs.ApplicationPath");

    @Inject
    public HealthAnnotationProcessor(Archive archive, IndexView index) {
        this.archive = archive;
        this.index = index;
    }

    @Override
    public void process() throws NamingException {

        // first pass: jboss-web context root
        Optional<String> jbossWebContext = Optional.empty();
        //if (archive instanceof JBossWebContainer) {
        if (archive.getName().endsWith(".war")) {
            JBossWebContainer war = archive.as(WARArchive.class);
            if (war.getContextRoot() != null) {
                jbossWebContext = Optional.of(war.getContextRoot());
            }
        }

        // second pass: JAX-RS applications
        Optional<String> appPath = Optional.empty();
        Collection<AnnotationInstance> appPathAnnotations = index.getAnnotations(APP_PATH);
        for (AnnotationInstance annotation : appPathAnnotations) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                appPath = Optional.of(annotation.value().asString());
            }
        }

        // third pass: JAX-RS resources
        Collection<AnnotationInstance> pathAnnotations = index.getAnnotations(PATH);
        for (AnnotationInstance annotation : pathAnnotations) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                ClassInfo classInfo = annotation.target().asClass();

                for (MethodInfo methodInfo : classInfo.methods()) {
                    if (methodInfo.hasAnnotation(HEALTH) || methodInfo.hasAnnotation(MP_HEALTH)) {
                        StringBuilder sb = new StringBuilder();
                        boolean isSecure = false;


                        // prepend the jboss-web cntext if given
                        if (jbossWebContext.isPresent() && !jbossWebContext.get().equals("/")) {
                            safeAppend(sb, jbossWebContext.get());
                        }

                        // prepend the appPath if given
                        if (appPath.isPresent() && !appPath.get().equals("/")) {
                            safeAppend(sb, appPath.get());
                        }

                        // the class level @Path
                        for (AnnotationInstance classAnnotation : classInfo.classAnnotations()) {
                            if (classAnnotation.name().equals(PATH)) {
                                String methodPathValue = classAnnotation.value().asString();
                                if (!methodPathValue.equals("/")) {
                                    safeAppend(sb, methodPathValue);
                                }
                            }
                        }

                        if (methodInfo.hasAnnotation(PATH)) {

                            // the method level @Path
                            safeAppend(sb, methodInfo.annotation(PATH).value().asString());

                            // the method level @Health either MP or regular Swarm
                            AnnotationInstance healthAnnotation = methodInfo.annotation(HEALTH);
                            if (null == healthAnnotation) {
                                healthAnnotation = methodInfo.annotation(MP_HEALTH);
                            }

                            isSecure = healthAnnotation.value("inheritSecurity") != null ? healthAnnotation.value("inheritSecurity").asBoolean() : true;

                        } else {
                            throw new RuntimeException("@Health requires an explicit @Path annotation");
                        }

                        HealthMetaData metaData = new HealthMetaData(sb.toString(), isSecure);
                        Monitor.lookup().registerHealth(metaData);
                    }
                }

            }
        }
    }

    public static void safeAppend(StringBuilder sb, String pathToken) {

        // normalise the token to '/foobar'
        if (!pathToken.startsWith("/")) {
            pathToken = "/" + pathToken;
        }

        if (pathToken.endsWith("/")) {
            pathToken = pathToken.substring(0, pathToken.length() - 1);
        }

        // append to buffer
        sb.append(pathToken);

    }

    private final Archive<?> archive;

    private final IndexView index;
}
