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
package org.wildfly.swarm.tools;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.env.FractionManifest;
import org.wildfly.swarm.bootstrap.env.WildFlySwarmManifest;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class DependencyManagerTest {

    private static MockArtifactResolver RESOLVER = new MockArtifactResolver();

    private static ArtifactSpec SERVLET_SPEC = simple("org.jboss.spec.javax.servlet:jboss-servlet-api_4.0_spec:jar:1.0.0.Final");
    private static ArtifactSpec JAXRS_SPEC = simple("org.jboss.spec.javax.ws.rs:jboss-jaxrs-api_2.1_spec:1.0.1.Final", (config)->{
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
        this.manager = new DependencyManager(RESOLVER, false);
    }

    @Test
    public void testNoSwarmJars() throws Exception {
        // Explicitly asked-for dependencies are also the full resolved tree
        DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        declaredDependencies.add(JAXRS_SPEC, SERVLET_SPEC);
        declaredDependencies.add(COMMON_DEP);

        manager.analyzeDependencies(false, declaredDependencies);
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
        DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        declaredDependencies.add(JAXRS_SPEC, SERVLET_SPEC);
        declaredDependencies.add(COMMON_DEP);

        declaredDependencies.add(JAXRS_FRACTION, JAXRS_SPEC);
        declaredDependencies.add(JAXRS_FRACTION, COMMON_DEP);
        declaredDependencies.add(JAXRS_FRACTION, UNDERTOW_FRACTION);
        declaredDependencies.add(JAXRS_SPEC, SERVLET_SPEC);

        manager.analyzeDependencies(true, declaredDependencies);
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
        DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        declaredDependencies.add(JAXRS_SPEC, SERVLET_SPEC);

        declaredDependencies.add(JAXRS_FRACTION, JAXRS_SPEC);
        declaredDependencies.add(JAXRS_FRACTION, COMMON_DEP);
        declaredDependencies.add(JAXRS_FRACTION, UNDERTOW_FRACTION);
        declaredDependencies.add(JAXRS_SPEC, SERVLET_SPEC);


        manager.analyzeDependencies(true, declaredDependencies);
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
        DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        declaredDependencies.add(JAXRS_FRACTION, JAXRS_SPEC);
        declaredDependencies.add(JAXRS_FRACTION, COMMON_DEP);
        declaredDependencies.add(JAXRS_FRACTION, UNDERTOW_FRACTION);
        declaredDependencies.add(JAXRS_FRACTION, SERVLET_SPEC);

        manager.analyzeDependencies(false, declaredDependencies);

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
        DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        declaredDependencies.add(JAXRS_FRACTION, JAXRS_SPEC);
        declaredDependencies.add(JAXRS_FRACTION, COMMON_DEP);
        declaredDependencies.add(JAXRS_FRACTION, UNDERTOW_FRACTION);
        declaredDependencies.add(JAXRS_FRACTION, SERVLET_SPEC);

        declaredDependencies.add(COMMON_DEP);


        manager.analyzeDependencies(false, declaredDependencies);
        assertThat(manager.getRemovableDependencies()).containsOnly(JAXRS_FRACTION, JAXRS_SPEC, UNDERTOW_FRACTION, SERVLET_SPEC);
        assertThat(manager.getDependencies()).containsOnly(JAXRS_FRACTION, JAXRS_SPEC, COMMON_DEP, UNDERTOW_FRACTION, SERVLET_SPEC);

        WildFlySwarmManifest manifest = manager.getWildFlySwarmManifest();
        assertThat(manifest.bootstrapArtifacts()).containsOnly(JAXRS_FRACTION.mavenGav(), UNDERTOW_FRACTION.mavenGav());
        assertThat(manifest.bootstrapModules()).containsOnly("org.wildfly.swarm.jaxrs", "org.wildfly.swarm.undertow");

        assertThat( manifest.getDependencies() ).containsOnly( COMMON_DEP.mavenGav() );
    }

    @Test
    public void testComparison() throws Exception {
        Set<ArtifactSpec> samples = new HashSet<>();
        samples.add(JAXRS_SPEC);
        samples.add(COMMON_DEP);

        ArtifactSpec sameCommonSpec = ArtifactSpec.fromMscGav("org.useful:utility:1.0");

        assertThat(samples).contains(sameCommonSpec);
        org.junit.Assert.assertFalse(samples.contains(SERVLET_SPEC));

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
