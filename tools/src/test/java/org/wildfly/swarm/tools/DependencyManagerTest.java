package org.wildfly.swarm.tools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
@SuppressWarnings("unchecked")
public class DependencyManagerTest {

    private ArtifactSpec BOOTSTRAP_JAR;

    private ArtifactSpec BOOTSTRAP_EMPTY_A;

    private ArtifactSpec BOOTSTRAP_EMPTY_B;

    private ArtifactSpec BOOTSTRAP_CONF;

    private ArtifactSpec MODULES_A;

    private ArtifactSpec PROVIDED_A;

    private ArtifactSpec COM_SUN_MAIL;

    private ArtifactSpec CXF;

    private ArtifactSpec WS_INTEGRATION;

    private MockArtifactResolver resolver;

    private DependencyManager manager;

    @Before
    public void setUp() throws Exception {
        BOOTSTRAP_JAR = ArtifactSpec.fromMscGav("org.wildfly.swarm:wildfly-swarm-bootstrap:1.0");

        BOOTSTRAP_EMPTY_A = ArtifactSpec.fromMscGav("test:bootstrap-empty-A:1.0");

        BOOTSTRAP_EMPTY_B = ArtifactSpec.fromMscGav("test:bootstrap-empty-B:1.0");

        BOOTSTRAP_CONF = ArtifactSpec.fromMscGav("test:bootstrap-conf:1.0");

        MODULES_A = ArtifactSpec.fromMscGav("test:with-modules-A:1.0");

        PROVIDED_A = ArtifactSpec.fromMscGav("test:provided-A:1.0");

        COM_SUN_MAIL = ArtifactSpec.fromMscGav("com.sun.mail:javax.mail:1.0");

        CXF = ArtifactSpec.fromMscGav("org.jboss.ws.cxf:jbossws-cxf-resources:5.1.0.Final:wildfly1000");

        WS_INTEGRATION = ArtifactSpec.fromMscGav("org.wildfly:wildfly-webservices-server-integration:10.0.0.CR4");

        resolver = new MockArtifactResolver();

        resolver.add(BOOTSTRAP_JAR, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "nothing");
        });

        resolver.add("test:no-module-A:1.0", (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "nothing");
        });

        resolver.add("test:no-module-B:1.0", (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "nothing");
        });

        resolver.add(BOOTSTRAP_EMPTY_A, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "wildfly-swarm-bootstrap.conf");
        });

        resolver.add(BOOTSTRAP_EMPTY_B, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "wildfly-swarm-bootstrap.conf");
        });

        resolver.add(BOOTSTRAP_CONF, (archive) -> {
            archive.add(new StringAsset(
                    "com.module1\n" +
                            "com.module2\n"
            ), "wildfly-swarm-bootstrap.conf");
        });

        resolver.add(MODULES_A, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "wildfly-swarm-bootstrap.conf");
            archive.add(new ClassLoaderAsset("module.xml"), "modules/org/jboss/as/webservices/main/module.xml");
        });

        resolver.add(PROVIDED_A, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "wildfly-swarm-bootstrap.conf");
            archive.add(new StringAsset(
                    "com.sun.mail:javax.mail\n" +
                            "org.keycloak:keycloak-core|org.keycloak.keycloak-core-module"
            ), "provided-dependencies.txt");

        });

        resolver.add(COM_SUN_MAIL, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "nothing");
        });

        resolver.add(CXF, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "nothing");
        });

        resolver.add(WS_INTEGRATION, (archive) -> {
            archive.add(EmptyAsset.INSTANCE, "nothing");
        });

        this.manager = new DependencyManager();
        this.manager.setArtifactResolvingHelper(resolver);
    }

    @After
    public void tearDownManager() {
        this.manager = null;
    }

    @Test
    public void analyzeDependenciesZero() throws Exception {
        manager.analyzeDependencies(false);
        assertThat(manager.getDependencies()).isEmpty();
    }

    @Test
    public void analyzeDependenciesNoModules() throws Exception {
        manager.addDependency(ArtifactSpec.fromMscGav("test:no-module-A:1.0"));
        manager.analyzeDependencies(false);
        assertThat(manager.getDependencies()).hasSize(1);
        assertThat(manager.getBootstrapDependencies()).isEmpty();
    }

    @Test
    public void analyzeDependenciesWithBootstrapJar() throws Exception {
        manager.addDependency(ArtifactSpec.fromMscGav("test:no-module-A:1.0"));
        manager.addDependency(BOOTSTRAP_JAR);
        manager.analyzeDependencies(false);
        assertThat(manager.getDependencies()).hasSize(2);
        assertThat(manager.getBootstrapDependencies()).hasSize(1);
        assertThat(manager.getBootstrapDependencies()).contains(BOOTSTRAP_JAR);
        assertThat(manager.getBootstrapModules()).isEmpty();
    }

    @Test
    public void analyzeDependenciesWithBootstrapJarAndBootstrapConf() throws Exception {
        manager.addDependency(BOOTSTRAP_JAR);
        manager.addDependency(ArtifactSpec.fromMscGav("test:no-module-A:1.0"));
        manager.addDependency(BOOTSTRAP_EMPTY_A);
        manager.addDependency(BOOTSTRAP_EMPTY_B);
        manager.analyzeDependencies(false);
        assertThat(manager.getDependencies()).hasSize(4);
        assertThat(manager.getBootstrapDependencies()).hasSize(3);
        assertThat(manager.getBootstrapDependencies()).contains(BOOTSTRAP_JAR);
        assertThat(manager.getBootstrapDependencies()).contains(BOOTSTRAP_EMPTY_A);
        assertThat(manager.getBootstrapDependencies()).contains(BOOTSTRAP_EMPTY_B);
        assertThat(manager.getBootstrapModules()).isEmpty();
    }

    @Test
    public void analyzeDependenciesWithBootstrapConfContents() throws Exception {
        manager.addDependency(BOOTSTRAP_JAR);
        manager.addDependency(BOOTSTRAP_CONF);
        manager.analyzeDependencies(false);
        assertThat(manager.getDependencies()).hasSize(2);
        assertThat(manager.getBootstrapDependencies()).hasSize(2);
        assertThat(manager.getBootstrapDependencies()).contains(BOOTSTRAP_JAR);
        assertThat(manager.getBootstrapDependencies()).contains(BOOTSTRAP_CONF);
        assertThat(manager.getBootstrapModules()).hasSize(2);
        assertThat(manager.getBootstrapModules()).contains("com.module1");
        assertThat(manager.getBootstrapModules()).contains("com.module2");
    }

    @Test
    public void analyzeDependenciesWithModuleXml() throws Exception {
        manager.addDependency(MODULES_A);
        manager.analyzeDependencies(false);
        assertThat(manager.getDependencies()).hasSize(1);
        assertThat(manager.getDependencies()).contains(MODULES_A);
        assertThat(manager.getBootstrapDependencies()).hasSize(1);
        assertThat(manager.getBootstrapDependencies()).contains(MODULES_A);

        assertThat(manager.getModuleDependencies()).contains(CXF);
        assertThat(manager.getModuleDependencies()).contains(WS_INTEGRATION);
    }

    @Test
    public void populateUberJarMavenRepository() throws Exception {
        manager.addDependency(BOOTSTRAP_JAR);
        manager.addDependency(BOOTSTRAP_CONF);
        manager.addDependency(BOOTSTRAP_EMPTY_A);
        manager.addDependency(MODULES_A);
        manager.analyzeDependencies(false);

        Archive archive = ShrinkWrap.create(JavaArchive.class);

        manager.populateUberJarMavenRepository(archive);

        Map<ArchivePath, Node> content = archive.getContent();

        List<String> jars = content.keySet().stream().map(ArchivePath::get).filter((e) -> e.endsWith(".jar")).collect(Collectors.toList());

        assertThat(jars).hasSize(5);
        assertThat(jars).contains("/m2repo/" + BOOTSTRAP_EMPTY_A.repoPath(true));
        assertThat(jars).contains("/m2repo/" + BOOTSTRAP_CONF.repoPath(true));
        assertThat(jars).contains("/m2repo/" + MODULES_A.repoPath(true));
        assertThat(jars).contains("/m2repo/" + CXF.repoPath(true));
        assertThat(jars).contains("/m2repo/" + WS_INTEGRATION.repoPath(true));
    }

    @Test
    public void analyzeDependenciesWithProvided() throws Exception {
        manager.addDependency(PROVIDED_A);
        manager.analyzeDependencies(false);

        assertThat(manager.getDependencies()).hasSize(1);
        assertThat(manager.getProvidedGAVs()).hasSize(3);
        assertThat(manager.getProvidedGAVs()).contains(PROVIDED_A.groupId() + ":" + PROVIDED_A.artifactId());
        assertThat(manager.getProvidedGAVs()).contains("com.sun.mail:javax.mail");
        assertThat(manager.getProvidedGAVs()).contains("org.keycloak:keycloak-core");

        assertThat(manager.getProvidedGAVToModuleMappings()).hasSize(1);
        assertThat(manager.getProvidedGAVToModuleMappings().get("org.keycloak:keycloak-core")).isNotNull();
        assertThat(manager.getProvidedGAVToModuleMappings().get("org.keycloak:keycloak-core")).isEqualTo("org.keycloak.keycloak-core-module");
    }

    @Test
    public void populateUberJarMavenRepositoryAvoidingProvided() throws Exception {
        manager.addDependency(BOOTSTRAP_JAR);
        manager.addDependency(BOOTSTRAP_CONF);
        manager.addDependency(BOOTSTRAP_EMPTY_A);
        manager.addDependency(MODULES_A);
        manager.addDependency(PROVIDED_A);
        manager.addDependency(COM_SUN_MAIL);
        manager.analyzeDependencies(false);

        assertThat(manager.getProvidedGAVs()).contains("com.sun.mail:javax.mail");

        Archive archive = ShrinkWrap.create(JavaArchive.class);

        manager.populateUberJarMavenRepository(archive);

        Map<ArchivePath, Node> content = archive.getContent();

        List<String> jars = content.keySet().stream().map(ArchivePath::get).filter((e) -> e.endsWith(".jar")).collect(Collectors.toList());

        assertThat(jars).hasSize(6);
        assertThat(jars).contains("/m2repo/" + BOOTSTRAP_EMPTY_A.repoPath(true));
        assertThat(jars).contains("/m2repo/" + BOOTSTRAP_CONF.repoPath(true));
        assertThat(jars).contains("/m2repo/" + MODULES_A.repoPath(true));
        assertThat(jars).contains("/m2repo/" + CXF.repoPath(true));
        assertThat(jars).contains("/m2repo/" + WS_INTEGRATION.repoPath(true));
        assertThat(jars).contains("/m2repo/" + PROVIDED_A.repoPath(true));
    }

    @Test
    public void analyzeDependenciesUnresolveable() throws Exception {
        manager.addDependency(ArtifactSpec.fromMscGav("no:such-thing:1.0"));
        manager.analyzeDependencies(false);

    }

}
