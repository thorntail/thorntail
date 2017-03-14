package org.wildfly.swarm.drools.server.runtime;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthentication;
import org.wildfly.swarm.config.security.security_domain.authentication.LoginModule;
import org.wildfly.swarm.security.SecurityFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Ken Finnigan
 */
@Post
@ApplicationScoped
public class DroolsSetup implements Customizer {

    private static String configFolder = System.getProperty("org.drools.server.swarm.security.conf");

    @Inject
    @Any
    Instance<SecurityFraction> security;

    @Override
    public void customize() throws Exception {
        if (System.getProperty("org.drools.server.swarm.security.conf") == null) {
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
        }


        LoginModule<?> loginModule = new LoginModule<>("UsersRoles");
        loginModule.flag(Flag.REQUIRED)
                .code("UsersRoles")
                .moduleOption("usersProperties", configFolder + "/application-users.properties")
                .moduleOption("rolesProperties", configFolder + "/application-roles.properties");

        if (!this.security.isUnsatisfied()) {
            SecurityDomain<?> security = new SecurityDomain<>("other-drools")
                    .classicAuthentication(new ClassicAuthentication<>()
                            .loginModule(loginModule));
            this.security.get().securityDomain(security);
        }

    }
}
