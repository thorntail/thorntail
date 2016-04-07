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
package org.wildfly.swarm.drools.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthentication;
import org.wildfly.swarm.config.security.security_domain.authentication.LoginModule;
import org.wildfly.swarm.security.SecurityFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Salaboy
 */
public class DroolsServerFraction implements Fraction {
    
    private static String configFolder = System.getProperty("org.drools.server.swarm.security.conf");

    public DroolsServerFraction() {
    }
   

    @Override
    public void postInitialize(Fraction.PostInitContext initContext) {

        if (System.getProperty("org.drools.server.swarm.security.conf") == null) {
            try {
                //Path dir = Files.createTempDirectory("swarm-keycloak-config");
                File dir = TempFileManager.INSTANCE.newTempDirectory("swarm-drools-security-config", ".d");
                System.setProperty("org.drools.server.swarm.conf", dir.getAbsolutePath());
                Files.copy(getClass().getClassLoader().getResourceAsStream("config/security/application-users.properties"),
                           dir.toPath().resolve("application-users.properties"),
                           StandardCopyOption.REPLACE_EXISTING);
                Files.copy(getClass().getClassLoader().getResourceAsStream("config/security/application-roles.properties"),
                           dir.toPath().resolve("application-roles.properties"),
                           StandardCopyOption.REPLACE_EXISTING);
                configFolder = dir.toPath().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
        System.out.println("\tConfiguration folder is " + configFolder);
        LoginModule<?> loginModule = new LoginModule<>("UsersRoles");
        loginModule.flag(Flag.REQUIRED)
        .code("UsersRoles")
        .moduleOption("usersProperties", configFolder + "/application-users.properties")
        .moduleOption("rolesProperties", configFolder + "/application-roles.properties");
        
        SecurityDomain<?> security = new SecurityDomain<>("other-drools")
                .classicAuthentication(new ClassicAuthentication<>()
                        .loginModule(loginModule)); 
        
        SecurityFraction securityFraction = (SecurityFraction) initContext.fraction("security");
        securityFraction.securityDomain(security);
        
    }

}
