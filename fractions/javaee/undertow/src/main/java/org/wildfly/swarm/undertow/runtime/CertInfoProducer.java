/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.undertow.runtime;

import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.string;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.wildfly.swarm.SwarmInfo;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.descriptors.CertInfo;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@ApplicationScoped
public class CertInfoProducer {

    public static final String JBOSS_DATA_DIR = "jboss.server.data.dir";

    @Inject
    UndertowFraction undertow;

    @AttributeDocumentation("Should a self-signed certificate be generated")
    @Configurable("thorntail.https.certificate.generate")
    Defaultable<Boolean> generateSelfCertificate = bool(false);

    @AttributeDocumentation("Hostname for the generated self-signed certificate")
    @Configurable("thorntail.https.certificate.generate.host")
    Defaultable<String> selfCertificateHost = string("localhost");

    @AttributeDocumentation("Should an embedded keystore be created")
    @Configurable("thorntail.https.keystore.embedded")
    Defaultable<Boolean> embeddedKeystore = bool(false);

    @Produces
    @Singleton
    public CertInfo produceCertInfo() {
        if (generateSelfCertificate.get()) {
            if (SwarmInfo.isProduct()) {
                throw SwarmMessages.MESSAGES.generateSelfSignedCertificateNotSupported();
            }
            checkDataDir();
            return new CertInfo(selfCertificateHost.get(), JBOSS_DATA_DIR);
        } else {
            String keystorePath = undertow.keystorePath();
            if (embeddedKeystore.get()) {
                checkDataDir();
                Path dataDir = Paths.get(System.getProperty(JBOSS_DATA_DIR));
                Path certDestination = dataDir.resolve(keystorePath);
                try {
                    URL jks = ClassLoader.getSystemClassLoader().getResource(keystorePath);
                    if (jks == null) {
                        Module appModule = Module.getCallerModuleLoader().loadModule("swarm.application");
                        jks = appModule.getClassLoader().getResource(keystorePath);
                    }
                    if (jks == null) {
                        throw new RuntimeException(String.format("Unable to locate embedded keystore %s in classpath", keystorePath));
                    }
                    Files.copy(jks.openStream(), certDestination);
                    keystorePath = certDestination.toString();
                } catch (Exception ie) {
                    throw new RuntimeException("Error copying embedded certificate", ie);
                }
            }
            String keystorePassword = undertow.keystorePassword();
            String keyPassword = undertow.keyPassword();
            String keystoreAlias = undertow.alias();
            return new CertInfo(keystorePath, keystorePassword, keyPassword, keystoreAlias);
        }
    }

    protected void checkDataDir() {
        // Remove when SWARM-634 is fixed
        if (System.getProperty(JBOSS_DATA_DIR) == null) {
            File tmpDir = null;
            try {
                tmpDir = TempFileManager.INSTANCE.newTempDirectory("wildfly-swarm-data", ".d");
                System.setProperty(JBOSS_DATA_DIR, tmpDir.getAbsolutePath());
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
