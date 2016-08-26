package org.wildfly.swarm.tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.env.FractionManifest;
import org.wildfly.swarm.bootstrap.env.WildFlySwarmManifest;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class DependencyManagerTest {

    private static MockArtifactResolver RESOLVER = new MockArtifactResolver();

    private static ArtifactSpec SERVLET_SPEC = simple("org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:jar:1.0.0.Final");
    private static ArtifactSpec JAXRS_SPEC = simple("org.jboss.spec.javax.ws.rs:jboss-jaxrs-api_2.0_spec:1.0.0.Final", (config)->{
        config.addDependency( SERVLET_SPEC );
    });

    private static ArtifactSpec COMMON_DEP = simple("org.useful:utility:1.0");

    private static ArtifactSpec UNDERTOW_FRACTION = fraction("org.wildfly.swarm:undertow", (config) -> {
        config.addDependency(SERVLET_SPEC);
        config.addDependency(COMMON_DEP);
    });

    private static ArtifactSpec JAXRS_FRACTION = fraction("org.wildfly.swarm:jaxrs", (config) -> {
        config.addDependency(JAXRS_SPEC);
        config.addDependency(COMMON_DEP);
        config.addDependency(UNDERTOW_FRACTION);
    });

    private DependencyManager manager;

    @Before
    public void setUp() {
        this.manager = new DependencyManager();
        this.manager.setArtifactResolvingHelper(RESOLVER);
    }

    @Test
    public void testNoSwarmJars() throws Exception {
        // Explicitly asked-for dependencies are also the full resolved tree
        manager.addExplicitDependency(JAXRS_SPEC);
        manager.addExplicitDependency(COMMON_DEP);

        manager.addPresolvedDependency(JAXRS_SPEC);
        manager.addPresolvedDependency(COMMON_DEP);
        manager.addPresolvedDependency(SERVLET_SPEC);

        manager.analyzeDependencies(false);
        assertThat(manager.getRemovableDependencies()).isEmpty();

        assertThat(manager.getDependencies()).containsOnly(JAXRS_SPEC, SERVLET_SPEC, COMMON_DEP);

        WildFlySwarmManifest manifest = manager.getWildFlySwarmManifest();

        // these are empty because we have not actually done any auto-detection.
        assertThat(manifest.bootstrapArtifacts()).isEmpty();
        assertThat(manifest.bootstrapModules()).isEmpty();

        assertThat(manifest.getDependencies()).containsOnly(
                SERVLET_SPEC.mavenGav(),
                JAXRS_SPEC.mavenGav(),
                COMMON_DEP.mavenGav() );
    }

    @Test
    public void testAutodetectedSwarmJar() throws Exception {
        // Explicitly asked-for dependencies are also the full resolved tree
        manager.addExplicitDependency(JAXRS_SPEC);
        manager.addExplicitDependency(COMMON_DEP);

        // auto-detected, not pre-solved
        manager.addExplicitDependency(JAXRS_FRACTION);

        manager.addPresolvedDependency(JAXRS_SPEC);
        manager.addPresolvedDependency(SERVLET_SPEC);
        manager.addPresolvedDependency(COMMON_DEP);

        manager.analyzeDependencies(true);
        assertThat(manager.getRemovableDependencies()).containsOnly(JAXRS_FRACTION, UNDERTOW_FRACTION);
        assertThat(manager.getDependencies()).containsOnly(JAXRS_FRACTION, UNDERTOW_FRACTION, JAXRS_SPEC, SERVLET_SPEC, COMMON_DEP);

        WildFlySwarmManifest manifest = manager.getWildFlySwarmManifest();

        assertThat(manifest.bootstrapArtifacts()).containsOnly( JAXRS_FRACTION.mavenGav(), UNDERTOW_FRACTION.mavenGav() );
        assertThat(manifest.bootstrapModules()).containsOnly( "org.wildfly.swarm.jaxrs", "org.wildfly.swarm.undertow" );

        // still includes the jaxrs-spec because it was explicit and we just can't know
        assertThat(manifest.getDependencies()).containsOnly(
                SERVLET_SPEC.mavenGav(),
                JAXRS_SPEC.mavenGav(),
                COMMON_DEP.mavenGav() );
    }

    @Test
    public void testAutodetectedSwarmJarNoExplicitCommon() throws Exception {
        // Explicitly asked-for dependencies are also the full resolved tree
        manager.addExplicitDependency(JAXRS_SPEC);

        // auto-detected, not pre-solved
        manager.addExplicitDependency(JAXRS_FRACTION);

        manager.addPresolvedDependency(JAXRS_SPEC);
        manager.addPresolvedDependency(SERVLET_SPEC);

        manager.analyzeDependencies(true);
        assertThat(manager.getRemovableDependencies()).containsOnly(JAXRS_FRACTION, UNDERTOW_FRACTION, COMMON_DEP);
        assertThat(manager.getDependencies()).containsOnly(JAXRS_FRACTION, UNDERTOW_FRACTION, SERVLET_SPEC, JAXRS_SPEC, COMMON_DEP);

        WildFlySwarmManifest manifest = manager.getWildFlySwarmManifest();

        assertThat(manifest.bootstrapArtifacts()).containsOnly( JAXRS_FRACTION.mavenGav(), UNDERTOW_FRACTION.mavenGav() );
        assertThat(manifest.bootstrapModules()).containsOnly( "org.wildfly.swarm.jaxrs", "org.wildfly.swarm.undertow" );

        // still includes the jaxrs-spec because it was explicit and we just can't know
        assertThat(manifest.getDependencies()).containsOnly( JAXRS_SPEC.mavenGav(), SERVLET_SPEC.mavenGav() );
    }

    @Test
    public void testWithSwarmJarBeingOnlyUserOfDep() throws Exception {
        // Only :jaxrs is the explicit resolved tree, but brings in jaxrs-spec and common dep
        manager.addExplicitDependency(JAXRS_FRACTION);

        manager.addPresolvedDependency(JAXRS_FRACTION);
        manager.addPresolvedDependency(JAXRS_SPEC);
        manager.addPresolvedDependency(UNDERTOW_FRACTION);
        manager.addPresolvedDependency(SERVLET_SPEC);
        manager.addPresolvedDependency(COMMON_DEP);
        manager.analyzeDependencies(false);

        assertThat(manager.getRemovableDependencies()).containsOnly(JAXRS_FRACTION, JAXRS_SPEC, UNDERTOW_FRACTION, SERVLET_SPEC, COMMON_DEP);
        assertThat(manager.getDependencies()).containsOnly(JAXRS_FRACTION, JAXRS_SPEC, UNDERTOW_FRACTION, SERVLET_SPEC, COMMON_DEP);

        WildFlySwarmManifest manifest = manager.getWildFlySwarmManifest();
        assertThat(manifest.bootstrapArtifacts()).containsOnly(JAXRS_FRACTION.mavenGav(), UNDERTOW_FRACTION.mavenGav());
        assertThat(manifest.bootstrapModules()).containsOnly("org.wildfly.swarm.jaxrs", "org.wildfly.swarm.undertow");

        assertThat(manifest.getDependencies()).isEmpty();
    }

    @Test
    public void testWithApplicationAlsoUsingDep() throws Exception {
        // :jaxrs and common dep are explicit, implying application needs common

        manager.addExplicitDependency(JAXRS_FRACTION);
        manager.addExplicitDependency(COMMON_DEP);

        manager.addPresolvedDependency(JAXRS_FRACTION);
        manager.addPresolvedDependency(JAXRS_SPEC);
        manager.addPresolvedDependency(UNDERTOW_FRACTION);
        manager.addPresolvedDependency(SERVLET_SPEC);
        manager.addPresolvedDependency(COMMON_DEP);

        manager.analyzeDependencies(false);
        assertThat(manager.getRemovableDependencies()).containsOnly(JAXRS_FRACTION, JAXRS_SPEC, UNDERTOW_FRACTION, SERVLET_SPEC);
        assertThat(manager.getDependencies()).containsOnly(JAXRS_FRACTION, JAXRS_SPEC, COMMON_DEP, UNDERTOW_FRACTION, SERVLET_SPEC);

        WildFlySwarmManifest manifest = manager.getWildFlySwarmManifest();
        assertThat(manifest.bootstrapArtifacts()).containsOnly(JAXRS_FRACTION.mavenGav(), UNDERTOW_FRACTION.mavenGav());
        assertThat(manifest.bootstrapModules()).containsOnly("org.wildfly.swarm.jaxrs", "org.wildfly.swarm.undertow");

        assertThat( manifest.getDependencies() ).containsOnly( COMMON_DEP.mavenGav() );
    }

    private static ArtifactSpec simple(String gav) {
        ArtifactSpec spec = ArtifactSpec.fromMscGav(gav);
        RESOLVER.add(spec);
        return spec;
    }

    private static ArtifactSpec simple(String gav, Consumer<MockArtifactResolver.Entry> config) {
        ArtifactSpec spec = ArtifactSpec.fromMscGav(gav);
        RESOLVER.add(spec, config);
        return spec;
    }

    private static ArtifactSpec fraction(String ga, Consumer<MockArtifactResolver.Entry> config) {
        String moduleName = ga.replace(':', '.');
        String gav = ga + ":" + System.getProperty("project.version");
        ArtifactSpec spec = ArtifactSpec.fromMscGav(gav);

        JavaArchive jar = ShrinkWrap.create( JavaArchive.class );

        StringBuilder yaml = new StringBuilder();

        yaml.append( "module: " + moduleName );

        jar.add(new StringAsset(yaml.toString()), FractionManifest.CLASSPATH_LOCATION );

        RESOLVER.add(spec, jar, config);

        return spec;
    }

}
