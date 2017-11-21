/**
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
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
 *
 */
package org.wildfly.swarm.mpjwtauth.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import io.undertow.servlet.ServletExtension;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.mpjwtauth.MicroProfileJWTAuthFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.JBossWebAsset;
import org.wildfly.swarm.undertow.descriptors.SecurityConstraint;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

/**
 * A DeploymentProcessor implementation for the MP-JWT custom authentication mechanism that adds support
 * for that mechanism to any war the declares a login-config/auth-method = MP-JWT.
 *
 * This also handles the mapping of the javax.annotation security annotations to WebXml security
 * constraints.
 */
@DeploymentScoped
@SuppressWarnings("unused")
public class MPJWTAuthExtensionArchivePreparer implements DeploymentProcessor {
    private static Logger log = Logger.getLogger(MPJWTAuthExtensionArchivePreparer.class);

    private static final DotName LOGIN_CONFIG = DotName.createSimple("org.eclipse.microprofile.auth.LoginConfig");
    private static final DotName ROLES_ALLOWED = DotName.createSimple("javax.annotation.security.RolesAllowed");
    private static final DotName DENY_ALL = DotName.createSimple("javax.annotation.security.DenyAll");
    private static final DotName PERMIT_ALL = DotName.createSimple("javax.annotation.security.PermitAll");
    private static final DotName PATH = DotName.createSimple("javax.ws.rs.Path");
    private static final DotName APP_PATH = DotName.createSimple("javax.ws.rs.ApplicationPath");

    private final Archive archive;

    private final IndexView index;
    // A map of the deployment classes scanned for security annotations
    private HashSet<DotName> scannedClasses = new HashSet<>();

    @Inject
    @SuppressWarnings("unused")
    private MicroProfileJWTAuthFraction fraction;

    @Inject
    @SuppressWarnings("unused")
    public MPJWTAuthExtensionArchivePreparer(Archive archive, IndexView index) {
        this.archive = archive;
        this.index = index;
    }

    @Override
    public void process() throws Exception {
        // This is really a work around addAsServiceProvider not supporting multiple addAsServiceProvider calls (https://github.com/shrinkwrap/shrinkwrap/issues/112)
        JavaArchive jwtAuthJar = ShrinkWrap.create(JavaArchive.class, "jwt-auth-wfswarm.jar")
                .addAsServiceProvider(ServletExtension.class.getName(), "org.wildfly.swarm.mpjwtauth.deployment.auth.JWTAuthMethodExtension")
                .addAsServiceProvider("org.wildfly.swarm.mpjwtauth.deployment.principal.JWTCallerPrincipalFactory", "org.wildfly.swarm.mpjwtauth.deployment.principal.DefaultJWTCallerPrincipalFactory")
                .addAsServiceProvider(Extension.class.getName(), "org.wildfly.swarm.mpjwtauth.deployment.auth.cdi.MPJWTExtension");
        WARArchive war = archive.as(WARArchive.class);
        war.addAsLibraries(jwtAuthJar);
        // Check for LoginConfig annotation
        Collection<AnnotationInstance> lcAnnotations = index.getAnnotations(LOGIN_CONFIG);
        for (AnnotationInstance lc : lcAnnotations) {
            AnnotationValue authMethod = lc.value("authMethod");
            AnnotationValue realmName = lc.value("realmName");
            String realm = realmName != null ? realmName.asString() : "";
            // Set the web.xml login-config auth-method and jboss-web.xml security domain
            if (authMethod != null) {
                WebXmlAsset webXml = war.findWebXmlAsset();
                webXml.setLoginConfig(authMethod.asString(), realm);
            }
            if (realm.length() > 0) {
                JBossWebAsset jBossWeb = war.findJbossWebAsset();
                jBossWeb.setSecurityDomain(realm);
            }
        }
        // Get the @ApplicationPath setting
        WebXmlAsset webXml = war.findWebXmlAsset();
        String appPath = "/";
        Collection<AnnotationInstance> appPaths = index.getAnnotations(APP_PATH);
        if (!appPaths.isEmpty()) {
            appPath = appPaths.iterator().next().value().asString();
        }

        // Process the @RolesAllowed, @PermitAll and @DenyAll annotations
        Collection<AnnotationInstance> rolesAnnotations = index.getAnnotations(ROLES_ALLOWED);
        for (AnnotationInstance annotation : rolesAnnotations) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                // Process the root resource
                String[] roles = annotation.value().asStringArray();
                ClassInfo classInfo = annotation.target().asClass();
                if (!scannedClasses.contains(classInfo.name())) {
                    generateSecurityConstraints(webXml, classInfo, roles, appPath);
                }
            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                // Process the containing root resource if it has not been already
                MethodInfo methodInfo = annotation.target().asMethod();
                ClassInfo classInfo = methodInfo.declaringClass();
                if (!scannedClasses.contains(classInfo.name())) {
                    String[] roles = {};
                    generateSecurityConstraints(webXml, classInfo, roles, appPath);
                }
            }

        }

        // Handle the verification configuration on the fraction
        if (fraction.getTokenIssuer().isPresent()) {
            log.debugf("Issuer: %s", fraction.getTokenIssuer().get());
            war.addAsManifestResource(new StringAsset(fraction.getTokenIssuer().get()), "MP-JWT-ISSUER");
        }
        if (fraction.getPublicKey() != null) {
            log.debugf("PublicKey: %s", fraction.getPublicKey());
            war.addAsManifestResource(new StringAsset(fraction.getPublicKey()), "MP-JWT-SIGNER");
        }
        if (log.isTraceEnabled()) {
            log.trace("jar: " + jwtAuthJar.toString(true));
            log.trace("war: " + war.toString(true));
        }
    }

    /**
     * Generate security constraints for a resource root class.
     *
     * @param webXml - the deployment web.xml metadata to add the security constraints to
     * @param classInfo - the class to scan for security constraints
     * @param roles - class level roles if any
     * @param appPath - the @ApplicationPath if any
     */
    private void generateSecurityConstraints(WebXmlAsset webXml, ClassInfo classInfo, String[] roles, final String appPath) {
        // This includes both class level and method level @Path instances
        List<AnnotationInstance> paths = classInfo.annotations().get(PATH);
        if (paths == null || paths.size() == 0) {
            // Not a resource root
            return;
        }

        StringBuilder fullAppPath = new StringBuilder(appPath);
        if (fullAppPath.charAt(fullAppPath.length() - 1) != '/') {
            fullAppPath.append('/');
        }

        List<SecurityConstraint> newConstraints = new ArrayList<>();
        HashSet<String> allRoles = new HashSet<>();
        allRoles.addAll(Arrays.asList(roles));
        // Get the root @Path annotation if it exists
        ListIterator<AnnotationInstance> pathsIter = paths.listIterator();
        while (pathsIter.hasNext()) {
            AnnotationInstance ann = pathsIter.next();
            if (ann.target().kind() == AnnotationTarget.Kind.CLASS) {
                String subpath = ann.value().asString();
                if (subpath.charAt(0) == '/') {
                    fullAppPath.append(subpath.substring(1));
                } else {
                    fullAppPath.append(subpath);
                }
                if (fullAppPath.charAt(fullAppPath.length() - 1) != '/') {
                    fullAppPath.append('/');
                }
                pathsIter.remove();
                break;
            }
        }

        // Check for a class level @DenyAll
        boolean classIsDenyAll = false;
        boolean classIsPermitAll = false;

        List<AnnotationInstance> classDenyAll = classInfo.annotations().get(DENY_ALL);
        if (classDenyAll != null) {
            for (AnnotationInstance ann : classDenyAll) {
                if (ann.target() == classInfo) {
                    // Create a security constraint that denies all access to subresources by default
                    SecurityConstraint sc = webXml.protect(fullAppPath.toString() + "*").withRole("");
                    newConstraints.add(sc);
                    classIsDenyAll = true;
                }
            }
        }
        // Check for class level @PermitAll
        List<AnnotationInstance> classPermitAll = classInfo.annotations().get(PERMIT_ALL);
        if (classPermitAll != null) {
            for (AnnotationInstance ann : classPermitAll) {
                if (ann.target() == classInfo) {
                    // Create a security constraint that permits all access to subresources by default
                    SecurityConstraint sc = webXml.protect(fullAppPath.toString() + "*").permitAll();
                    newConstraints.add(sc);
                    classIsPermitAll = true;
                }
            }
        }

        // Process the method level @Path and security annotations into security constraints
        for (AnnotationInstance path : paths) {
            if (path.target().kind() == AnnotationTarget.Kind.METHOD) {
                // For each method determine the endpoint path and roles
                String subpath = path.value().asString();
                MethodInfo methodInfo = path.target().asMethod();
                AnnotationInstance rolesAllowed = methodInfo.annotation(ROLES_ALLOWED);
                AnnotationInstance denyAll = methodInfo.annotation(DENY_ALL);
                AnnotationInstance permitAll = methodInfo.annotation(PERMIT_ALL);
                // Start with the class level @RolesAllowed
                HashSet<String> localRoles = new HashSet<>(allRoles);
                if (denyAll != null) {
                    // To deny access we need a security constraint with an empty roles which we indicate by a null
                    localRoles = null;
                } else if (permitAll != null) {
                    // To permit all access we need a security contraint with no auth contraint which we indicate by an empty roles
                    localRoles.clear();
                } else if (rolesAllowed != null) {
                    // Override the class level roles
                    localRoles.clear();
                    localRoles.addAll(Arrays.asList(rolesAllowed.value().asStringArray()));
                } else if (classIsDenyAll) {
                    localRoles = null;
                } else if (classIsPermitAll) {
                    localRoles.clear();
                }

                String uriPath;
                if (subpath.charAt(0) == '/') {
                    uriPath = fullAppPath.toString() + subpath.substring(1);
                } else {
                    uriPath = fullAppPath.toString() + subpath;
                }
                // If this uri includes a path param, truncate and add a wildcard
                int pathParamStart = uriPath.indexOf('{');
                if (pathParamStart >= 0) {
                    uriPath = uriPath.substring(0, pathParamStart);
                    if (uriPath.charAt(uriPath.length() - 1) != '/') {
                        uriPath += '/';
                    }
                    uriPath += "*";
                }
                SecurityConstraint sc = webXml.protect(uriPath);
                // No roles == @DenyAll
                if (localRoles == null) {
                    sc.withRole("");
                    // Empy roles == @PermitAll
                } else if (localRoles.isEmpty()) {
                    sc.permitAll();
                } else {
                    localRoles.forEach(sc::withRole);
                }
                newConstraints.add(sc);
            }
        }

        if (log.isDebugEnabled()) {
            log.debugf("SecurityConstraints introduced by class: %s", classInfo.name());
            for (SecurityConstraint sc : newConstraints) {
                log.debugf("SecurityConstraint(%s), roles=%s, isPermitAll=%s",
                           sc.urlPattern(), sc.roles(), sc.isPermitAll());
            }
        }
        scannedClasses.add(classInfo.name());
    }
}
