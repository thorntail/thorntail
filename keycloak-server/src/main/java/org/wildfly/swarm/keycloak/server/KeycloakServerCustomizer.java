package org.wildfly.swarm.keycloak.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.config.Infinispan;
import org.wildfly.swarm.config.infinispan.CacheContainer;
import org.wildfly.swarm.config.infinispan.cache_container.TransactionComponent;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.infinispan.InfinispanFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Post;

/**
 * @author Bob McWhirter
 */
@Post
@Singleton
public class KeycloakServerCustomizer implements Customizer {

    @Inject @Any
    private InfinispanFraction infinispan;

    @Inject @Any
    private DatasourcesFraction datasources;


    @Override
    public void customize() {

        if (System.getProperty("jboss.server.config.dir") == null) {
            try {
                //Path dir = Files.createTempDirectory("swarm-keycloak-config");
                File dir = TempFileManager.INSTANCE.newTempDirectory("swarm-keycloak-config", ".d");
                System.setProperty("jboss.server.config.dir", dir.getAbsolutePath());
                Files.copy(getClass().getClassLoader().getResourceAsStream("keycloak-server.json"),
                        dir.toPath().resolve("keycloak-server.json"),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        CacheContainer cache = infinispan.subresources().cacheContainer("keycloak");
        if (cache == null) {
            infinispan.cacheContainer("keycloak", (c) -> c.jndiName("infinispan/Keycloak")
                    .localCache("realms")
                    .localCache("users")
                    .localCache("sessions")
                    .localCache("offlineSessions")
                    .localCache("loginFailures")
                    .localCache("work")
                    .localCache("realmVersions", (ca) -> ca.transactionComponent(new TransactionComponent()
                            .mode(TransactionComponent.Mode.BATCH)
                            .locking(TransactionComponent.Locking.PESSIMISTIC)))
            );
        }

        if (datasources.subresources().dataSource("KeycloakDS") == null) {
            if (datasources.subresources().jdbcDriver("h2") == null) {
                datasources.jdbcDriver("h2", (driver) -> {
                    driver.driverModuleName("com.h2database.h2");
                    driver.moduleSlot("main");
                    driver.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                });
            }

            datasources.dataSource("KeycloakDS", (ds) -> {
                ds.jndiName("java:jboss/datasources/KeycloakDS");
                ds.useJavaContext(true);
                ds.connectionUrl("jdbc:h2:${wildfly.swarm.keycloak.server.db:./keycloak};AUTO_SERVER=TRUE");
                ds.driverName("h2");
                ds.userName("sa");
                ds.password("sa");
            });
        }

    }
}
