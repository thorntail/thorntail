/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.thorntail.openapi.impl;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;

/**
 * Wraps an {@link IndexView} instance and filters the contents based on the
 * settings provided via {@link OpenApiConfig}.
 * 
 * TODO Remove this once smallrye-open-api version 1.0.1 is released!  This class has been moved there.
 * 
 * @author eric.wittmann@gmail.com
 */
public class FilteredIndexView implements IndexView {

    private final IndexView delegate;

    private final Set<String> scanClasses;
    private final Set<String> scanPackages;
    private final Set<String> scanExcludeClasses;
    private final Set<String> scanExcludePackages;

    /**
     * Constructor.
     * @param delegate
     * @param config
     */
    public FilteredIndexView(IndexView delegate, OpenApiConfig config) {
        this.delegate = delegate;

        scanClasses = config.scanClasses();
        scanPackages = config.scanPackages();
        scanExcludeClasses = config.scanExcludeClasses();
        scanExcludePackages = config.scanExcludePackages();

    }
    
    /**
     * Returns true if the class name should be included in the index (is either included or
     * not excluded).
     * @param className
     */
    private boolean accepts(DotName className) {
        String fqcn = className.toString();
        String packageName = fqcn.substring(0, fqcn.lastIndexOf('.'));
        boolean accept;
        // Includes
        if (scanClasses.isEmpty() && scanPackages.isEmpty()) {
            accept = true;
        } else if (!scanClasses.isEmpty() && scanPackages.isEmpty()) {
            accept = scanClasses.contains(fqcn);
        } else if (scanClasses.isEmpty() && !scanPackages.isEmpty()) {
            accept = scanPackages.contains(packageName);
        } else {
            accept = scanClasses.contains(fqcn) || scanPackages.contains(packageName);
        }
        // Excludes override includes
        if (!scanExcludeClasses.isEmpty() && scanExcludeClasses.contains(fqcn)) {
            accept = false;
        }
        if (!scanExcludePackages.isEmpty() && scanExcludePackages.contains(packageName)) {
            accept = false;
        }
        return accept;

    }

    /**
     * @see org.jboss.jandex.IndexView#getKnownClasses()
     */
    @Override
    public Collection<ClassInfo> getKnownClasses() {
        return this.delegate.getKnownClasses().stream().filter( ci -> accepts(ci.name())).collect(Collectors.toList());
    }

    /**
     * @see org.jboss.jandex.IndexView#getClassByName(org.jboss.jandex.DotName)
     */
    @Override
    public ClassInfo getClassByName(DotName className) {
        if (this.accepts(className)) {
            return this.delegate.getClassByName(className);
        } else {
            return null;
        }
    }

    /**
     * @see org.jboss.jandex.IndexView#getKnownDirectSubclasses(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
        return this.delegate.getKnownDirectSubclasses(className).stream().filter( ci -> accepts(ci.name())).collect(Collectors.toList());
    }

    /**
     * @see org.jboss.jandex.IndexView#getAllKnownSubclasses(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        return this.delegate.getAllKnownSubclasses(className).stream().filter( ci -> accepts(ci.name())).collect(Collectors.toList());
    }

    /**
     * @see org.jboss.jandex.IndexView#getKnownDirectImplementors(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(DotName className) {
        return this.delegate.getKnownDirectImplementors(className).stream().filter( ci -> accepts(ci.name())).collect(Collectors.toList());
    }

    /**
     * @see org.jboss.jandex.IndexView#getAllKnownImplementors(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
        return this.delegate.getAllKnownImplementors(interfaceName).stream().filter( ci -> accepts(ci.name())).collect(Collectors.toList());
    }

    /**
     * @see org.jboss.jandex.IndexView#getAnnotations(org.jboss.jandex.DotName)
     */
    @Override
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
        return this.delegate.getAnnotations(annotationName).stream().filter( ai -> {
            AnnotationTarget target = ai.target();
            switch (target.kind()) {
            case CLASS:
                return accepts(target.asClass().name());
            case FIELD:
                return accepts(target.asField().declaringClass().name());
            case METHOD:
                return accepts(target.asMethod().declaringClass().name());
            case METHOD_PARAMETER:
                return accepts(target.asMethodParameter().method().declaringClass().name());
            case TYPE:
                // TODO properly handle filtering of "type" annotation targets
                return true;
            default:
                return false;
            }
        }).collect(Collectors.toList());
    }

}
