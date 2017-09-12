/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.mpjwtauth.runtime;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
 */
@DeploymentScoped
public class MPJWTAuthExtensionArchivePreparer implements DeploymentProcessor {
    private static Logger log = Logger.getLogger(MPJWTAuthExtensionArchivePreparer.class);

    static final DotName LOGIN_CONFIG = DotName.createSimple("org.eclipse.microprofile.auth.LoginConfig");
    static final DotName ROLES_ALLOWED = DotName.createSimple("javax.annotation.security.RolesAllowed");
    static final DotName DENY_ALL = DotName.createSimple("javax.annotation.security.DenyAll");
    static final DotName PERMIT_ALL = DotName.createSimple("javax.annotation.security.PermitAll");

    public static final DotName PATH = DotName.createSimple("javax.ws.rs.Path");

    public static final DotName APP_PATH = DotName.createSimple("javax.ws.rs.ApplicationPath");

    private final Archive archive;

    private final IndexView index;
    private HashSet<DotName> scannedClasses = new HashSet<>();

    @Inject
    private MicroProfileJWTAuthFraction fraction;

    @Inject
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
                jBossWeb.setSecurityDomain(realmName.asString());
            }
        }
        // Get the @ApplicationPath setting
        WebXmlAsset webXml = war.findWebXmlAsset();
        String appPath = "/";
        Collection<AnnotationInstance> appPaths = index.getAnnotations(APP_PATH);
        if (!appPaths.isEmpty()) {
            appPath = appPaths.iterator().next().value().asString();
        }

        // Process the @RolesAllowed and @PermitAll annotations
        // TODO: @DenyAll
        Collection<AnnotationInstance> rolesAnnotations = index.getAnnotations(ROLES_ALLOWED);
        for (AnnotationInstance annotation : rolesAnnotations) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                String[] roles = annotation.value().asStringArray();
                ClassInfo classInfo = annotation.target().asClass();
                List<AnnotationInstance> path = classInfo.annotations().get(PATH);
                log.infof("+++ Class(%s).RolesAllowed: %s, appPath=%s, path=%s", classInfo.name(), Arrays.asList(roles), appPath, path);
                if (!scannedClasses.contains(classInfo.name())) {
                    generateSecurityConstraints(webXml, classInfo, roles, appPath);
                }
            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                MethodInfo methodInfo = annotation.target().asMethod();
                ClassInfo classInfo = methodInfo.declaringClass();
                if (!scannedClasses.contains(classInfo.name()) && !classInfo.classAnnotations().contains(ROLES_ALLOWED)) {
                    String[] roles = {};
                    generateSecurityConstraints(webXml, classInfo, roles, appPath);
                }
            }

        }

        if (fraction.getTokenIssuer().isPresent()) {
            log.debugf("Issuer: %s", fraction.getTokenIssuer().get());
            war.addAsManifestResource(new StringAsset(fraction.getTokenIssuer().get()), "MP-JWT-ISSUER");
        }
        if (fraction.getPublicKey() != null) {
            log.debugf("PublicKey: %s", fraction.getPublicKey());
            war.addAsManifestResource(new StringAsset(fraction.getPublicKey()), "MP-JWT-SIGNER");
        }
        if (log.isInfoEnabled()) {
            log.info("jar: " + jwtAuthJar.toString(true));
            log.info("war: " + war.toString(true));
        }
    }

    /**
     * Generate security constraints for a resource root class.
     *
     * TODO:
     * @param webXml
     * @param classInfo
     * @param roles
     * @param appPath
     */
    private void generateSecurityConstraints(WebXmlAsset webXml, ClassInfo classInfo, String[] roles, String appPath) {
        if (appPath.equals("/")) {
            appPath = "";
        }
        HashSet<String> allRoles = new HashSet<>();
        allRoles.addAll(Arrays.asList(roles));
        List<AnnotationInstance> paths = classInfo.annotations().get(PATH);
        if (paths == null || paths.size() == 0) {
            // Not a resource root
            return;
        }

        // Check for a class level @DenyAll
        boolean classIsDenyAll = false;
        boolean classIsPermitAll = false;
        List<AnnotationInstance> classDenyAll = classInfo.annotations().get(DENY_ALL);
        if (classDenyAll != null) {
            for (AnnotationInstance path : classDenyAll) {
                if (path.target() == classInfo) {
                    SecurityConstraint sc = webXml.protect(appPath + "/*").withRole("");
                    classIsDenyAll = true;
                }
            }
        }
        List<AnnotationInstance> classPermitAll = classInfo.annotations().get(PERMIT_ALL);
        if (classPermitAll != null) {
            for (AnnotationInstance path : classPermitAll) {
                if (path.target() == classInfo) {
                    SecurityConstraint sc = webXml.protect(appPath + "/*").permitAll();
                    classIsPermitAll = true;
                }
            }
        }

        HashMap<String, HashSet<String>> subpaths = new HashMap<>();
        for (AnnotationInstance path : paths) {
            if (path.target() == classInfo) {
                appPath = appPath + path.value().asString();
                System.out.printf("Updated appPath to: %s\n", appPath);
            } else if (path.target().kind() == AnnotationTarget.Kind.METHOD) {
                path.target().asMethod();
                String subpath = path.value().asString();
                if (!(subpath.charAt(0) == '/')) {
                    subpath = "/" + subpath;
                }
                MethodInfo methodInfo = path.target().asMethod();
                AnnotationInstance methodRoles = methodInfo.annotation(ROLES_ALLOWED);
                AnnotationInstance denyAll = methodInfo.annotation(DENY_ALL);
                AnnotationInstance permitAll = methodInfo.annotation(PERMIT_ALL);
                HashSet<String> localRoles = new HashSet<>(allRoles);
                if (denyAll != null) {
                    localRoles = null;
                } else if (permitAll != null) {
                    localRoles.clear();
                } else if (methodRoles != null) {
                    localRoles.addAll(Arrays.asList(methodRoles.value().asStringArray()));
                } else if (classIsDenyAll) {
                    localRoles = null;
                } else if (classIsPermitAll) {
                    localRoles.clear();
                }
                subpaths.put(subpath, localRoles);
            }
        }

        for (Map.Entry<String, HashSet<String>> entry : subpaths.entrySet()) {
            String subpath = appPath + entry.getKey();
            SecurityConstraint sc = webXml.protect(subpath);
            HashSet<String> entryRoles = entry.getValue();
            if (entryRoles == null) {
                // No roles == @DenyAll
                sc.withRole("");
            } else if (entryRoles.isEmpty()) {
                sc.permitAll();
            } else {
                entryRoles.forEach(role -> sc.withRole(role));
            }
        }
        webXml.allConstraints().forEach(sc -> System.out.printf("SecurityConstraint(%s), roles=%s, isPermitAll=%s\n", sc.urlPattern(), sc.roles(), sc.isPermitAll()));
        scannedClasses.add(classInfo.name());
    }
}
