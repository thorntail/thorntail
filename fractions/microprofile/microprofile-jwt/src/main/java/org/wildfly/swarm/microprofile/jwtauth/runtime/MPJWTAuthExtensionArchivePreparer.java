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
package org.wildfly.swarm.microprofile.jwtauth.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.wildfly.swarm.microprofile.jwtauth.MicroProfileJWTAuthFraction;
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
public class MPJWTAuthExtensionArchivePreparer implements DeploymentProcessor {

    private static Logger log = Logger.getLogger(MPJWTAuthExtensionArchivePreparer.class);

    private static final DotName LOGIN_CONFIG = DotName.createSimple("org.eclipse.microprofile.auth.LoginConfig");
    private static final DotName ROLES_ALLOWED = DotName.createSimple("javax.annotation.security.RolesAllowed");
    private static final DotName DENY_ALL = DotName.createSimple("javax.annotation.security.DenyAll");
    private static final DotName PERMIT_ALL = DotName.createSimple("javax.annotation.security.PermitAll");
    private static final DotName PATH = DotName.createSimple("javax.ws.rs.Path");
    private static final DotName APP_PATH = DotName.createSimple("javax.ws.rs.ApplicationPath");

    private static final DotName HTTP_METHOD = DotName.createSimple("javax.ws.rs.HttpMethod");
    private static final DotName GET = DotName.createSimple("javax.ws.rs.GET");
    private static final DotName POST = DotName.createSimple("javax.ws.rs.POST");
    private static final DotName PUT = DotName.createSimple("javax.ws.rs.PUT");
    private static final DotName DELETE = DotName.createSimple("javax.ws.rs.DELETE");
    private static final DotName HEAD = DotName.createSimple("javax.ws.rs.HEAD");
    private static final DotName OPTIONS = DotName.createSimple("javax.ws.rs.OPTIONS");

    private static final String[] EMPTY_ROLES = new String[0];

    private final Archive archive;

    private final IndexView index;

    @Inject
    private MicroProfileJWTAuthFraction fraction;

    @Inject
    public MPJWTAuthExtensionArchivePreparer(Archive archive, IndexView index) {
        this.archive = archive;
        this.index = index;
    }

    @Override
    public void process() throws Exception {
        WARArchive war = archive.as(WARArchive.class);
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

        Iterable<DotName> httpMethods = collectHttpMethods();
        Set<DotName> scannedClasses = new HashSet<>();

        // Process the @RolesAllowed, @PermitAll and @DenyAll annotations
        List<AnnotationInstance> securityAnnotations = new ArrayList<>();
        securityAnnotations.addAll(index.getAnnotations(ROLES_ALLOWED));
        securityAnnotations.addAll(index.getAnnotations(PERMIT_ALL));
        securityAnnotations.addAll(index.getAnnotations(DENY_ALL));

        for (AnnotationInstance annotation : securityAnnotations) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                ClassInfo classInfo = annotation.target().asClass();
                if (!scannedClasses.contains(classInfo.name())) {
                    generateSecurityConstraints(webXml, classInfo, appPath, httpMethods, scannedClasses);
                }
            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                MethodInfo methodInfo = annotation.target().asMethod();
                ClassInfo classInfo = methodInfo.declaringClass();
                if (!scannedClasses.contains(classInfo.name())) {
                    generateSecurityConstraints(webXml, classInfo, appPath, httpMethods, scannedClasses);
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
     * @param httpMethods
     */
    private void generateSecurityConstraints(WebXmlAsset webXml, ClassInfo classInfo, String appPath, Iterable<DotName> httpMethods,
            Set<DotName> scannedClasses) {

        List<MethodInfo> resourceMethods = getResourceMethods(classInfo, httpMethods);
        if (resourceMethods.isEmpty()) {
            // Not a resource root
            return;
        }

        StringBuilder fullAppPath = new StringBuilder(appPath);
        if (fullAppPath.charAt(fullAppPath.length() - 1) != '/') {
            fullAppPath.append('/');
        }

        List<SecurityConstraint> newConstraints = new ArrayList<>();

        // Get the root @Path annotation if it exists
        Optional<AnnotationInstance> rooPath = classInfo.classAnnotations().stream().filter(a -> a.name().equals(PATH)).findFirst();
        if (rooPath.isPresent()) {
            String subpath = rooPath.get().value().asString();
            if (subpath.charAt(0) == '/') {
                fullAppPath.append(subpath.substring(1));
            } else {
                fullAppPath.append(subpath);
            }
            if (fullAppPath.charAt(fullAppPath.length() - 1) != '/') {
                fullAppPath.append('/');
            }
        }

        // Process the method level security annotations into security constraints
        for (Iterator<MethodInfo> iterator = resourceMethods.iterator(); iterator.hasNext();) {
            MethodInfo resourceMethod = iterator.next();

            AnnotationInstance path = resourceMethod.annotation(PATH);
            String subpath = path != null ? path.value().asString() : "";

            AnnotationInstance rolesAllowed = resourceMethod.annotation(ROLES_ALLOWED);
            AnnotationInstance denyAll = resourceMethod.annotation(DENY_ALL);
            AnnotationInstance permitAll = resourceMethod.annotation(PERMIT_ALL);

            if (rolesAllowed == null && denyAll == null && permitAll == null) {
                // Non-constrained resource method
                continue;
            }

            // To deny access we need a security constraint with an empty roles which we indicate by a null
            String[] localRoles = null;
            if (permitAll != null) {
                // To permit all access we need a security contraint with no auth contraint which we indicate by an empty roles
                localRoles = EMPTY_ROLES;
            } else if (rolesAllowed != null) {
                // Override the class level roles
                localRoles = rolesAllowed.value().asStringArray();
            }
            newConstraints.add(createSecurityConstraint(webXml, getUriPath(subpath, fullAppPath.toString()), localRoles));
            iterator.remove();
        }

        // If there are some non-constrained resource methods then try to apply class-level constraints
        if (!resourceMethods.isEmpty()) {

            // Check for a class level annotations
            AnnotationInstance classRolesAllowed = classInfo.classAnnotations().stream().filter(a -> a.name().equals(ROLES_ALLOWED)).findFirst().orElse(null);
            AnnotationInstance classDenyAll = classInfo.classAnnotations().stream().filter(a -> a.name().equals(DENY_ALL)).findFirst().orElse(null);
            AnnotationInstance classPermitAll = classInfo.classAnnotations().stream().filter(a -> a.name().equals(PERMIT_ALL)).findFirst().orElse(null);

            if (newConstraints.isEmpty()) {
                // No resource method is constrained - use global constraints
                String uriPath = fullAppPath.toString() + "*";
                if (classDenyAll != null) {
                    // Create a security constraint that denies all access to subresources by default
                    newConstraints.add(createSecurityConstraint(webXml, uriPath, null));
                } else if (classPermitAll != null) {
                    // Create a security constraint that permits all access to subresources by default
                    newConstraints.add(createSecurityConstraint(webXml, uriPath, EMPTY_ROLES));
                } else if (classRolesAllowed != null) {
                    newConstraints.add(createSecurityConstraint(webXml, uriPath, classRolesAllowed.value().asStringArray()));
                }
            } else {
                // Try to apply class-level constraints
                for (MethodInfo nonConstrained : resourceMethods) {

                    AnnotationInstance path = nonConstrained.annotation(PATH);
                    String subpath = path != null ? path.value().asString() : "";

                    String[] localRoles = null;
                    if (classPermitAll != null) {
                        localRoles = EMPTY_ROLES;
                    } else if (classRolesAllowed != null) {
                        localRoles = classRolesAllowed.value().asStringArray();
                    }

                    if (localRoles != null || (localRoles == null && (classDenyAll != null || fraction.isDefaultMissingMethodPermissionsDenyAccess()))) {
                        newConstraints.add(createSecurityConstraint(webXml, getUriPath(subpath, fullAppPath.toString()), localRoles));
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debugf("SecurityConstraints introduced by class: %s", classInfo.name());
            for (SecurityConstraint sc : newConstraints) {
                log.debugf("SecurityConstraint(%s), roles=%s, isPermitAll=%s", sc.urlPattern(), sc.roles(), sc.isPermitAll());
            }
        }
        scannedClasses.add(classInfo.name());
    }

    private SecurityConstraint createSecurityConstraint(WebXmlAsset webXml, String uriPath, String[] localRoles) {
        SecurityConstraint securityConstraint = webXml.protect(uriPath);
        if (localRoles == null) {
            // No roles == @DenyAll
            securityConstraint.withRole("");
        } else if (localRoles.length == 0) {
            // Empty roles == @PermitAll
            securityConstraint.permitAll();
        } else {
            securityConstraint.withRole(localRoles);
        }
        return securityConstraint;
    }

    private String getUriPath(String subpath, String fullAppPath) {
        String uriPath;
        if (subpath.isEmpty()) {
            // Remove the trailing slash
            uriPath = fullAppPath.substring(0, fullAppPath.length() - 1);
        } else if (subpath.charAt(0) == '/') {
            uriPath = fullAppPath + subpath.substring(1);
        } else {
            uriPath = fullAppPath + subpath;
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
        return uriPath;
    }

    private List<MethodInfo> getResourceMethods(ClassInfo classInfo, Iterable<DotName> httpMethods) {
        List<MethodInfo> resourceMethods = new ArrayList<>();
        for (MethodInfo method : classInfo.methods()) {
            if (isResourceMethod(method, httpMethods)) {
                resourceMethods.add(method);
            }
        }
        return resourceMethods;
    }

    private boolean isResourceMethod(MethodInfo method, Iterable<DotName> httpMethods) {
        for (DotName httpMethod : httpMethods) {
            if (method.hasAnnotation(httpMethod)) {
                return true;
            }
        }
        return false;
    }

    private Iterable<DotName> collectHttpMethods() {
        List<DotName> httpMethods = new ArrayList<>();
        httpMethods.add(GET);
        httpMethods.add(POST);
        httpMethods.add(PUT);
        httpMethods.add(DELETE);
        httpMethods.add(HEAD);
        httpMethods.add(OPTIONS);
        for (AnnotationInstance customHttpMethod : index.getAnnotations(HTTP_METHOD)) {
            httpMethods.add(customHttpMethod.name());
        }
        return httpMethods;
    }
}
