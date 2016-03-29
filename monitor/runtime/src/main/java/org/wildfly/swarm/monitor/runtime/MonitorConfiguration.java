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
package org.wildfly.swarm.monitor.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassAsset;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.wildfly.swarm.monitor.MonitorFraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;

/**
 * @author Heiko Braun
 */
public class MonitorConfiguration extends AbstractServerConfiguration<MonitorFraction> {

    /**
     * Path to the WEB-INF inside of the Archive.
     */
    private static final ArchivePath PATH_WEB_INF = ArchivePaths.create("WEB-INF");

    /**
     * Path to the classes inside of the Archive.
     */
    private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_WEB_INF, "classes");

    public static final DotName HEALTH = DotName.createSimple("org.wildfly.swarm.monitor.Health");
    public static final DotName PATH = DotName.createSimple("javax.ws.rs.Path");

    public MonitorConfiguration() {
        super(MonitorFraction.class);
    }

    @Override
    public MonitorFraction defaultFraction() {
        return new MonitorFraction();
    }

    @Override
    public List<ServiceActivator> getServiceActivators(MonitorFraction fraction) {
        List<ServiceActivator> activators = new ArrayList<>();
        activators.add(new MonitorServiceActivator(fraction.securityRealm()));
        return activators;
    }

    @Override
    public void prepareArchive(Archive<?> a) {
        JARArchive jarArchive = a.as(JARArchive.class);
        jarArchive.addModule("javax.ws.rs.api");
        jarArchive.addModule("org.wildfly.swarm.monitor");
        jarArchive.addModule("org.jboss.dmr");

        Asset resource = new ClassAsset(HealthResponseFilter.class);
        ArchivePath location = new BasicPath(PATH_CLASSES, AssetUtil.getFullPathForClassResource(HealthResponseFilter.class));
        jarArchive.add(resource, location);
    }

    @Override
    public void processArchiveMetaData(Archive<?> a, Index index) {
        List<AnnotationInstance> annotations = index.getAnnotations(PATH);
        for (AnnotationInstance annotation : annotations) {
            if(annotation.target().kind()== AnnotationTarget.Kind.CLASS)
            {
                ClassInfo classInfo = annotation.target().asClass();

                for (MethodInfo methodInfo : classInfo.methods()) {
                    if (methodInfo.hasAnnotation(HEALTH)) {
                        StringBuffer sb = new StringBuffer();
                        boolean isSecure = false;

                        // the class level @Path
                        for (AnnotationInstance classAnnotation : classInfo.classAnnotations()) {
                            if (classAnnotation.name().equals(PATH)) {
                                String methodPathValue = classAnnotation.value().asString();
                                if (!methodPathValue.equals("/"))
                                    sb.append(methodPathValue);
                            }
                        }

                        if (methodInfo.hasAnnotation(PATH)) {

                            // the method local @Path
                            sb.append(methodInfo.annotation(PATH).value().asString());

                            // the method local @Health
                            AnnotationInstance healthAnnotation = methodInfo.annotation(HEALTH);
                            isSecure = healthAnnotation.value("inheritSecurity")!=null ? healthAnnotation.value("inheritSecurity").asBoolean() : true;

                        } else {
                            throw new RuntimeException("@Health requires an explicit @Path annotation");
                        }

                        try {
                            HealthMetaData metaData = new HealthMetaData(sb.toString(), isSecure);
                            Monitor.lookup().registerHealth(metaData);
                        } catch (NamingException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }


}
